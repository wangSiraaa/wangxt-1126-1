package com.heatstation.controller;

import com.heatstation.common.PageResult;
import com.heatstation.common.Result;
import com.heatstation.entity.InspectionException;
import com.heatstation.entity.RepairOrder;
import com.heatstation.service.InspectionExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/inspection/exception")
public class InspectionExceptionController {

    @Autowired
    private InspectionExceptionService exceptionService;

    @GetMapping("/page")
    public Result<PageResult<InspectionException>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                        @RequestParam(defaultValue = "10") Long pageSize,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String exceptionLevel,
                                                        @RequestParam(required = false) Long stationId,
                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        PageResult<InspectionException> result = exceptionService.queryPage(pageNum, pageSize, status, exceptionLevel, stationId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<InspectionException> getById(@PathVariable Long id) {
        InspectionException exception = exceptionService.getById(id);
        return Result.success(exception);
    }

    @PostMapping("/confirm")
    public Result<?> confirm(@RequestBody Map<String, Object> params) {
        Long exceptionId = Long.valueOf(params.get("exceptionId").toString());
        Long handlerId = params.get("handlerId") != null ? Long.valueOf(params.get("handlerId").toString()) : null;
        String handlerName = (String) params.get("handlerName");

        exceptionService.confirmException(exceptionId, handlerId, handlerName);
        return Result.success("确认成功");
    }

    @PostMapping("/dispatchRepair")
    public Result<RepairOrder> dispatchRepair(@RequestBody Map<String, Object> params) {
        Long exceptionId = Long.valueOf(params.get("exceptionId").toString());
        Long dispatcherId = params.get("dispatcherId") != null ? Long.valueOf(params.get("dispatcherId").toString()) : null;
        String dispatcherName = (String) params.get("dispatcherName");
        Long repairTeamId = params.get("repairTeamId") != null ? Long.valueOf(params.get("repairTeamId").toString()) : null;
        String repairTeamName = (String) params.get("repairTeamName");

        RepairOrder order = exceptionService.dispatchRepair(exceptionId, dispatcherId, dispatcherName, repairTeamId, repairTeamName);
        return Result.success("派单成功", order);
    }

    @PostMapping("/autoUpgrade")
    public Result<RepairOrder> autoUpgrade(@RequestBody Map<String, Object> params) {
        Long exceptionId = Long.valueOf(params.get("exceptionId").toString());
        Long dispatcherId = params.get("dispatcherId") != null ? Long.valueOf(params.get("dispatcherId").toString()) : null;
        String reason = (String) params.get("reason");

        RepairOrder order = exceptionService.autoUpgradeToRepair(exceptionId, dispatcherId, reason);
        return Result.success("自动升级成功", order);
    }

    @PostMapping("/close")
    public Result<?> close(@RequestBody Map<String, Object> params) {
        Long exceptionId = Long.valueOf(params.get("exceptionId").toString());
        Long closerId = params.get("closerId") != null ? Long.valueOf(params.get("closerId").toString()) : null;
        String closerName = (String) params.get("closerName");
        String closeRemark = (String) params.get("closeRemark");

        exceptionService.closeException(exceptionId, closerId, closerName, closeRemark);
        return Result.success("关闭成功");
    }

    @GetMapping("/listByRoute")
    public Result<?> listByRoute(@RequestParam Long routeId) {
        return Result.success(exceptionService.lambdaQuery()
                .eq(InspectionException::getRouteId, routeId)
                .orderByDesc(InspectionException::getReportTime)
                .list());
    }

    @GetMapping("/myExceptions")
    public Result<?> myExceptions(@RequestParam Long inspectorId,
                                   @RequestParam(required = false) String status) {
        return Result.success(exceptionService.lambdaQuery()
                .eq(InspectionException::getReporterId, inspectorId)
                .eq(status != null && !status.isEmpty(), InspectionException::getStatus, status)
                .orderByDesc(InspectionException::getReportTime)
                .list());
    }
}
