package com.example.DrawStrategy;

public class DeliverResult {
    public enum Status { DELIVERED, QUEUED, FAILED }

    private Status status;
    private String message;
    private String deliveryId; // third-party order id 或队列 id

    public DeliverResult(Status status, String message) {
        this(status, message, null);
    }
    public DeliverResult(Status status, String message, String deliveryId) {
        this.status = status; this.message = message; this.deliveryId = deliveryId;
    }
    // getters/setters
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public String getDeliveryId() { return deliveryId; }
}
