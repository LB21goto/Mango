package com.example.controller;

import com.example.service.SeatBitmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket/seats")
public class TicketSeatController {

    @Autowired
    private SeatBitmapService seatBitmapService;

    /**
     * GET /ticket/seats/sold?eventId=xxx[&zeroBased=true]
     * 返回已售座位列表，JSON 数组，例如: [1,5,8,12]
     */
    @GetMapping("/sold")
    public ResponseEntity<List<Integer>> getSoldSeats(
            @RequestParam("eventId") String eventId,
            @RequestParam(value = "zeroBased", required = false, defaultValue = "false") boolean zeroBased
    ) {
        List<Integer> sold = seatBitmapService.getSoldSeats(eventId, zeroBased);
        return ResponseEntity.ok(sold);
    }
}
