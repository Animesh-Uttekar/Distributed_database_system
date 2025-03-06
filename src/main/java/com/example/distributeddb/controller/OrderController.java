package com.example.distributeddb.controller;

import com.example.distributeddb.model.Order;
import com.example.distributeddb.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrderWithConsensus(order);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<Order>> getOrders(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrders(customerId);
        return ResponseEntity.ok(orders);
    }
}