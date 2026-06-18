package com.heatstation.controller;

import com.heatstation.common.PageResult;
import com.heatstation.common.Result;
import com.heatstation.entity.Station;
import com.heatstation.entity.StationMetricHistory;
import com.heatstation.service.StationMetricHistoryService;
import com.heatstation.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Autowired
    private StationService stationService;

    @Autowired
    private StationMetricHistoryService metricHistoryService;

    @GetMapping("/page")
    public Result<PageResult<Station>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                            @RequestParam(defaultValue = "10") Long pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String stationType) {
        PageResult<Station> result = stationService.queryPage(pageNum, pageSize, keyword, stationType);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<Station>> list(@RequestParam(required = false) String stationType) {
        List<Station> list = stationService.lambdaQuery()
                .eq(stationType != null, Station::getStationType, stationType)
                .eq(Station::getStatus, 1)
                .orderByAsc(Station::getSortOrder)
                .list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Station> getById(@PathVariable Long id) {
        Station station = stationService.getById(id);
        return Result.success(station);
    }

    @PostMapping("/save")
    public Result<Station> save(@RequestBody Station station) {
        stationService.saveOrUpdate(station);
        return Result.success(station);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        stationService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}/metricHistory")
    public Result<List<StationMetricHistory>> getMetricHistory(@PathVariable Long id,
                                                               @RequestParam(required = false) String metricType,
                                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        List<StationMetricHistory> list = metricHistoryService.queryByStationAndType(id, metricType, startTime, endTime);
        return Result.success(list);
    }
}
