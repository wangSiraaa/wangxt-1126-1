package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.entity.StationMetricHistory;
import com.heatstation.mapper.StationMetricHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class StationMetricHistoryService extends ServiceImpl<StationMetricHistoryMapper, StationMetricHistory> {

    public List<StationMetricHistory> queryByStationAndType(Long stationId, String metricType,
                                                            Date startTime, Date endTime) {
        LambdaQueryWrapper<StationMetricHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StationMetricHistory::getStationId, stationId);
        if (metricType != null) {
            wrapper.eq(StationMetricHistory::getMetricType, metricType);
        }
        if (startTime != null) {
            wrapper.ge(StationMetricHistory::getRecordTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(StationMetricHistory::getRecordTime, endTime);
        }
        wrapper.orderByAsc(StationMetricHistory::getRecordTime);
        return this.list(wrapper);
    }
}
