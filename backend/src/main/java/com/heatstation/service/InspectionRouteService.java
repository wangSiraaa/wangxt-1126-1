package com.heatstation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heatstation.common.PageResult;
import com.heatstation.entity.InspectionRoute;
import com.heatstation.entity.InspectionRouteStation;
import com.heatstation.entity.SysUser;
import com.heatstation.mapper.InspectionRouteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class InspectionRouteService extends ServiceImpl<InspectionRouteMapper, InspectionRoute> {

    @Autowired
    private InspectionRouteStationService routeStationService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private OrderFlowLogService flowLogService;

    public PageResult<InspectionRoute> queryPage(Long pageNum, Long pageSize, String status,
                                                  Long inspectorId, Date planDateStart, Date planDateEnd) {
        Page<InspectionRoute> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InspectionRoute> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(InspectionRoute::getStatus, status);
        }
        if (inspectorId != null) {
            wrapper.eq(InspectionRoute::getInspectorId, inspectorId);
        }
        if (planDateStart != null) {
            wrapper.ge(InspectionRoute::getPlanDate, planDateStart);
        }
        if (planDateEnd != null) {
            wrapper.le(InspectionRoute::getPlanDate, planDateEnd);
        }
        wrapper.orderByDesc(InspectionRoute::getCreateTime);
        Page<InspectionRoute> result = this.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public InspectionRoute createRoute(InspectionRoute route, List<Long> stationIds) {
        SysUser inspector = userService.getById(route.getInspectorId());
        if (inspector != null) {
            route.setInspectorName(inspector.getUserName());
        }

        String routeCode = generateRouteCode();
        route.setRouteCode(routeCode);
        route.setStatus("DRAFT");
        route.setTotalStations(stationIds != null ? stationIds.size() : 0);
        route.setCompletedStations(0);
        route.setExceptionCount(0);

        this.save(route);

        if (stationIds != null && !stationIds.isEmpty()) {
            routeStationService.saveRouteStations(route.getId(), stationIds);
        }

        flowLogService.addLog("ROUTE", route.getId(), routeCode,
                "CREATE", "创建巡检路线",
                route.getDispatcherId(), route.getDispatcherName(), null);

        return route;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoute(Long routeId, Long dispatcherId, String dispatcherName) {
        InspectionRoute route = this.getById(routeId);
        if (route == null) {
            throw new RuntimeException("巡检路线不存在");
        }
        if (!"DRAFT".equals(route.getStatus())) {
            throw new RuntimeException("只有草稿状态的路线可以下发");
        }

        route.setStatus("ASSIGNED");
        route.setDispatcherId(dispatcherId);
        route.setDispatcherName(dispatcherName);
        boolean result = this.updateById(route);

        flowLogService.addLog("ROUTE", routeId, route.getRouteCode(),
                "ASSIGN", "下发巡检路线",
                dispatcherId, dispatcherName, null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean startRoute(Long routeId, Long inspectorId) {
        InspectionRoute route = this.getById(routeId);
        if (route == null) {
            throw new RuntimeException("巡检路线不存在");
        }
        if (!"ASSIGNED".equals(route.getStatus())) {
            throw new RuntimeException("只有已下发的路线可以开始巡检");
        }

        route.setStatus("IN_PROGRESS");
        route.setActualStartTime(new Date());
        boolean result = this.updateById(route);

        flowLogService.addLog("ROUTE", routeId, route.getRouteCode(),
                "START", "开始巡检",
                inspectorId, route.getInspectorName(), null);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean completeRoute(Long routeId) {
        InspectionRoute route = this.getById(routeId);
        if (route == null) {
            throw new RuntimeException("巡检路线不存在");
        }

        List<InspectionRouteStation> stations = routeStationService.listByRouteId(routeId);
        long completedCount = stations.stream()
                .filter(s -> "COMPLETED".equals(s.getInspectionStatus()))
                .count();

        if (completedCount < stations.size()) {
            throw new RuntimeException("还有未完成的站点巡检，不能完成路线");
        }

        route.setStatus("COMPLETED");
        route.setActualEndTime(new Date());
        route.setCompletedStations((int) completedCount);
        boolean result = this.updateById(route);

        flowLogService.addLog("ROUTE", routeId, route.getRouteCode(),
                "COMPLETE", "完成巡检路线",
                route.getInspectorId(), route.getInspectorName(), null);

        return result;
    }

    public List<InspectionRouteStation> getRouteStations(Long routeId) {
        return routeStationService.listByRouteId(routeId);
    }

    private String generateRouteCode() {
        return "RT" + System.currentTimeMillis();
    }

    public void incrementExceptionCount(Long routeId) {
        InspectionRoute route = this.getById(routeId);
        if (route != null) {
            route.setExceptionCount(route.getExceptionCount() + 1);
            this.updateById(route);
        }
    }

    public void incrementCompletedStations(Long routeId) {
        InspectionRoute route = this.getById(routeId);
        if (route != null) {
            route.setCompletedStations(route.getCompletedStations() + 1);
            this.updateById(route);
        }
    }
}
