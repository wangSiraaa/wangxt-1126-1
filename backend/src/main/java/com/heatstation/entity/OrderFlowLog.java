package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_order_flow_log")
public class OrderFlowLog extends BaseEntity {

    private String orderType;

    private Long orderId;

    private String orderNo;

    private String actionType;

    private String actionDesc;

    private Long operatorId;

    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;

    private String remark;
}
