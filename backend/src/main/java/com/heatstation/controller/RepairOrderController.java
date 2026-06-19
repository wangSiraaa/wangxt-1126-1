package com.heatstation.controller;

import com.heatstation.common.PageResult;
import com.heatstation.common.Result;
import com.heatstation.entity.RepairOrder;
import com.heatstation.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/repair/order")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @GetMapping("/page")
    public Result<PageResult<RepairOrder>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                @RequestParam(defaultValue = "10") Long pageSize,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String faultLevel,
                                                @RequestParam(required = false) Long stationId,
                                                @RequestParam(required = false) Long repairTeamId,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        PageResult<RepairOrder> result = repairOrderService.queryPage(pageNum, pageSize, status, faultLevel, stationId, repairTeamId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<RepairOrder> getById(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getById(id);
        return Result.success(order);
    }

    @PostMapping("/create")
    public Result<RepairOrder> create(@RequestBody RepairOrder order) {
        order.setOrderNo(repairOrderService.generateOrderNo());
        order.setStatus("CREATED");
        repairOrderService.save(order);
        return Result.success(order);
    }

    @PostMapping("/accept")
    public Result<?> accept(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long repairTeamId = params.get("repairTeamId") != null ? Long.valueOf(params.get("repairTeamId").toString()) : null;
        String repairTeamName = (String) params.get("repairTeamName");
        String repairPerson = (String) params.get("repairPerson");
        String repairPhone = (String) params.get("repairPhone");

        repairOrderService.acceptOrder(orderId, repairTeamId, repairTeamName, repairPerson, repairPhone);
        return Result.success("接单成功");
    }

    @PostMapping("/arrive")
    public Result<?> arrive(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());

        repairOrderService.arriveSite(orderId);
        return Result.success("已到达现场，开始抢修");
    }

    @PostMapping("/finish")
    public Result<?> finish(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());

        BigDecimal pressureAfter = params.get("pressureAfter") != null ? new BigDecimal(params.get("pressureAfter").toString()) : null;
        BigDecimal supplyTempAfter = params.get("supplyTempAfter") != null ? new BigDecimal(params.get("supplyTempAfter").toString()) : null;
        BigDecimal returnTempAfter = params.get("returnTempAfter") != null ? new BigDecimal(params.get("returnTempAfter").toString()) : null;

        String repairContent = (String) params.get("repairContent");
        String repairMaterial = (String) params.get("repairMaterial");
        BigDecimal workHours = params.get("workHours") != null ? new BigDecimal(params.get("workHours").toString()) : null;

        String valveOperation = (String) params.get("valveOperation");
        String tempHeatPlan = (String) params.get("tempHeatPlan");
        Date estRestoreTime = null;
        if (params.get("estRestoreTime") != null && !params.get("estRestoreTime").toString().isEmpty()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                estRestoreTime = sdf.parse(params.get("estRestoreTime").toString());
            } catch (Exception ignored) {
            }
        }

        repairOrderService.finishRepair(orderId, pressureAfter, supplyTempAfter, returnTempAfter,
                repairContent, repairMaterial, workHours,
                valveOperation, tempHeatPlan, estRestoreTime);
        return Result.success("抢修完成");
    }

    @PostMapping("/confirm")
    public Result<?> confirm(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long confirmerId = params.get("confirmerId") != null ? Long.valueOf(params.get("confirmerId").toString()) : null;
        String confirmerName = (String) params.get("confirmerName");

        repairOrderService.confirmRepair(orderId, confirmerId, confirmerName);
        return Result.success("确认成功");
    }

    @PostMapping("/close")
    public Result<?> close(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long closerId = params.get("closerId") != null ? Long.valueOf(params.get("closerId").toString()) : null;
        String closerName = (String) params.get("closerName");
        String closeRemark = (String) params.get("closeRemark");

        repairOrderService.closeOrder(orderId, closerId, closerName, closeRemark);
        return Result.success("关闭成功");
    }

    @GetMapping("/myOrders")
    public Result<List<RepairOrder>> myOrders(@RequestParam Long repairTeamId,
                                               @RequestParam(required = false) String status) {
        List<RepairOrder> orders = repairOrderService.listByRepairTeam(repairTeamId, status);
        return Result.success(orders);
    }

    @PostMapping("/dispatch")
    public Result<?> dispatch(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Long dispatcherId = params.get("dispatcherId") != null ? Long.valueOf(params.get("dispatcherId").toString()) : null;
        String dispatcherName = (String) params.get("dispatcherName");
        Long repairTeamId = params.get("repairTeamId") != null ? Long.valueOf(params.get("repairTeamId").toString()) : null;
        String repairTeamName = (String) params.get("repairTeamName");

        RepairOrder order = repairOrderService.getById(orderId);
        if (order == null) {
            return Result.fail("工单不存在");
        }
        order.setDispatcherId(dispatcherId);
        order.setDispatcherName(dispatcherName);
        order.setRepairTeamId(repairTeamId);
        order.setRepairTeamName(repairTeamName);
        order.setDispatchTime(new Date());
        order.setStatus("DISPATCHED");
        repairOrderService.updateById(order);

        return Result.success("派单成功");
    }
}
