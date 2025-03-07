package com.example.distributeddb.raft;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class RaftConsensusService {
    private boolean isLeader = true;
    private final Random random = new Random();

    @Value("${raft.node.id}")
    private String nodeId;

    @Value("${raft.peers}")
    private List<String> peers = new ArrayList<>();

    public boolean commitTransaction(String command) {
        try {
            Thread.sleep(random.nextInt(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (!isLeader || random.nextDouble() < 0.01) {
            System.out.println("Node " + nodeId + " simulated consensus failure for command: " + command);
            return false;
        }
        System.out.println("Node " + nodeId + " simulated consensus commit for command: " + command);
        return true;
    }

    public void simulateLeaderElection() {
        int leaderIndex = random.nextInt(peers.size() + 1);
        String newLeader = leaderIndex < peers.size() ? peers.get(leaderIndex) : nodeId;

        isLeader = newLeader.equals(nodeId);
        if (isLeader) {
            System.out.println("Simulated leader election: Node " + nodeId + " is now LEADER.");
        } else {
            System.out.println("Simulated leader election: Node " + nodeId + " is now FOLLOWER.");
        }
    }

    public boolean isLeader() {
        return isLeader;
    }
}
