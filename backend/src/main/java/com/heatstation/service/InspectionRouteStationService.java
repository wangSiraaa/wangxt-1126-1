package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.entity.InspectionRouteStation;
import com.heatstation.entity.Station;
import com.heatstation.mapper.InspectionRouteStationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InspectionRouteStationService extends ServiceImpl<InspectionRouteStationMapper, InspectionRouteStation> {

    @Autowired
    private StationService stationService;

    public List<InspectionRouteStation> listByRouteId(Long routeId) {
        LambdaQueryWrapper<InspectionRouteStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionRouteStation::getRouteId, routeId);
        wrapper.orderByAsc(InspectionRouteStation::getSortOrder);
        return this.list(wrapper);
    }

    public void saveRouteStations(Long routeId, List<Long> stationIds) {
        List<Station> stations = stationService.listByIds(stationIds);

        List<InspectionRouteStation> routeStations = new ArrayList<>();
        for (int i = 0; i < stationIds.size(); i++) {
            Long stationId = stationIds.get(i);
            Station station = stations.stream()
                    .filter(s -> s.getId().equals(stationId))
                    .findFirst()
                    .orElse(null);

            if (station != null) {
                InspectionRouteStation rs = new InspectionRouteStation();
                rs.setRouteId(routeId);
                rs.setStationId(stationId);
                rs.setStationCode(station.getStationCode());
                rs.setStationName(station.getStationName());
                rs.setSortOrder(i + 1);
                rs.setInspectionStatus("PENDING");
                rs.setHasException(0);
                routeStations.add(rs);
            }
        }

        this.saveBatch(routeStations);
    }

    public InspectionRouteStation getByRouteAndStation(Long routeId, Long stationId) {
        LambdaQueryWrapper<InspectionRouteStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionRouteStation::getRouteId, routeId);
        wrapper.eq(InspectionRouteStation::getStationId, stationId);
        return this.getOne(wrapper);
    }
}
