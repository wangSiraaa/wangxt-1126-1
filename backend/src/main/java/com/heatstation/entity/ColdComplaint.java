package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_cold_complaint")
public class ColdComplaint extends BaseEntity {

    private String complaintNo;

    private Long stationId;

    private String stationCode;

    private String stationName;

    private String residentName;

    private String residentPhone;

    private String residentAddress;

    private String community;

    private String buildingNo;

    private String roomNo;

    private String complaintType;

    private String complaintDesc;

    private BigDecimal indoorTemp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date complaintTime;

    private String reporterName;

    private String status;

    private Long handlerId;

    private String handlerName;

    private Long repairOrderId;

    private String processDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date visitTime;

    private String visitorName;

    private String visitResult;

    private Integer satisfaction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date closeTime;

    private Long closerId;

    private String closerName;

    private String closeRemark;

    private String remark;
}
