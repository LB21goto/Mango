package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Program {
    private Long id;
    private String title;
    private String subtitle;
    private String category;
    private String venue;
    private String address;
    private String description;
    private String coverImage;
    private String session;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
