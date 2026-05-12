package com.example.service;

import com.example.entity.Order;


public interface OrderService {
    Order getOrder(String orderNo);
    void cancelOrder(String orderNo);


}
