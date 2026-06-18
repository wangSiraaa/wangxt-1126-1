package com.heatstation.controller;

import com.heatstation.common.PageResult;
import com.heatstation.common.Result;
import com.heatstation.entity.InspectionRecord;
import com.heatstation.service.InspectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/inspection/record")
public class InspectionRecordController {

    @Autowired
    private InspectionRecordService recordService;

    @GetMapping("/page")
    public Result<PageResult<InspectionRecord>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                     @RequestParam(defaultValue = "10") Long pageSize,
                                                     @RequestParam(required = false) Long stationId,
                                                     @RequestParam(required = false) Long inspectorId,
                                                     @RequestParam(required = false) Long routeId,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        PageResult<InspectionRecord> result = recordService.queryPage(pageNum, pageSize, stationId, inspectorId, routeId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<InspectionRecord> getById(@PathVariable Long id) {
        InspectionRecord record = recordService.getById(id);
        return Result.success(record);
    }

    @PostMapping("/submit")
    public Result<InspectionRecord> submit(@RequestBody Map<String, Object> params) {
        InspectionRecord record = new InspectionRecord();
        record.setStationId(Long.valueOf(params.get("stationId").toString()));
        record.setInspectorId(params.get("inspectorId") != null ? Long.valueOf(params.get("inspectorId").toString()) : null);
        record.setInspectorName((String) params.get("inspectorName"));
        record.setRouteId(params.get("routeId") != null ? Long.valueOf(params.get("routeId").toString()) : null);
        record.setRouteStationId(params.get("routeStationId") != null ? Long.valueOf(params.get("routeStationId").toString()) : null);

        if (params.get("pressure") != null) {
            record.setPressure(new BigDecimal(params.get("pressure").toString()));
        }
        if (params.get("supplyTemp") != null) {
            record.setSupplyTemp(new BigDecimal(params.get("supplyTemp").toString()));
        }
        if (params.get("returnTemp") != null) {
            record.setReturnTemp(new BigDecimal(params.get("returnTemp").toString()));
        }

        record.setRemark((String) params.get("remark"));

        Long routeStationId = params.get("routeStationId") != null ? Long.valueOf(params.get("routeStationId").toString()) : null;

        InspectionRecord result = recordService.submitInspection(record, routeStationId);
        return Result.success("巡检记录提交成功", result);
    }

    @GetMapping("/listByRoute")
    public Result<?> listByRoute(@RequestParam Long routeId) {
        return Result.success(recordService.lambdaQuery()
                .eq(InspectionRecord::getRouteId, routeId)
                .orderByDesc(InspectionRecord::getInspectionTime)
                .list());
    }
}
