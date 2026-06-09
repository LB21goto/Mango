package com.example.controller;

import com.example.dto.TicketDTO;
import com.example.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping ("/ticket/seat")
public class Order {

    @Autowired
    private SeatService seatService;
    @PostMapping("/create")
    public String createOrder(@RequestBody TicketDTO ticketDTO) {
        // 直接打印原始请求体（方便查看前端到底发了什么）
        System.out.println("=== 收到购票原始请求 ===");
        System.out.println(ticketDTO);
        System.out.println("=== 原始请求结束 ===");
        Long userId = ticketDTO.getUserId();
        Long seatId = ticketDTO.getSeatId();
        System.out.println("收到购票请求 -> 用户:" + userId
                + ", 座位:" + seatId
                + ", 节目:" + ticketDTO.getEventId());
        return seatService.getseat(userId, seatId);
    }
    @GetMapping("/relese")
    public String releseOrder(@RequestParam("userId") Long userId, @RequestParam("seatId") Long seatId) {
        return seatService.releseseat(seatId);
    }


}
