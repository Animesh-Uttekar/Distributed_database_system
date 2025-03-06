package com.example.distributeddb.service;

import com.example.distributeddb.model.Order;
import com.example.distributeddb.raft.RaftConsensusService;
import com.example.distributeddb.repository.OrderRepository;
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

    public Order createOrder(Order order) {

        int shardId = shardRouter.getShard(order.getCustomerId(), NUM_SHARDS);
        System.out.println("Routing order to shard: " + shardId);

        String command = "CREATE_ORDER:" + order.getCustomerId() + ":" + order.getAmount();

        if (!raftConsensusService.commitTransaction(command)) {
            throw new RuntimeException("Transaction failed due to consensus issues.");
        }

        return orderRepository.save(order);
    }

    public List<Order> getOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}
