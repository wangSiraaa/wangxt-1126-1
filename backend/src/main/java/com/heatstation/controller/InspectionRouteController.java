package com.heatstation.controller;

import com.heatstation.common.PageResult;
import com.heatstation.common.Result;
import com.heatstation.entity.InspectionRoute;
import com.heatstation.entity.InspectionRouteStation;
import com.heatstation.entity.Station;
import com.heatstation.service.InspectionRouteService;
import com.heatstation.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inspection/route")
public class InspectionRouteController {

    @Autowired
    private InspectionRouteService routeService;

    @Autowired
    private StationService stationService;

    @GetMapping("/page")
    public Result<PageResult<InspectionRoute>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                    @RequestParam(defaultValue = "10") Long pageSize,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Long inspectorId,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date planDateStart,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date planDateEnd) {
        PageResult<InspectionRoute> result = routeService.queryPage(pageNum, pageSize, status, inspectorId, planDateStart, planDateEnd);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<InspectionRoute> getById(@PathVariable Long id) {
        InspectionRoute route = routeService.getById(id);
        return Result.success(route);
    }

    @GetMapping("/{id}/stations")
    public Result<List<InspectionRouteStation>> getRouteStations(@PathVariable Long id) {
        List<InspectionRouteStation> stations = routeService.getRouteStations(id);
        return Result.success(stations);
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        InspectionRoute route = routeService.getById(id);
        if (route == null) {
            return Result.fail("路线不存在");
        }
        List<InspectionRouteStation> stations = routeService.getRouteStations(id);
        List<Long> stationIds = stations.stream().map(InspectionRouteStation::getStationId).collect(Collectors.toList());
        Map<Long, Station> stationMap = new HashMap<>();
        if (!stationIds.isEmpty()) {
            List<Station> stationList = stationService.listByIds(stationIds);
            for (Station s : stationList) {
                stationMap.put(s.getId(), s);
            }
        }
        List<Map<String, Object>> stationList = new java.util.ArrayList<>();
        for (InspectionRouteStation rs : stations) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", rs.getStationId());
            item.put("routeStationId", rs.getId());
            item.put("stationId", rs.getStationId());
            item.put("stationCode", rs.getStationCode());
            item.put("stationName", rs.getStationName());
            item.put("sortOrder", rs.getSortOrder());
            item.put("status", rs.getInspectionStatus());
            item.put("inspectionStatus", rs.getInspectionStatus());
            item.put("inspectionTime", rs.getInspectionTime());
            item.put("inspectorId", rs.getInspectorId());
            item.put("inspectorName", rs.getInspectorName());
            item.put("pressure", rs.getPressure());
            item.put("supplyTemp", rs.getSupplyTemp());
            item.put("returnTemp", rs.getReturnTemp());
            item.put("hasException", rs.getHasException());
            item.put("remark", rs.getRemark());
            Station station = stationMap.get(rs.getStationId());
            if (station != null) {
                item.put("stationType", station.getStationType());
                item.put("stationAddress", station.getStationAddress());
                item.put("pressureLimitMin", station.getPressureLimitMin());
                item.put("pressureLimitMax", station.getPressureLimitMax());
                item.put("tempLimitMin", station.getTempLimitMin());
                item.put("tempLimitMax", station.getTempLimitMax());
                item.put("dutyPerson", station.getDutyPerson());
                item.put("dutyPhone", station.getDutyPhone());
            }
            stationList.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("route", route);
        result.put("stations", stationList);
        return Result.success(result);
    }

    @PostMapping("/create")
    public Result<InspectionRoute> create(@RequestBody Map<String, Object> params) {
        InspectionRoute route = new InspectionRoute();
        route.setRouteName((String) params.get("routeName"));
        route.setRouteDesc((String) params.get("routeDesc"));
        route.setInspectorId(params.get("inspectorId") != null ? Long.valueOf(params.get("inspectorId").toString()) : null);
        route.setInspectorName((String) params.get("inspectorName"));
        route.setDispatcherId(params.get("dispatcherId") != null ? Long.valueOf(params.get("dispatcherId").toString()) : null);
        route.setDispatcherName((String) params.get("dispatcherName"));
        route.setRemark((String) params.get("remark"));

        if (params.get("planDate") != null) {
            route.setPlanDate(java.sql.Date.valueOf((String) params.get("planDate")));
        }

        @SuppressWarnings("unchecked")
        List<Long> stationIds = (List<Long>) params.get("stationIds");

        InspectionRoute result = routeService.createRoute(route, stationIds);
        return Result.success(result);
    }

    @PostMapping("/assign")
    public Result<?> assign(@RequestBody Map<String, Object> params) {
        Long routeId = Long.valueOf(params.get("routeId").toString());
        Long dispatcherId = params.get("dispatcherId") != null ? Long.valueOf(params.get("dispatcherId").toString()) : null;
        String dispatcherName = (String) params.get("dispatcherName");

        routeService.assignRoute(routeId, dispatcherId, dispatcherName);
        return Result.success("下发成功");
    }

    @PostMapping("/start")
    public Result<?> start(@RequestBody Map<String, Object> params) {
        Long routeId = Long.valueOf(params.get("routeId").toString());
        Long inspectorId = Long.valueOf(params.get("inspectorId").toString());

        routeService.startRoute(routeId, inspectorId);
        return Result.success("开始巡检");
    }

    @PostMapping("/complete")
    public Result<?> complete(@RequestBody Map<String, Object> params) {
        Long routeId = Long.valueOf(params.get("routeId").toString());

        routeService.completeRoute(routeId);
        return Result.success("完成巡检路线");
    }

    @GetMapping("/myRoutes")
    public Result<List<InspectionRoute>> myRoutes(@RequestParam Long inspectorId,
                                                  @RequestParam(required = false) String status) {
        List<InspectionRoute> routes = routeService.lambdaQuery()
                .eq(InspectionRoute::getInspectorId, inspectorId)
                .eq(status != null, InspectionRoute::getStatus, status)
                .orderByDesc(InspectionRoute::getCreateTime)
                .list();
        return Result.success(routes);
    }
}
