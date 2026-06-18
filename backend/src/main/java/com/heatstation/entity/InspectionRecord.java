package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_inspection_record")
public class InspectionRecord extends BaseEntity {

    private String recordNo;

    private Long routeId;

    private Long routeStationId;

    private Long stationId;

    private String stationCode;

    private String stationName;

    private Long inspectorId;

    private String inspectorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date inspectionTime;

    private BigDecimal pressure;

    private BigDecimal supplyTemp;

    private BigDecimal returnTemp;

    private String pressureStatus;

    private String tempStatus;

    private Integer hasException;

    private String exceptionLevel;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
}
