package com.heatstation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heatstation.entity.Station;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StationMapper extends BaseMapper<Station> {
}
