package com.Ai.dto;

import java.util.List;
import java.util.Map;

public class TicketIntent {
    public String eventName;
    public String showTime;
    public String venue;
    public List<Seat> seats;
    public Integer price;
    public Map<String, String> buyerInfo;

    public static class Seat {
        public String area;
        public String row;
        public String seat;
    }

    // getters/setters 可按需添加或使用 public 字段并使用 Jackson
}