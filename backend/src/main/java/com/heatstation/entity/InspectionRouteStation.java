package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_inspection_route_station")
public class InspectionRouteStation extends BaseEntity {

    private Long routeId;

    private Long stationId;

    private String stationCode;

    private String stationName;

    private Integer sortOrder;

    private String inspectionStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date inspectionTime;

    private Long inspectorId;

    private String inspectorName;

    private BigDecimal pressure;

    private BigDecimal supplyTemp;

    private BigDecimal returnTemp;

    private Integer hasException;

    private String remark;
}
