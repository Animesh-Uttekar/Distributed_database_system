package com.example.distributeddb.sharding;

import org.springframework.stereotype.Component;

@Component
public class ShardRouter {

    public int getShard(Long customerId, int numShards) {
        return (int) (customerId % numShards);
    }
}