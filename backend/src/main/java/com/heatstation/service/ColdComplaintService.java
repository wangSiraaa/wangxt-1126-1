package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.common.PageResult;
import com.heatstation.entity.ColdComplaint;
import com.heatstation.entity.RepairOrder;
import com.heatstation.mapper.ColdComplaintMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class ColdComplaintService extends ServiceImpl<ColdComplaintMapper, ColdComplaint> {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private OrderFlowLogService flowLogService;

    public PageResult<ColdComplaint> queryPage(Long pageNum, Long pageSize, String status,
                                                Long stationId, String residentPhone,
                                                Date startTime, Date endTime) {
        Page<ColdComplaint> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ColdComplaint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(ColdComplaint::getStatus, status);
        }
        if (stationId != null) {
            wrapper.eq(ColdComplaint::getStationId, stationId);
        }
        if (StringUtils.hasText(residentPhone)) {
            wrapper.like(ColdComplaint::getResidentPhone, residentPhone);
        }
        if (startTime != null) {
            wrapper.ge(ColdComplaint::getComplaintTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(ColdComplaint::getComplaintTime, endTime);
        }
        wrapper.orderByDesc(ColdComplaint::getComplaintTime);
        Page<ColdComplaint> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public ColdComplaint createComplaint(ColdComplaint complaint) {
        complaint.setComplaintNo(generateComplaintNo());
        complaint.setStatus("CREATED");
        complaint.setComplaintTime(new Date());
        this.save(complaint);

        flowLogService.addLog("COMPLAINT", complaint.getId(), complaint.getComplaintNo(),
                "CREATE", "创建报冷事件",
                null, complaint.getReporterName(),
                complaint.getComplaintType() + ": " + complaint.getComplaintDesc());

        return complaint;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean processComplaint(Long complaintId, Long handlerId, String handlerName, String processDesc) {
        ColdComplaint complaint = this.getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("报冷事件不存在");
        }
        if (!"CREATED".equals(complaint.getStatus())) {
            throw new RuntimeException("只有新建状态的事件可以处理");
        }

        complaint.setStatus("PROCESSING");
        complaint.setHandlerId(handlerId);
        complaint.setHandlerName(handlerName);
        complaint.setProcessDesc(processDesc);
        boolean result = this.updateById(complaint);

        flowLogService.addLog("COMPLAINT", complaintId, complaint.getComplaintNo(),
                "START", "开始处理",
                handlerId, handlerName, processDesc);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public RepairOrder dispatchRepair(Long complaintId, Long dispatcherId, String dispatcherName,
                                       Long repairTeamId, String repairTeamName,
                                       String faultType, String faultLevel, String faultDesc) {
        ColdComplaint complaint = this.getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("报冷事件不存在");
        }
        if (!"PROCESSING".equals(complaint.getStatus()) && !"CREATED".equals(complaint.getStatus())) {
            throw new RuntimeException("当前状态不能派单抢修");
        }

        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setOrderNo(repairOrderService.generateOrderNo());
        repairOrder.setSourceType("COMPLAINT");
        repairOrder.setSourceId(complaintId);
        repairOrder.setStationId(complaint.getStationId());
        repairOrder.setStationCode(complaint.getStationCode());
        repairOrder.setStationName(complaint.getStationName());
        repairOrder.setFaultType(faultType != null ? faultType : "TEMPERATURE");
        repairOrder.setFaultLevel(faultLevel != null ? faultLevel : "WARNING");
        repairOrder.setFaultDesc(faultDesc != null ? faultDesc : complaint.getComplaintDesc());
        repairOrder.setDispatcherId(dispatcherId);
        repairOrder.setDispatcherName(dispatcherName);
        repairOrder.setDispatchTime(new Date());
        repairOrder.setRepairTeamId(repairTeamId);
        repairOrder.setRepairTeamName(repairTeamName);
        repairOrder.setStatus("DISPATCHED");

        repairOrderService.save(repairOrder);

        complaint.setStatus("REPAIRING");
        complaint.setRepairOrderId(repairOrder.getId());
        this.updateById(complaint);

        flowLogService.addLog("COMPLAINT", complaintId, complaint.getComplaintNo(),
                "DISPATCH", "派单抢修，工单号：" + repairOrder.getOrderNo(),
                dispatcherId, dispatcherName, null);

        flowLogService.addLog("REPAIR", repairOrder.getId(), repairOrder.getOrderNo(),
                "CREATE", "由报冷事件" + complaint.getComplaintNo() + "创建",
                dispatcherId, dispatcherName, null);

        return repairOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean finishRepair(Long complaintId) {
        ColdComplaint complaint = this.getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("报冷事件不存在");
        }
        if (!"REPAIRING".equals(complaint.getStatus())) {
            throw new RuntimeException("当前状态不能标记修复完成");
        }

        complaint.setStatus("REPAIRED");
        complaint.setFinishTime(new Date());
        boolean result = this.updateById(complaint);

        flowLogService.addLog("COMPLAINT", complaintId, complaint.getComplaintNo(),
                "FINISH", "修复完成",
                null, null, null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean visitComplaint(Long complaintId, String visitorName,
                                   String visitResult, Integer satisfaction) {
        ColdComplaint complaint = this.getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("报冷事件不存在");
        }
        if (!"REPAIRED".equals(complaint.getStatus())) {
            throw new RuntimeException("只有修复完成的事件可以回访");
        }

        complaint.setStatus("VISITED");
        complaint.setVisitTime(new Date());
        complaint.setVisitorName(visitorName);
        complaint.setVisitResult(visitResult);
        complaint.setSatisfaction(satisfaction);
        boolean result = this.updateById(complaint);

        flowLogService.addLog("COMPLAINT", complaintId, complaint.getComplaintNo(),
                "VISIT", "回访完成，满意度：" + satisfaction + "分",
                null, visitorName, visitResult);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean closeComplaint(Long complaintId, Long closerId, String closerName, String closeRemark) {
        ColdComplaint complaint = this.getById(complaintId);
        if (complaint == null) {
            throw new RuntimeException("报冷事件不存在");
        }
        if ("CLOSED".equals(complaint.getStatus())) {
            throw new RuntimeException("事件已关闭");
        }

        if (complaint.getRepairOrderId() != null) {
            RepairOrder repairOrder = repairOrderService.getById(complaint.getRepairOrderId());
            if (repairOrder != null) {
                String orderStatus = repairOrder.getStatus();
                if (!"FINISHED".equals(orderStatus) && !"CONFIRMED".equals(orderStatus)
                        && !"CLOSED".equals(orderStatus)) {
                    throw new RuntimeException("关联的抢修工单未完成，不能关闭报冷事件。当前工单状态：" + getStatusDesc(orderStatus));
                }
            }
        }

        if (!"VISITED".equals(complaint.getStatus()) && !"REPAIRED".equals(complaint.getStatus())
                && !"PROCESSING".equals(complaint.getStatus())) {
            throw new RuntimeException("当前状态不能关闭事件");
        }

        complaint.setStatus("CLOSED");
        complaint.setCloseTime(new Date());
        complaint.setCloserId(closerId);
        complaint.setCloserName(closerName);
        complaint.setCloseRemark(closeRemark);
        boolean result = this.updateById(complaint);

        flowLogService.addLog("COMPLAINT", complaintId, complaint.getComplaintNo(),
                "CLOSE", "关闭报冷事件",
                closerId, closerName, closeRemark);

        return result;
    }

    private String getStatusDesc(String status) {
        switch (status) {
            case "CREATED": return "已创建";
            case "DISPATCHED": return "已派单";
            case "ACCEPTED": return "已接单";
            case "IN_PROGRESS": return "抢修中";
            case "FINISHED": return "已完成";
            case "CONFIRMED": return "已确认";
            case "CLOSED": return "已关闭";
            default: return status;
        }
    }

    private String generateComplaintNo() {
        return "CL" + System.currentTimeMillis();
    }
}
