package com.heatstation.controller;

import com.heatstation.common.PageResult;
import com.heatstation.common.Result;
import com.heatstation.entity.ColdComplaint;
import com.heatstation.entity.RepairOrder;
import com.heatstation.service.ColdComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/cold/complaint")
public class ColdComplaintController {

    @Autowired
    private ColdComplaintService complaintService;

    @GetMapping("/page")
    public Result<PageResult<ColdComplaint>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                   @RequestParam(defaultValue = "10") Long pageSize,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long stationId,
                                                   @RequestParam(required = false) String residentPhone,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        PageResult<ColdComplaint> result = complaintService.queryPage(pageNum, pageSize, status, stationId, residentPhone, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ColdComplaint> getById(@PathVariable Long id) {
        ColdComplaint complaint = complaintService.getById(id);
        return Result.success(complaint);
    }

    @PostMapping("/create")
    public Result<ColdComplaint> create(@RequestBody ColdComplaint complaint) {
        ColdComplaint result = complaintService.createComplaint(complaint);
        return Result.success(result);
    }

    @PostMapping("/process")
    public Result<?> process(@RequestBody Map<String, Object> params) {
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        Long handlerId = params.get("handlerId") != null ? Long.valueOf(params.get("handlerId").toString()) : null;
        String handlerName = (String) params.get("handlerName");
        String processDesc = (String) params.get("processDesc");

        complaintService.processComplaint(complaintId, handlerId, handlerName, processDesc);
        return Result.success("开始处理");
    }

    @PostMapping("/dispatchRepair")
    public Result<RepairOrder> dispatchRepair(@RequestBody Map<String, Object> params) {
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        Long dispatcherId = params.get("dispatcherId") != null ? Long.valueOf(params.get("dispatcherId").toString()) : null;
        String dispatcherName = (String) params.get("dispatcherName");
        Long repairTeamId = params.get("repairTeamId") != null ? Long.valueOf(params.get("repairTeamId").toString()) : null;
        String repairTeamName = (String) params.get("repairTeamName");
        String faultType = (String) params.get("faultType");
        String faultLevel = (String) params.get("faultLevel");
        String faultDesc = (String) params.get("faultDesc");

        RepairOrder order = complaintService.dispatchRepair(complaintId, dispatcherId, dispatcherName,
                repairTeamId, repairTeamName, faultType, faultLevel, faultDesc);
        return Result.success("派单成功", order);
    }

    @PostMapping("/finishRepair")
    public Result<?> finishRepair(@RequestBody Map<String, Object> params) {
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        complaintService.finishRepair(complaintId);
        return Result.success("修复完成");
    }

    @PostMapping("/visit")
    public Result<?> visit(@RequestBody Map<String, Object> params) {
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        String visitorName = (String) params.get("visitorName");
        String visitResult = (String) params.get("visitResult");
        Integer satisfaction = params.get("satisfaction") != null ? Integer.valueOf(params.get("satisfaction").toString()) : null;

        complaintService.visitComplaint(complaintId, visitorName, visitResult, satisfaction);
        return Result.success("回访完成");
    }

    @PostMapping("/close")
    public Result<?> close(@RequestBody Map<String, Object> params) {
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        Long closerId = params.get("closerId") != null ? Long.valueOf(params.get("closerId").toString()) : null;
        String closerName = (String) params.get("closerName");
        String closeRemark = (String) params.get("closeRemark");

        complaintService.closeComplaint(complaintId, closerId, closerName, closeRemark);
        return Result.success("关闭成功");
    }

    @PostMapping("/confirmMetricStable")
    public Result<?> confirmMetricStable(@RequestBody Map<String, Object> params) {
        Long complaintId = Long.valueOf(params.get("complaintId").toString());
        Long operatorId = params.get("operatorId") != null ? Long.valueOf(params.get("operatorId").toString()) : null;
        String operatorName = (String) params.get("operatorName");

        complaintService.confirmMetricStable(complaintId, operatorId, operatorName);
        return Result.success("站点指标已确认回稳");
    }
}
