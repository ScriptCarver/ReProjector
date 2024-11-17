package com.example.reprojector.helpers;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadsHolder {
    private static final int calculationsThreadPoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2 - 1);
    private static final ExecutorService calculationsExecutorService = Executors.newFixedThreadPool(calculationsThreadPoolSize);
    private static final LinkedList<Future> futures = new LinkedList<>();

    public static void addTask(Runnable runnable) {
        futures.add(calculationsExecutorService.submit(runnable));
    }

    public static void waitForTasksToFinish() {
        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        futures.clear();
    }


}