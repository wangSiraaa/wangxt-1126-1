package com.heatstation.event;

import org.springframework.context.ApplicationEvent;

public class RepairOrderStatusChangedEvent extends ApplicationEvent {

    private final Long repairOrderId;
    private final String sourceType;
    private final Long sourceId;
    private final String newStatus;

    public RepairOrderStatusChangedEvent(Object source, Long repairOrderId,
                                          String sourceType, Long sourceId, String newStatus) {
        super(source);
        this.repairOrderId = repairOrderId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.newStatus = newStatus;
    }

    public Long getRepairOrderId() {
        return repairOrderId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
