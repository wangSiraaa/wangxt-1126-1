-- 换热站巡检抢修系统数据库设计
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS heat_station DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE heat_station;

-- ============================================
-- 用户表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_code VARCHAR(32) NOT NULL UNIQUE COMMENT '用户编码',
    user_name VARCHAR(64) NOT NULL COMMENT '用户姓名',
    password VARCHAR(128) NOT NULL DEFAULT '123456' COMMENT '密码',
    role_code VARCHAR(32) NOT NULL COMMENT '角色编码：DISPATCHER-调度员 INSPECTOR-巡检员 REPAIR-抢修队',
    phone VARCHAR(20) COMMENT '联系电话',
    org_id BIGINT COMMENT '所属组织ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 换热站表
-- ============================================
DROP TABLE IF EXISTS hs_station;
CREATE TABLE hs_station (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    station_code VARCHAR(32) NOT NULL UNIQUE COMMENT '站点编码',
    station_name VARCHAR(128) NOT NULL COMMENT '站点名称',
    station_address VARCHAR(256) COMMENT '站点地址',
    station_type VARCHAR(32) DEFAULT 'PRIMARY' COMMENT '站点类型：PRIMARY-一次网 SECONDARY-二次网',
    pressure_limit_min DECIMAL(10,4) COMMENT '压力下限(MPa)',
    pressure_limit_max DECIMAL(10,4) COMMENT '压力上限(MPa)',
    temp_limit_min DECIMAL(10,2) COMMENT '温度下限(℃)',
    temp_limit_max DECIMAL(10,2) COMMENT '温度上限(℃)',
    duty_person VARCHAR(64) COMMENT '责任人',
    duty_phone VARCHAR(20) COMMENT '责任电话',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    branch_line VARCHAR(64) COMMENT '所属支线编码，同支线站点编码相同表示在同一供水管线上',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_station_type (station_type),
    INDEX idx_status (status),
    INDEX idx_branch_line (branch_line)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='换热站表';

-- ============================================
-- 巡检路线表
-- ============================================
DROP TABLE IF EXISTS hs_inspection_route;
CREATE TABLE hs_inspection_route (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    route_code VARCHAR(32) NOT NULL UNIQUE COMMENT '路线编码',
    route_name VARCHAR(128) NOT NULL COMMENT '路线名称',
    route_desc VARCHAR(512) COMMENT '路线描述',
    inspector_id BIGINT NOT NULL COMMENT '巡检员ID',
    inspector_name VARCHAR(64) COMMENT '巡检员姓名',
    plan_date DATE NOT NULL COMMENT '计划巡检日期',
    plan_start_time DATETIME COMMENT '计划开始时间',
    plan_end_time DATETIME COMMENT '计划结束时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿 ASSIGNED-已下发 IN_PROGRESS-进行中 COMPLETED-已完成 CANCELLED-已取消',
    total_stations INT DEFAULT 0 COMMENT '站点总数',
    completed_stations INT DEFAULT 0 COMMENT '已巡检站点数',
    exception_count INT DEFAULT 0 COMMENT '异常数',
    dispatcher_id BIGINT COMMENT '调度员ID',
    dispatcher_name VARCHAR(64) COMMENT '调度员姓名',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_inspector_id (inspector_id),
    INDEX idx_status (status),
    INDEX idx_plan_date (plan_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检路线表';

-- ============================================
-- 巡检路线-站点关联表
-- ============================================
DROP TABLE IF EXISTS hs_inspection_route_station;
CREATE TABLE hs_inspection_route_station (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    route_id BIGINT NOT NULL COMMENT '路线ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    station_code VARCHAR(32) COMMENT '站点编码',
    station_name VARCHAR(128) COMMENT '站点名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '巡检顺序',
    inspection_status VARCHAR(32) DEFAULT 'PENDING' COMMENT '巡检状态：PENDING-待巡检 IN_PROGRESS-巡检中 COMPLETED-已完成 SKIPPED-已跳过',
    inspection_time DATETIME COMMENT '巡检时间',
    inspector_id BIGINT COMMENT '巡检员ID',
    inspector_name VARCHAR(64) COMMENT '巡检员姓名',
    pressure DECIMAL(10,4) COMMENT '现场压力读数(MPa)',
    supply_temp DECIMAL(10,2) COMMENT '供水温度(℃)',
    return_temp DECIMAL(10,2) COMMENT '回水温度(℃)',
    has_exception TINYINT DEFAULT 0 COMMENT '是否有异常：0-否 1-是',
    remark VARCHAR(512) COMMENT '巡检备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_route_station (route_id, station_id),
    INDEX idx_route_id (route_id),
    INDEX idx_station_id (station_id),
    INDEX idx_inspection_status (inspection_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检路线站点关联表';

-- ============================================
-- 巡检记录表（站点级巡检明细）
-- ============================================
DROP TABLE IF EXISTS hs_inspection_record;
CREATE TABLE hs_inspection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    record_no VARCHAR(32) NOT NULL UNIQUE COMMENT '记录编号',
    route_id BIGINT COMMENT '所属路线ID',
    route_station_id BIGINT COMMENT '路线站点ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    station_code VARCHAR(32) COMMENT '站点编码',
    station_name VARCHAR(128) COMMENT '站点名称',
    inspector_id BIGINT NOT NULL COMMENT '巡检员ID',
    inspector_name VARCHAR(64) COMMENT '巡检员姓名',
    inspection_time DATETIME NOT NULL COMMENT '巡检时间',
    pressure DECIMAL(10,4) NOT NULL COMMENT '压力读数(MPa)',
    supply_temp DECIMAL(10,2) NOT NULL COMMENT '供水温度(℃)',
    return_temp DECIMAL(10,2) NOT NULL COMMENT '回水温度(℃)',
    pressure_status VARCHAR(32) DEFAULT 'NORMAL' COMMENT '压力状态：NORMAL-正常 BELOW_LIMIT-低于下限 ABOVE_LIMIT-高于上限',
    temp_status VARCHAR(32) DEFAULT 'NORMAL' COMMENT '温度状态：NORMAL-正常 BELOW_LIMIT-低于下限 ABOVE_LIMIT-高于上限',
    has_exception TINYINT DEFAULT 0 COMMENT '是否有异常：0-否 1-是',
    exception_level VARCHAR(32) COMMENT '异常级别：NORMAL-一般 WARNING-警告 CRITICAL-严重',
    remark VARCHAR(1024) COMMENT '巡检说明',
    submit_time DATETIME COMMENT '提交时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_route_id (route_id),
    INDEX idx_station_id (station_id),
    INDEX idx_inspector_id (inspector_id),
    INDEX idx_inspection_time (inspection_time),
    INDEX idx_has_exception (has_exception)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检记录表';

-- ============================================
-- 巡检异常表
-- ============================================
DROP TABLE IF EXISTS hs_inspection_exception;
CREATE TABLE hs_inspection_exception (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    exception_no VARCHAR(32) NOT NULL UNIQUE COMMENT '异常编号',
    route_id BIGINT COMMENT '路线ID',
    record_id BIGINT COMMENT '巡检记录ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    station_code VARCHAR(32) COMMENT '站点编码',
    station_name VARCHAR(128) COMMENT '站点名称',
    exception_type VARCHAR(64) NOT NULL COMMENT '异常类型：PRESSURE_HIGH-压力过高 PRESSURE_LOW-压力过低 TEMP_HIGH-温度过高 TEMP_LOW-温度过低 DEVICE_FAULT-设备故障 OTHER-其他',
    exception_level VARCHAR(32) NOT NULL DEFAULT 'WARNING' COMMENT '异常级别：NORMAL-一般 WARNING-警告 CRITICAL-严重',
    exception_desc VARCHAR(1024) COMMENT '异常描述',
    pressure_val DECIMAL(10,4) COMMENT '异常压力值',
    supply_temp_val DECIMAL(10,2) COMMENT '异常供水温度',
    return_temp_val DECIMAL(10,2) COMMENT '异常回水温度',
    limit_min DECIMAL(10,4) COMMENT '下限值',
    limit_max DECIMAL(10,4) COMMENT '上限值',
    reporter_id BIGINT NOT NULL COMMENT '上报人ID',
    reporter_name VARCHAR(64) COMMENT '上报人姓名',
    report_time DATETIME NOT NULL COMMENT '上报时间',
    status VARCHAR(32) NOT NULL DEFAULT 'REPORTED' COMMENT '状态：REPORTED-已上报 CONFIRMED-已确认 DISPATCHED-已派单 IN_REPAIR-抢修中 REPAIRED-已修复 CLOSED-已关闭',
    auto_upgrade TINYINT DEFAULT 0 COMMENT '是否自动升级：0-否 1-是',
    upgrade_reason VARCHAR(512) COMMENT '升级原因',
    close_restriction VARCHAR(32) COMMENT '关闭限制：PRIMARY_OVERLIMIT-一次网越限禁止关闭 NULL-无限制',
    repair_order_id BIGINT COMMENT '关联抢修工单ID',
    handler_id BIGINT COMMENT '处理人ID',
    handler_name VARCHAR(64) COMMENT '处理人姓名',
    handle_time DATETIME COMMENT '处理时间',
    close_time DATETIME COMMENT '关闭时间',
    closer_id BIGINT COMMENT '关闭人ID',
    closer_name VARCHAR(64) COMMENT '关闭人姓名',
    remark VARCHAR(1024) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_route_id (route_id),
    INDEX idx_station_id (station_id),
    INDEX idx_status (status),
    INDEX idx_exception_level (exception_level),
    INDEX idx_report_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检异常表';

-- ============================================
-- 抢修工单表
-- ============================================
DROP TABLE IF EXISTS hs_repair_order;
CREATE TABLE hs_repair_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '工单编号',
    source_type VARCHAR(32) NOT NULL COMMENT '来源类型：INSPECTION-巡检异常 COMPLAINT-居民报冷 MANUAL-手动创建',
    source_id BIGINT COMMENT '来源记录ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    station_code VARCHAR(32) COMMENT '站点编码',
    station_name VARCHAR(128) COMMENT '站点名称',
    fault_type VARCHAR(64) NOT NULL COMMENT '故障类型：PRESSURE-压力异常 TEMPERATURE-温度异常 DEVICE-设备故障 PIPE-管网泄漏 OTHER-其他',
    fault_level VARCHAR(32) NOT NULL DEFAULT 'WARNING' COMMENT '故障级别：NORMAL-一般 WARNING-警告 CRITICAL-严重',
    fault_desc VARCHAR(1024) COMMENT '故障描述',
    dispatcher_id BIGINT COMMENT '派单人ID',
    dispatcher_name VARCHAR(64) COMMENT '派单人姓名',
    dispatch_time DATETIME COMMENT '派单时间',
    repair_team_id BIGINT COMMENT '抢修队ID',
    repair_team_name VARCHAR(64) COMMENT '抢修队名称',
    repair_person VARCHAR(128) COMMENT '抢修人员',
    repair_phone VARCHAR(20) COMMENT '抢修联系电话',
    accept_time DATETIME COMMENT '接单时间',
    arrive_time DATETIME COMMENT '到达时间',
    start_time DATETIME COMMENT '开始抢修时间',
    finish_time DATETIME COMMENT '抢修完成时间',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED-已创建 DISPATCHED-已派单 ACCEPTED-已接单 IN_PROGRESS-抢修中 FINISHED-已完成 CONFIRMED-已确认 CLOSED-已关闭',
    pressure_after DECIMAL(10,4) COMMENT '抢修后压力',
    supply_temp_after DECIMAL(10,2) COMMENT '抢修后供水温度',
    return_temp_after DECIMAL(10,2) COMMENT '抢修后回水温度',
    repair_content VARCHAR(2048) COMMENT '抢修内容',
    repair_material VARCHAR(1024) COMMENT '使用材料',
    valve_operation VARCHAR(2048) COMMENT '阀门操作记录：操作的阀门编号、开关状态、操作时间等',
    temp_heat_plan VARCHAR(2048) COMMENT '临时供热方案：应急供热措施描述',
    est_restore_time DATETIME COMMENT '预计恢复正常时间',
    work_hours DECIMAL(6,2) COMMENT '工时(小时)',
    remark VARCHAR(1024) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_station_id (station_id),
    INDEX idx_status (status),
    INDEX idx_repair_team_id (repair_team_id),
    INDEX idx_dispatch_time (dispatch_time),
    INDEX idx_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢修工单表';

-- ============================================
-- 居民报冷事件表
-- ============================================
DROP TABLE IF EXISTS hs_cold_complaint;
CREATE TABLE hs_cold_complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    complaint_no VARCHAR(32) NOT NULL UNIQUE COMMENT '报冷编号',
    station_id BIGINT COMMENT '所属换热站ID',
    station_code VARCHAR(32) COMMENT '站点编码',
    station_name VARCHAR(128) COMMENT '站点名称',
    resident_name VARCHAR(64) NOT NULL COMMENT '居民姓名',
    resident_phone VARCHAR(20) NOT NULL COMMENT '居民电话',
    resident_address VARCHAR(256) COMMENT '居民地址',
    community VARCHAR(128) COMMENT '所属小区',
    building_no VARCHAR(32) COMMENT '楼栋号',
    room_no VARCHAR(32) COMMENT '房间号',
    complaint_type VARCHAR(64) NOT NULL COMMENT '报冷类型：NOT_HOT-不暖 TEMPERATURE_LOW-温度低 LEAKAGE-漏水 NOISE-噪音 OTHER-其他',
    complaint_desc VARCHAR(1024) COMMENT '问题描述',
    indoor_temp DECIMAL(10,2) COMMENT '室内温度',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级：0-普通 1-较高 2-紧急 3-最高紧急',
    priority_upgrade_reason VARCHAR(512) COMMENT '优先级升级原因',
    linked_exception_id BIGINT COMMENT '关联巡检异常ID（同支线异常触发升级时关联）',
    metric_stable TINYINT DEFAULT 0 COMMENT '站点指标是否回稳：0-未回稳 1-已回稳',
    metric_stable_time DATETIME COMMENT '指标回稳确认时间',
    complaint_time DATETIME NOT NULL COMMENT '报冷时间',
    reporter_name VARCHAR(64) COMMENT '登记人姓名',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED-已创建 PROCESSING-处理中 REPAIRING-抢修中 REPAIRED-已修复 VISITED-已回访 CLOSED-已关闭',
    handler_id BIGINT COMMENT '处理人ID',
    handler_name VARCHAR(64) COMMENT '处理人姓名',
    repair_order_id BIGINT COMMENT '关联抢修工单ID',
    process_desc VARCHAR(1024) COMMENT '处理说明',
    finish_time DATETIME COMMENT '处理完成时间',
    visit_time DATETIME COMMENT '回访时间',
    visitor_name VARCHAR(64) COMMENT '回访人',
    visit_result VARCHAR(512) COMMENT '回访结果',
    satisfaction INT COMMENT '满意度：1-5分',
    close_time DATETIME COMMENT '关闭时间',
    closer_id BIGINT COMMENT '关闭人ID',
    closer_name VARCHAR(64) COMMENT '关闭人姓名',
    close_remark VARCHAR(512) COMMENT '关闭备注',
    remark VARCHAR(1024) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_station_id (stationId),
    INDEX idx_status (status),
    INDEX idx_complaint_time (complaint_time),
    INDEX idx_resident_phone (resident_phone),
    INDEX idx_priority (priority),
    INDEX idx_linked_exception_id (linked_exception_id),
    INDEX idx_metric_stable (metric_stable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='居民报冷事件表';

-- ============================================
-- 工单流转日志表
-- ============================================
DROP TABLE IF EXISTS hs_order_flow_log;
CREATE TABLE hs_order_flow_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_type VARCHAR(32) NOT NULL COMMENT '工单类型：EXCEPTION-异常单 REPAIR-抢修单 COMPLAINT-报冷单',
    order_id BIGINT NOT NULL COMMENT '工单ID',
    order_no VARCHAR(32) COMMENT '工单编号',
    action_type VARCHAR(64) NOT NULL COMMENT '操作类型：CREATE-创建 ASSIGN-派单 ACCEPT-接单 START-开始处理 FINISH-完成 CONFIRM-确认 CLOSE-关闭 UPGRADE-升级 TRANSFER-转办',
    action_desc VARCHAR(512) COMMENT '操作描述',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人姓名',
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    remark VARCHAR(1024) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order (order_type, order_id),
    INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志表';

-- ============================================
-- 站点指标历史表（用于趋势分析）
-- ============================================
DROP TABLE IF EXISTS hs_station_metric_history;
CREATE TABLE hs_station_metric_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    station_code VARCHAR(32) COMMENT '站点编码',
    station_name VARCHAR(128) COMMENT '站点名称',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型：PRESSURE-压力 SUPPLY_TEMP-供水温度 RETURN_TEMP-回水温度',
    metric_value DECIMAL(10,4) NOT NULL COMMENT '指标值',
    metric_status VARCHAR(32) DEFAULT 'NORMAL' COMMENT '指标状态：NORMAL-正常 BELOW_LIMIT-低于下限 ABOVE_LIMIT-高于上限',
    source_type VARCHAR(32) NOT NULL COMMENT '数据来源：INSPECTION-巡检录入 AUTO-自动采集 MANUAL-人工录入',
    record_id BIGINT COMMENT '来源记录ID',
    record_time DATETIME NOT NULL COMMENT '记录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_station_time (station_id, record_time),
    INDEX idx_metric_type (metric_type),
    INDEX idx_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点指标历史表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化用户
INSERT INTO sys_user (user_code, user_name, role_code, phone, status) VALUES
('dispatcher01', '张调度', 'DISPATCHER', '13800000001', 1),
('dispatcher02', '李调度', 'DISPATCHER', '13800000002', 1),
('inspector01', '王巡检', 'INSPECTOR', '13800000003', 1),
('inspector02', '赵巡检', 'INSPECTOR', '13800000004', 1),
('inspector03', '孙巡检', 'INSPECTOR', '13800000005', 1),
('repair01', '抢修一队', 'REPAIR', '13800000006', 1),
('repair02', '抢修二队', 'REPAIR', '13800000007', 1);

-- 初始化换热站
INSERT INTO hs_station (station_code, station_name, station_address, station_type, pressure_limit_min, pressure_limit_max, temp_limit_min, temp_limit_max, duty_person, duty_phone, sort_order, branch_line, status) VALUES
('ST001', '一号换热站', '朝阳区建国路88号', 'PRIMARY', 0.6, 1.2, 60.0, 95.0, '王站长', '13900000001', 1, 'BL-EAST-01', 1),
('ST002', '二号换热站', '朝阳区朝阳路100号', 'PRIMARY', 0.6, 1.2, 60.0, 95.0, '李站长', '13900000002', 2, 'BL-EAST-01', 1),
('ST003', '三号换热站', '海淀区中关村大街1号', 'PRIMARY', 0.6, 1.2, 60.0, 95.0, '张站长', '13900000003', 3, 'BL-WEST-01', 1),
('ST004', '四号换热站', '海淀区西三环北路2号', 'SECONDARY', 0.3, 0.6, 40.0, 60.0, '刘站长', '13900000004', 4, 'BL-WEST-01', 1),
('ST005', '五号换热站', '西城区西单北大街100号', 'SECONDARY', 0.3, 0.6, 40.0, 60.0, '陈站长', '13900000005', 5, 'BL-CENTER-01', 1),
('ST006', '六号换热站', '东城区东长安街1号', 'SECONDARY', 0.3, 0.6, 40.0, 60.0, '周站长', '13900000006', 6, 'BL-CENTER-01', 1),
('ST007', '七号换热站', '丰台区方庄路10号', 'PRIMARY', 0.6, 1.2, 60.0, 95.0, '吴站长', '13900000007', 7, 'BL-SOUTH-01', 1),
('ST008', '八号换热站', '石景山区八角西街8号', 'SECONDARY', 0.3, 0.6, 40.0, 60.0, '郑站长', '13900000008', 8, 'BL-SOUTH-01', 1);

-- 初始化居民报冷事件示例
INSERT INTO hs_cold_complaint (complaint_no, station_id, station_code, station_name, resident_name, resident_phone, resident_address, community, building_no, room_no, complaint_type, complaint_desc, indoor_temp, complaint_time, reporter_name, status) VALUES
('CL202501001', 1, 'ST001', '一号换热站', '张先生', '13600000001', '朝阳区建国路88号院1号楼101室', '阳光花园', '1号楼', '101', 'TEMPERATURE_LOW', '室内温度低，只有16度，暖气片不热', 16.0, '2025-01-15 08:30:00', '张调度', 'CREATED'),
('CL202501002', 2, 'ST002', '二号换热站', '李女士', '13600000002', '朝阳区朝阳路100号院3号楼502室', '朝阳家园', '3号楼', '502', 'LEAKAGE', '卫生间暖气片漏水', NULL, '2025-01-15 09:15:00', '李调度', 'PROCESSING'),
('CL202501003', 4, 'ST004', '四号换热站', '王先生', '13600000003', '海淀区西三环北路2号院2号楼303室', '紫金庄园', '2号楼', '303', 'NOT_HOT', '家里不暖和，需要上门查看', 17.5, '2025-01-15 10:00:00', '张调度', 'CREATED');
