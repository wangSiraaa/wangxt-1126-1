package com.heatstation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.entity.OrderFlowLog;
import com.heatstation.mapper.OrderFlowLogMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderFlowLogService extends ServiceImpl<OrderFlowLogMapper, OrderFlowLog> {

    public void addLog(String orderType, Long orderId, String orderNo,
                       String actionType, String actionDesc,
                       Long operatorId, String operatorName, String remark) {
        OrderFlowLog log = new OrderFlowLog();
        log.setOrderType(orderType);
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setActionType(actionType);
        log.setActionDesc(actionDesc);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperateTime(new Date());
        log.setRemark(remark);
        this.save(log);
    }
}
