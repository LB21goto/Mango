package com.example.dto;

public class TicketDTO {
    private Long userId;
    private Long seatId;
    private Long eventId;

    // 必须要有 Get 和 Set 方法，否则 JSON 反序列化会失败（赋不上值）
    // 如果你用了 Lombok，直接加 @Data 注解即可
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}
