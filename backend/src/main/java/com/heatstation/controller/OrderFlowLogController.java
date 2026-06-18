package com.heatstation.controller;

import com.heatstation.common.Result;
import com.heatstation.entity.OrderFlowLog;
import com.heatstation.service.OrderFlowLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order/flowLog")
public class OrderFlowLogController {

    @Autowired
    private OrderFlowLogService flowLogService;

    @GetMapping("/list")
    public Result<List<OrderFlowLog>> list(@RequestParam String orderType,
                                            @RequestParam Long orderId) {
        List<OrderFlowLog> logs = flowLogService.lambdaQuery()
                .eq(OrderFlowLog::getOrderType, orderType)
                .eq(OrderFlowLog::getOrderId, orderId)
                .orderByAsc(OrderFlowLog::getOperateTime)
                .list();
        return Result.success(logs);
    }
}
