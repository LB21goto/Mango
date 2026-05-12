package com.example.entity;

import java.time.LocalDateTime;

public class Activity {
    private Long id;
    private String name;
    private String status; // PENDING/ACTIVE/EXPIRED
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStrategyType() {
        return null;
    }
}