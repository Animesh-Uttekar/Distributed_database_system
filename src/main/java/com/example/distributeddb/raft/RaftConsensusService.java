package com.example.distributeddb.raft;

import org.springframework.stereotype.Service;

@Service
public class RaftConsensusService {

    private boolean isLeader = true;

    public boolean commitTransaction(String command) {
        if (!isLeader) {
            System.out.println("Not the leader. Transaction forwarded to leader.");
            return false;
        }
        System.out.println("Raft consensus: Command '" + command + "' committed.");
        return true;
    }
}