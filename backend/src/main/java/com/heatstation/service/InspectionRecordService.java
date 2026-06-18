package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.common.PageResult;
import com.heatstation.entity.*;
import com.heatstation.mapper.InspectionRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InspectionRecordService extends ServiceImpl<InspectionRecordMapper, InspectionRecord> {

    @Autowired
    private StationService stationService;

    @Autowired
    private InspectionRouteStationService routeStationService;

    @Autowired
    private InspectionRouteService routeService;

    @Autowired
    private InspectionExceptionService exceptionService;

    @Autowired
    private StationMetricHistoryService metricHistoryService;

    @Autowired
    private OrderFlowLogService flowLogService;

    public PageResult<InspectionRecord> queryPage(Long pageNum, Long pageSize, Long stationId,
                                                   Long inspectorId, Long routeId, Date startTime, Date endTime) {
        Page<InspectionRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InspectionRecord> wrapper = new LambdaQueryWrapper<>();
        if (stationId != null) {
            wrapper.eq(InspectionRecord::getStationId, stationId);
        }
        if (inspectorId != null) {
            wrapper.eq(InspectionRecord::getInspectorId, inspectorId);
        }
        if (routeId != null) {
            wrapper.eq(InspectionRecord::getRouteId, routeId);
        }
        if (startTime != null) {
            wrapper.ge(InspectionRecord::getInspectionTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InspectionRecord::getInspectionTime, endTime);
        }
        wrapper.orderByDesc(InspectionRecord::getInspectionTime);
        Page<InspectionRecord> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public InspectionRecord submitInspection(InspectionRecord record, Long routeStationId) {
        if (record.getPressure() == null) {
            throw new RuntimeException("请填写现场压力读数");
        }
        if (record.getSupplyTemp() == null) {
            throw new RuntimeException("请填写供水温度读数");
        }
        if (record.getReturnTemp() == null) {
            throw new RuntimeException("请填写回水温度读数");
        }

        Station station = stationService.getById(record.getStationId());
        if (station == null) {
            throw new RuntimeException("站点不存在");
        }

        String pressureStatus = checkPressureStatus(record.getPressure(), station);
        String tempStatus = checkTempStatus(record.getSupplyTemp(), record.getReturnTemp(), station);
        boolean hasException = !"NORMAL".equals(pressureStatus) || !"NORMAL".equals(tempStatus);
        String exceptionLevel = calculateExceptionLevel(pressureStatus, tempStatus, station.getStationType());

        record.setRecordNo(generateRecordNo());
        record.setStationCode(station.getStationCode());
        record.setStationName(station.getStationName());
        record.setPressureStatus(pressureStatus);
        record.setTempStatus(tempStatus);
        record.setHasException(hasException ? 1 : 0);
        record.setExceptionLevel(hasException ? exceptionLevel : null);
        record.setInspectionTime(new Date());
        record.setSubmitTime(new Date());

        if (record.getRouteStationId() == null) {
            record.setRouteStationId(routeStationId);
        }

        this.save(record);

        if (routeStationId != null) {
            updateRouteStationStatus(routeStationId, record, station);
        }

        if (record.getRouteId() != null) {
            routeService.incrementCompletedStations(record.getRouteId());
        }

        saveMetricHistory(record, station);

        if (hasException) {
            List<InspectionException> exceptions = createExceptions(record, station);
            for (InspectionException ex : exceptions) {
                if ("CRITICAL".equals(ex.getExceptionLevel()) && "PRIMARY".equals(station.getStationType())) {
                    exceptionService.autoUpgradeToRepair(ex.getId(), null, "一次网压力严重越限，自动升级抢修");
                }
            }
        }

        flowLogService.addLog("RECORD", record.getId(), record.getRecordNo(),
                "SUBMIT", "提交巡检记录",
                record.getInspectorId(), record.getInspectorName(),
                hasException ? "存在异常：" + exceptionLevel : "正常");

        return record;
    }

    private String checkPressureStatus(BigDecimal pressure, Station station) {
        if (station.getPressureLimitMin() != null && pressure.compareTo(station.getPressureLimitMin()) < 0) {
            return "BELOW_LIMIT";
        }
        if (station.getPressureLimitMax() != null && pressure.compareTo(station.getPressureLimitMax()) > 0) {
            return "ABOVE_LIMIT";
        }
        return "NORMAL";
    }

    private String checkTempStatus(BigDecimal supplyTemp, BigDecimal returnTemp, Station station) {
        if (station.getTempLimitMin() != null && supplyTemp.compareTo(station.getTempLimitMin()) < 0) {
            return "BELOW_LIMIT";
        }
        if (station.getTempLimitMax() != null && supplyTemp.compareTo(station.getTempLimitMax()) > 0) {
            return "ABOVE_LIMIT";
        }
        return "NORMAL";
    }

    private String calculateExceptionLevel(String pressureStatus, String tempStatus, String stationType) {
        boolean pressureCritical = "ABOVE_LIMIT".equals(pressureStatus) || "BELOW_LIMIT".equals(pressureStatus);
        boolean tempWarning = "ABOVE_LIMIT".equals(tempStatus) || "BELOW_LIMIT".equals(tempStatus);

        if (pressureCritical && "PRIMARY".equals(stationType)) {
            return "CRITICAL";
        }
        if (pressureCritical || tempWarning) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private void updateRouteStationStatus(Long routeStationId, InspectionRecord record, Station station) {
        InspectionRouteStation rs = routeStationService.getById(routeStationId);
        if (rs != null) {
            rs.setInspectionStatus("COMPLETED");
            rs.setInspectionTime(new Date());
            rs.setInspectorId(record.getInspectorId());
            rs.setInspectorName(record.getInspectorName());
            rs.setPressure(record.getPressure());
            rs.setSupplyTemp(record.getSupplyTemp());
            rs.setReturnTemp(record.getReturnTemp());
            rs.setHasException(record.getHasException());
            rs.setRemark(record.getRemark());
            routeStationService.updateById(rs);
        }
    }

    private void saveMetricHistory(InspectionRecord record, Station station) {
        List<StationMetricHistory> histories = new ArrayList<>();

        StationMetricHistory pressureHistory = new StationMetricHistory();
        pressureHistory.setStationId(station.getId());
        pressureHistory.setStationCode(station.getStationCode());
        pressureHistory.setStationName(station.getStationName());
        pressureHistory.setMetricType("PRESSURE");
        pressureHistory.setMetricValue(record.getPressure());
        pressureHistory.setMetricStatus(record.getPressureStatus());
        pressureHistory.setSourceType("INSPECTION");
        pressureHistory.setRecordId(record.getId());
        pressureHistory.setRecordTime(record.getInspectionTime());
        histories.add(pressureHistory);

        StationMetricHistory supplyTempHistory = new StationMetricHistory();
        supplyTempHistory.setStationId(station.getId());
        supplyTempHistory.setStationCode(station.getStationCode());
        supplyTempHistory.setStationName(station.getStationName());
        supplyTempHistory.setMetricType("SUPPLY_TEMP");
        supplyTempHistory.setMetricValue(record.getSupplyTemp());
        supplyTempHistory.setMetricStatus(record.getTempStatus());
        supplyTempHistory.setSourceType("INSPECTION");
        supplyTempHistory.setRecordId(record.getId());
        supplyTempHistory.setRecordTime(record.getInspectionTime());
        histories.add(supplyTempHistory);

        StationMetricHistory returnTempHistory = new StationMetricHistory();
        returnTempHistory.setStationId(station.getId());
        returnTempHistory.setStationCode(station.getStationCode());
        returnTempHistory.setStationName(station.getStationName());
        returnTempHistory.setMetricType("RETURN_TEMP");
        returnTempHistory.setMetricValue(record.getReturnTemp());
        returnTempHistory.setMetricStatus("NORMAL");
        returnTempHistory.setSourceType("INSPECTION");
        returnTempHistory.setRecordId(record.getId());
        returnTempHistory.setRecordTime(record.getInspectionTime());
        histories.add(returnTempHistory);

        metricHistoryService.saveBatch(histories);
    }

    private List<InspectionException> createExceptions(InspectionRecord record, Station station) {
        List<InspectionException> exceptions = new ArrayList<>();

        if ("ABOVE_LIMIT".equals(record.getPressureStatus())) {
            InspectionException ex = buildException(record, station,
                    "PRESSURE_HIGH", "压力过高",
                    record.getPressure(), station.getPressureLimitMax(),
                    "CRITICAL".equals(record.getExceptionLevel()) ? "CRITICAL" : "WARNING");
            exceptions.add(ex);
        }
        if ("BELOW_LIMIT".equals(record.getPressureStatus())) {
            InspectionException ex = buildException(record, station,
                    "PRESSURE_LOW", "压力过低",
                    record.getPressure(), station.getPressureLimitMin(),
                    "CRITICAL".equals(record.getExceptionLevel()) ? "CRITICAL" : "WARNING");
            exceptions.add(ex);
        }
        if ("ABOVE_LIMIT".equals(record.getTempStatus())) {
            InspectionException ex = buildException(record, station,
                    "TEMP_HIGH", "温度过高",
                    record.getSupplyTemp(), station.getTempLimitMax(), "WARNING");
            ex.setSupplyTempVal(record.getSupplyTemp());
            exceptions.add(ex);
        }
        if ("BELOW_LIMIT".equals(record.getTempStatus())) {
            InspectionException ex = buildException(record, station,
                    "TEMP_LOW", "温度过低",
                    record.getSupplyTemp(), station.getTempLimitMin(), "WARNING");
            ex.setSupplyTempVal(record.getSupplyTemp());
            exceptions.add(ex);
        }

        return exceptions;
    }

    private InspectionException buildException(InspectionRecord record, Station station,
                                               String exceptionType, String exceptionDesc,
                                               BigDecimal value, BigDecimal limit, String level) {
        InspectionException ex = new InspectionException();
        ex.setExceptionNo(generateExceptionNo());
        ex.setRouteId(record.getRouteId());
        ex.setRecordId(record.getId());
        ex.setStationId(station.getId());
        ex.setStationCode(station.getStationCode());
        ex.setStationName(station.getStationName());
        ex.setExceptionType(exceptionType);
        ex.setExceptionLevel(level);
        ex.setExceptionDesc(exceptionDesc + "，实测值：" + value + "，限值：" + limit);
        ex.setPressureVal(value);
        ex.setLimitMin("PRESSURE_LOW".equals(exceptionType) || "TEMP_LOW".equals(exceptionType) ? limit : null);
        ex.setLimitMax("PRESSURE_HIGH".equals(exceptionType) || "TEMP_HIGH".equals(exceptionType) ? limit : null);
        ex.setReporterId(record.getInspectorId());
        ex.setReporterName(record.getInspectorName());
        ex.setReportTime(new Date());
        ex.setStatus("REPORTED");
        ex.setAutoUpgrade(0);
        exceptionService.save(ex);
        return ex;
    }

    private String generateRecordNo() {
        return "RC" + System.currentTimeMillis();
    }

    private String generateExceptionNo() {
        return "EX" + System.currentTimeMillis();
    }
}
