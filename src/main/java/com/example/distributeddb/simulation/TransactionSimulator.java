package com.example.distributeddb.simulation;

import com.example.distributeddb.model.Order;
import com.example.distributeddb.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

@Component
public class TransactionSimulator implements CommandLineRunner {

    private final OrderService orderService;

    public TransactionSimulator(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "simulate".equalsIgnoreCase(args[0])) {
            System.out.println("Starting simulations...");
            simulateTransactions(true, "With Sharding & Raft");
            simulateTransactions(false, "Without Sharding & Raft");
        }
    }

    private void simulateTransactions(boolean useConsensus, String label) throws InterruptedException {
        final int totalTransactions = 100000;
        final int concurrencyLevel = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrencyLevel);
        CountDownLatch latch = new CountDownLatch(totalTransactions);

        LongAdder totalLatencyNanos = new LongAdder();
        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        long simulationStart = System.currentTimeMillis();

        for (int i = 0; i < totalTransactions; i++) {
            long customerId = i % 100;
            double amount = Math.random() * 100;
            Order order = new Order(customerId, amount);

            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    if (useConsensus) {
                        orderService.createOrderWithConsensus(order);
                    } else {
                        orderService.createOrderWithoutConsensus(order);
                    }
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Transaction error: " + e.getMessage());
                    errorCounter.incrementAndGet();
                } finally {
                    long elapsed = System.nanoTime() - start;
                    totalLatencyNanos.add(elapsed);
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        long simulationEnd = System.currentTimeMillis();
        long totalTimeMs = simulationEnd - simulationStart;
        double avgLatencyMs = (totalLatencyNanos.doubleValue() / totalTransactions) / 1000000.0;
        double throughput = (totalTransactions * 1000.0) / totalTimeMs;

        System.out.println("Simulation (" + label + ") complete:");
        System.out.println("  Total transactions: " + totalTransactions);
        System.out.println("  Total time: " + totalTimeMs + " ms");
        System.out.println("  Avg latency per transaction: " + avgLatencyMs + " ms");
        System.out.println("  Throughput: " + throughput + " tx/sec");
        System.out.println("  Successes: " + successCounter.get() + ", Errors: " + errorCounter.get());
        System.out.println("-----------------------------------------------------");
    }
}
