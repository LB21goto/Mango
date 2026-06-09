package com.example.dto;

import lombok.Data;

@Data
public class RepeatTestRequest {
    private String userId;
    private String orderId;
    private Double amount;
    private String description;
}