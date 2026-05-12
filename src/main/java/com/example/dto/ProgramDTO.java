package com.example.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProgramDTO {
    private Long id;
    private String title;
    private String subtitle;
    private String category;
    private String venue;
    private String address;
    private String description;
    private String coverImage;
    private String session;
    // 在 ProgramDTO 类里
    private List<Map<String, Object>> sessions;

    // getter setter
    public List<Map<String, Object>> getSessions() { return sessions; }
    public void setSessions(List<Map<String, Object>> sessions) { this.sessions = sessions; }

}
