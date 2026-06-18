package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.common.PageResult;
import com.heatstation.entity.Station;
import com.heatstation.mapper.StationMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StationService extends ServiceImpl<StationMapper, Station> {

    public PageResult<Station> queryPage(Long pageNum, Long pageSize, String keyword, String stationType) {
        Page<Station> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Station::getStationCode, keyword)
                    .or().like(Station::getStationName, keyword));
        }
        if (StringUtils.hasText(stationType)) {
            wrapper.eq(Station::getStationType, stationType);
        }
        wrapper.eq(Station::getStatus, 1);
        wrapper.orderByAsc(Station::getSortOrder);
        Page<Station> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }
}
