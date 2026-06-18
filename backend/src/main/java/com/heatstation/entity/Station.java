package com.heatstation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hs_station")
public class Station extends BaseEntity {

    private String stationCode;

    private String stationName;

    private String stationAddress;

    private String stationType;

    private BigDecimal pressureLimitMin;

    private BigDecimal pressureLimitMax;

    private BigDecimal tempLimitMin;

    private BigDecimal tempLimitMax;

    private String dutyPerson;

    private String dutyPhone;

    private Integer sortOrder;

    private Integer status;
}
