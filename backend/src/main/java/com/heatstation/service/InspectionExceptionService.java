package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.common.PageResult;
import com.heatstation.entity.InspectionException;
import com.heatstation.entity.RepairOrder;
import com.heatstation.entity.Station;
import com.heatstation.mapper.InspectionExceptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class InspectionExceptionService extends ServiceImpl<InspectionExceptionMapper, InspectionException> {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private StationService stationService;

    @Autowired
    private OrderFlowLogService flowLogService;

    public PageResult<InspectionException> queryPage(Long pageNum, Long pageSize, String status,
                                                      String exceptionLevel, Long stationId, Date startTime, Date endTime) {
        Page<InspectionException> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InspectionException> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(InspectionException::getStatus, status);
        }
        if (StringUtils.hasText(exceptionLevel)) {
            wrapper.eq(InspectionException::getExceptionLevel, exceptionLevel);
        }
        if (stationId != null) {
            wrapper.eq(InspectionException::getStationId, stationId);
        }
        if (startTime != null) {
            wrapper.ge(InspectionException::getReportTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InspectionException::getReportTime, endTime);
        }
        wrapper.orderByDesc(InspectionException::getReportTime);
        Page<InspectionException> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmException(Long exceptionId, Long handlerId, String handlerName) {
        InspectionException exception = this.getById(exceptionId);
        if (exception == null) {
            throw new RuntimeException("异常记录不存在");
        }
        if (!"REPORTED".equals(exception.getStatus())) {
            throw new RuntimeException("只有已上报状态的异常可以确认");
        }

        exception.setStatus("CONFIRMED");
        exception.setHandlerId(handlerId);
        exception.setHandlerName(handlerName);
        exception.setHandleTime(new Date());
        boolean result = this.updateById(exception);

        flowLogService.addLog("EXCEPTION", exceptionId, exception.getExceptionNo(),
                "CONFIRM", "确认异常",
                handlerId, handlerName, null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public RepairOrder dispatchRepair(Long exceptionId, Long dispatcherId, String dispatcherName,
                                       Long repairTeamId, String repairTeamName) {
        InspectionException exception = this.getById(exceptionId);
        if (exception == null) {
            throw new RuntimeException("异常记录不存在");
        }
        if (!"CONFIRMED".equals(exception.getStatus()) && !"REPORTED".equals(exception.getStatus())) {
            throw new RuntimeException("当前状态不能派单");
        }

        Station station = stationService.getById(exception.getStationId());

        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setOrderNo(repairOrderService.generateOrderNo());
        repairOrder.setSourceType("INSPECTION");
        repairOrder.setSourceId(exceptionId);
        repairOrder.setStationId(exception.getStationId());
        repairOrder.setStationCode(exception.getStationCode());
        repairOrder.setStationName(exception.getStationName());
        repairOrder.setFaultType(mapExceptionTypeToFaultType(exception.getExceptionType()));
        repairOrder.setFaultLevel(exception.getExceptionLevel());
        repairOrder.setFaultDesc(exception.getExceptionDesc());
        repairOrder.setDispatcherId(dispatcherId);
        repairOrder.setDispatcherName(dispatcherName);
        repairOrder.setDispatchTime(new Date());
        repairOrder.setRepairTeamId(repairTeamId);
        repairOrder.setRepairTeamName(repairTeamName);
        repairOrder.setStatus("DISPATCHED");

        repairOrderService.save(repairOrder);

        exception.setStatus("DISPATCHED");
        exception.setRepairOrderId(repairOrder.getId());
        this.updateById(exception);

        flowLogService.addLog("EXCEPTION", exceptionId, exception.getExceptionNo(),
                "DISPATCH", "派单抢修，工单号：" + repairOrder.getOrderNo(),
                dispatcherId, dispatcherName, null);

        flowLogService.addLog("REPAIR", repairOrder.getId(), repairOrder.getOrderNo(),
                "CREATE", "由异常单" + exception.getExceptionNo() + "创建",
                dispatcherId, dispatcherName, null);

        return repairOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    public RepairOrder autoUpgradeToRepair(Long exceptionId, Long dispatcherId, String reason) {
        InspectionException exception = this.getById(exceptionId);
        if (exception == null) {
            throw new RuntimeException("异常记录不存在");
        }

        Station station = stationService.getById(exception.getStationId());

        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setOrderNo(repairOrderService.generateOrderNo());
        repairOrder.setSourceType("INSPECTION");
        repairOrder.setSourceId(exceptionId);
        repairOrder.setStationId(exception.getStationId());
        repairOrder.setStationCode(exception.getStationCode());
        repairOrder.setStationName(exception.getStationName());
        repairOrder.setFaultType(mapExceptionTypeToFaultType(exception.getExceptionType()));
        repairOrder.setFaultLevel(exception.getExceptionLevel());
        repairOrder.setFaultDesc("【自动升级】" + reason + "。异常描述：" + exception.getExceptionDesc());
        repairOrder.setDispatcherId(dispatcherId);
        repairOrder.setDispatchTime(new Date());
        repairOrder.setStatus("CREATED");

        repairOrderService.save(repairOrder);

        exception.setStatus("DISPATCHED");
        exception.setAutoUpgrade(1);
        exception.setUpgradeReason(reason);
        exception.setRepairOrderId(repairOrder.getId());
        this.updateById(exception);

        flowLogService.addLog("EXCEPTION", exceptionId, exception.getExceptionNo(),
                "UPGRADE", "自动升级抢修工单，原因：" + reason,
                null, "系统自动", null);

        flowLogService.addLog("REPAIR", repairOrder.getId(), repairOrder.getOrderNo(),
                "CREATE", "由异常单" + exception.getExceptionNo() + "自动升级创建",
                null, "系统自动", null);

        return repairOrder;
    }

    private String mapExceptionTypeToFaultType(String exceptionType) {
        switch (exceptionType) {
            case "PRESSURE_HIGH":
            case "PRESSURE_LOW":
                return "PRESSURE";
            case "TEMP_HIGH":
            case "TEMP_LOW":
                return "TEMPERATURE";
            case "DEVICE_FAULT":
                return "DEVICE";
            default:
                return "OTHER";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean closeException(Long exceptionId, Long closerId, String closerName, String closeRemark) {
        InspectionException exception = this.getById(exceptionId);
        if (exception == null) {
            throw new RuntimeException("异常记录不存在");
        }
        if ("CLOSED".equals(exception.getStatus())) {
            throw new RuntimeException("异常已关闭");
        }

        if (exception.getRepairOrderId() != null) {
            RepairOrder repairOrder = repairOrderService.getById(exception.getRepairOrderId());
            if (repairOrder != null && !"CONFIRMED".equals(repairOrder.getStatus())
                    && !"CLOSED".equals(repairOrder.getStatus())
                    && !"FINISHED".equals(repairOrder.getStatus())) {
                throw new RuntimeException("关联的抢修工单未完成，不能关闭异常");
            }
        }

        exception.setStatus("CLOSED");
        exception.setCloseTime(new Date());
        exception.setCloserId(closerId);
        exception.setCloserName(closerName);
        exception.setRemark(closeRemark);
        boolean result = this.updateById(exception);

        flowLogService.addLog("EXCEPTION", exceptionId, exception.getExceptionNo(),
                "CLOSE", "关闭异常",
                closerId, closerName, closeRemark);

        return result;
    }
}
