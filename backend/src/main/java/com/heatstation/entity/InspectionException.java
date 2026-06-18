package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_inspection_exception")
public class InspectionException extends BaseEntity {

    private String exceptionNo;

    private Long routeId;

    private Long recordId;

    private Long stationId;

    private String stationCode;

    private String stationName;

    private String exceptionType;

    private String exceptionLevel;

    private String exceptionDesc;

    private BigDecimal pressureVal;

    private BigDecimal supplyTempVal;

    private BigDecimal returnTempVal;

    private BigDecimal limitMin;

    private BigDecimal limitMax;

    private Long reporterId;

    private String reporterName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reportTime;

    private String status;

    private Integer autoUpgrade;

    private String upgradeReason;

    private Long repairOrderId;

    private Long handlerId;

    private String handlerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handleTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date closeTime;

    private Long closerId;

    private String closerName;

    private String remark;
}
