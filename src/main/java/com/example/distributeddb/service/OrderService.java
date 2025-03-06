package com.example.distributeddb.service;

import com.example.distributeddb.model.Order;
import com.example.distributeddb.repository.OrderRepository;
import com.example.distributeddb.raft.RaftConsensusService;
import com.example.distributeddb.sharding.ShardRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RaftConsensusService raftConsensusService;

    @Autowired
    private ShardRouter shardRouter;

    private final int NUM_SHARDS = 4;

    // Method that uses sharding and simulated Raft consensus.
    public Order createOrderWithConsensus(Order order) {
        int shardId = shardRouter.getShard(order.getCustomerId(), NUM_SHARDS);
        System.out.println("Routing order for customer " + order.getCustomerId() + " to shard " + shardId);
        String command = "CREATE_ORDER:" + order.getCustomerId() + ":" + order.getAmount();
        boolean success = raftConsensusService.commitTransaction(command);
        if (!success) {
            throw new RuntimeException("Simulated consensus failure");
        }
        return orderRepository.save(order);
    }

    // Method that bypasses sharding and consensus.
    public Order createOrderWithoutConsensus(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> getOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}
