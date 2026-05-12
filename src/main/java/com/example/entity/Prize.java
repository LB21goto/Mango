package com.example.entity;

public class Prize {
    private Long id;
    private Long activityId;
    private String prizeName;
    private String prizeType; // VIRTUAL/PHYSICAL
    private boolean virtual;
    private int totalInventory;
    private int remainingInventory;
    private int weight;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }

    public String getPrizeType() { return prizeType; }
    public void setPrizeType(String prizeType) { this.prizeType = prizeType; }

    public boolean isVirtual() { return virtual; }
    public void setVirtual(boolean virtual) { this.virtual = virtual; }

    public int getTotalInventory() {
        return 0;
    }
    public void setTotalInventory(int totalInventory) { this.totalInventory = totalInventory; }

    public int getRemainingInventory() { return remainingInventory; }
    public void setRemainingInventory(int remainingInventory) { this.remainingInventory = remainingInventory; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public String getDeliveryType() {
        return virtual ? "virtualImmediateDispatcher" : "physicalImmediateDispatcher";
    }

    public Object getMeta() {
        return null;
    }
}
