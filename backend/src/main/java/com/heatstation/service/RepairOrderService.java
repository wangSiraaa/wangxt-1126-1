package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.common.PageResult;
import com.heatstation.entity.ColdComplaint;
import com.heatstation.entity.RepairOrder;
import com.heatstation.entity.StationMetricHistory;
import com.heatstation.mapper.RepairOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RepairOrderService extends ServiceImpl<RepairOrderMapper, RepairOrder> {

    @Autowired
    private OrderFlowLogService flowLogService;

    @Autowired
    private StationService stationService;

    @Autowired
    private StationMetricHistoryService metricHistoryService;

    @Autowired
    private ColdComplaintService coldComplaintService;

    public PageResult<RepairOrder> queryPage(Long pageNum, Long pageSize, String status,
                                              String faultLevel, Long stationId,
                                              Long repairTeamId, Date startTime, Date endTime) {
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (StringUtils.hasText(faultLevel)) {
            wrapper.eq(RepairOrder::getFaultLevel, faultLevel);
        }
        if (stationId != null) {
            wrapper.eq(RepairOrder::getStationId, stationId);
        }
        if (repairTeamId != null) {
            wrapper.eq(RepairOrder::getRepairTeamId, repairTeamId);
        }
        if (startTime != null) {
            wrapper.ge(RepairOrder::getDispatchTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(RepairOrder::getDispatchTime, endTime);
        }
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        Page<RepairOrder> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    public String generateOrderNo() {
        return "RO" + System.currentTimeMillis();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean acceptOrder(Long orderId, Long repairTeamId, String repairTeamName,
                               String repairPerson, String repairPhone) {
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("抢修工单不存在");
        }
        if (!"DISPATCHED".equals(order.getStatus()) && !"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不能接单");
        }

        order.setStatus("ACCEPTED");
        order.setRepairTeamId(repairTeamId);
        order.setRepairTeamName(repairTeamName);
        order.setRepairPerson(repairPerson);
        order.setRepairPhone(repairPhone);
        order.setAcceptTime(new Date());
        boolean result = this.updateById(order);

        flowLogService.addLog("REPAIR", orderId, order.getOrderNo(),
                "ACCEPT", "接单",
                repairTeamId, repairTeamName, null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean arriveSite(Long orderId) {
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("抢修工单不存在");
        }
        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不能到达现场");
        }

        order.setStatus("IN_PROGRESS");
        order.setArriveTime(new Date());
        order.setStartTime(new Date());
        boolean result = this.updateById(order);

        flowLogService.addLog("REPAIR", orderId, order.getOrderNo(),
                "START", "到达现场，开始抢修",
                order.getRepairTeamId(), order.getRepairTeamName(), null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean finishRepair(Long orderId, BigDecimal pressureAfter,
                                 BigDecimal supplyTempAfter, BigDecimal returnTempAfter,
                                 String repairContent, String repairMaterial, BigDecimal workHours) {
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("抢修工单不存在");
        }
        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不能完成抢修");
        }

        order.setStatus("FINISHED");
        order.setFinishTime(new Date());
        order.setPressureAfter(pressureAfter);
        order.setSupplyTempAfter(supplyTempAfter);
        order.setReturnTempAfter(returnTempAfter);
        order.setRepairContent(repairContent);
        order.setRepairMaterial(repairMaterial);
        order.setWorkHours(workHours);
        boolean result = this.updateById(order);

        if (pressureAfter != null) {
            saveRepairMetricHistory(order, "PRESSURE", pressureAfter);
        }
        if (supplyTempAfter != null) {
            saveRepairMetricHistory(order, "SUPPLY_TEMP", supplyTempAfter);
        }
        if (returnTempAfter != null) {
            saveRepairMetricHistory(order, "RETURN_TEMP", returnTempAfter);
        }

        flowLogService.addLog("REPAIR", orderId, order.getOrderNo(),
                "FINISH", "完成抢修",
                order.getRepairTeamId(), order.getRepairTeamName(),
                repairContent != null ? repairContent.substring(0, Math.min(100, repairContent.length())) : null);

        updateLinkedExceptions(orderId, "REPAIRED");

        return result;
    }

    private void saveRepairMetricHistory(RepairOrder order, String metricType, BigDecimal value) {
        StationMetricHistory history = new StationMetricHistory();
        history.setStationId(order.getStationId());
        history.setStationCode(order.getStationCode());
        history.setStationName(order.getStationName());
        history.setMetricType(metricType);
        history.setMetricValue(value);
        history.setMetricStatus("NORMAL");
        history.setSourceType("MANUAL");
        history.setRecordId(order.getId());
        history.setRecordTime(new Date());
        metricHistoryService.save(history);
    }

    private void updateLinkedExceptions(Long repairOrderId, String status) {
        List<com.heatstation.entity.InspectionException> exceptions =
                listExceptionsByRepairOrderId(repairOrderId);
        for (com.heatstation.entity.InspectionException ex : exceptions) {
            ex.setStatus(status);
        }
    }

    private List<com.heatstation.entity.InspectionException> listExceptionsByRepairOrderId(Long repairOrderId) {
        return new ArrayList<>();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmRepair(Long orderId, Long confirmerId, String confirmerName) {
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("抢修工单不存在");
        }
        if (!"FINISHED".equals(order.getStatus())) {
            throw new RuntimeException("只有已完成的工单可以确认");
        }

        order.setStatus("CONFIRMED");
        boolean result = this.updateById(order);

        flowLogService.addLog("REPAIR", orderId, order.getOrderNo(),
                "CONFIRM", "确认抢修完成",
                confirmerId, confirmerName, null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean closeOrder(Long orderId, Long closerId, String closerName, String closeRemark) {
        RepairOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("抢修工单不存在");
        }
        if ("CLOSED".equals(order.getStatus())) {
            throw new RuntimeException("工单已关闭");
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"FINISHED".equals(order.getStatus())) {
            throw new RuntimeException("只有已完成或已确认的工单可以关闭");
        }

        order.setStatus("CLOSED");
        boolean result = this.updateById(order);

        flowLogService.addLog("REPAIR", orderId, order.getOrderNo(),
                "CLOSE", "关闭工单",
                closerId, closerName, closeRemark);

        return result;
    }

    public List<RepairOrder> listByRepairTeam(Long repairTeamId, String status) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairOrder::getRepairTeamId, repairTeamId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        return this.list(wrapper);
    }
}
