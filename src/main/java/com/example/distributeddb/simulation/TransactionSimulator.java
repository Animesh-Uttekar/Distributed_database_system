package com.example.distributeddb.simulation;

import com.example.distributeddb.model.Order;
import com.example.distributeddb.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TransactionSimulator implements CommandLineRunner {

    private final OrderService orderService;

    public TransactionSimulator(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "simulate".equalsIgnoreCase(args[0])) {
            simulateTransactions();
        }
    }

    private void simulateTransactions() throws InterruptedException {
        int totalTransactions = 100000;
        int concurrencyLevel = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrencyLevel);
        CountDownLatch latch = new CountDownLatch(totalTransactions);

        for (int i = 0; i < totalTransactions; i++) {
            long customerId = i % 100;
            double amount = Math.random() * 100;
            Order order = new Order(customerId, amount);

            executor.submit(() -> {
                try {
                    orderService.createOrder(order);
                } catch (Exception e) {
                    System.err.println("Error processing order: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        System.out.println("Completed simulation of " + totalTransactions + " transactions.");
    }
}
