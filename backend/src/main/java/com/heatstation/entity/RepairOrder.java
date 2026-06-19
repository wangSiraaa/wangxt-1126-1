package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_repair_order")
public class RepairOrder extends BaseEntity {

    private String orderNo;

    private String sourceType;

    private Long sourceId;

    private Long stationId;

    private String stationCode;

    private String stationName;

    private String faultType;

    private String faultLevel;

    private String faultDesc;

    private Long dispatcherId;

    private String dispatcherName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dispatchTime;

    private Long repairTeamId;

    private String repairTeamName;

    private String repairPerson;

    private String repairPhone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date acceptTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;

    private String status;

    private BigDecimal pressureAfter;

    private BigDecimal supplyTempAfter;

    private BigDecimal returnTempAfter;

    private String repairContent;

    private String repairMaterial;

    private String valveOperation;

    private String tempHeatPlan;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date estRestoreTime;

    private BigDecimal workHours;

    private String remark;
}
