package com.example.controller;

import com.example.entity.Order;
import com.example.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class MyOrderController {

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/my")
    public List<Order> getMyOrders(@RequestParam("userId") Long userId) {
        return orderMapper.selectByUserId(userId);
    }
}