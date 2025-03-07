package com.example.distributeddb.simulation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

@Component
public class TransactionSimulator implements CommandLineRunner {

    @Value("${node.urls}")
    private String[] nodeUrls;

    private final HttpClient httpClient = HttpClient.newHttpClient();

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

            String targetNodeUrl = nodeUrls[i % nodeUrls.length];
            if (!useConsensus) {
                targetNodeUrl += (targetNodeUrl.contains("?") ? "&" : "?") + "mode=direct";
            }
            String jsonPayload = String.format("{\"customerId\": %d, \"amount\": %.2f}", customerId, amount);
            String finalTargetNodeUrl = targetNodeUrl;
            executor.submit(() -> {
                long startTime = System.nanoTime();
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(finalTargetNodeUrl))
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(5))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        successCounter.incrementAndGet();
                    } else {
                        errorCounter.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Transaction error: " + e.getMessage());
                    errorCounter.incrementAndGet();
                } finally {
                    long elapsedNanos = System.nanoTime() - startTime;
                    totalLatencyNanos.add(elapsedNanos);
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long simulationEnd = System.currentTimeMillis();
        long totalTimeMs = simulationEnd - simulationStart;
        double avgLatencyMs = (totalLatencyNanos.doubleValue() / totalTransactions) / 100000.0;
        double throughput = (totalTransactions * 1000.0) / totalTimeMs;

        System.out.println("Simulation (" + label + ") complete:");
        System.out.println("  Total transactions: " + totalTransactions);
        System.out.println("  Total time: " + totalTimeMs + " ms");
        System.out.println("  Average latency per transaction: " + avgLatencyMs + " ms");
        System.out.println("  Throughput: " + throughput + " tx/sec");
        System.out.println("  Successful transactions: " + successCounter.get() +
                ", Failed transactions: " + errorCounter.get());
    }
}
