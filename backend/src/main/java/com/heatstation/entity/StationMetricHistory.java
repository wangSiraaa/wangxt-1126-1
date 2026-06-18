package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_station_metric_history")
public class StationMetricHistory extends BaseEntity {

    private Long stationId;

    private String stationCode;

    private String stationName;

    private String metricType;

    private BigDecimal metricValue;

    private String metricStatus;

    private String sourceType;

    private Long recordId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordTime;
}
