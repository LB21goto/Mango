package com.Ai;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class LLMSingleFlightCache {
    private final ConcurrentHashMap<String, CompletableFuture<String>> inflight = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String getCachedResult(String key) {
        return cache.get(key);
    }

    public static class Flight {
        public final CompletableFuture<String> future;
        public final boolean isLeader;
        public Flight(CompletableFuture<String> future, boolean isLeader) {
            this.future = future;
            this.isLeader = isLeader;
        }
    }

    /**
     * 原子地为 key 创建或返回已有的 future。
     * 返回的 Flight.isLeader == true 表示调用者是创建者（应负责执行实际 LLM 调用并在完成后调用 cacheResult）
     */
    public Flight getOrCreateFuture(String key) {
        CompletableFuture<String> newFuture = new CompletableFuture<>();
        CompletableFuture<String> existing = inflight.putIfAbsent(key, newFuture);
        if (existing == null) {
            // 我创建了 future —— 我是 leader
            return new Flight(newFuture, true);
        } else {
            // 已有 leader，返回已有的 future（我是 follower）
            return new Flight(existing, false);
        }
    }

    /**
     * leader 在得到 LLM 结果后调用：
     * - 将结果写入缓存（cache）
     * - 完成等待该 future 的 follower（future.complete）
     * - 从 inflight 中移除该 key
     */
    public void cacheResult(String key, String result) {
        cache.put(key, result);
        CompletableFuture<String> future = inflight.remove(key);
        if (future != null && !future.isDone()) {
            future.complete(result);
        }
    }

    /**
     * leader 在发生异常时调用：completeExceptionally 并移除 inflight
     */
    public void cleanupOnError(String key, Throwable e) {
        CompletableFuture<String> future = inflight.remove(key);
        if (future != null && !future.isDone()) {
            future.completeExceptionally(e);
        }
    }

    // 可选：缓存失效策略、定时清理、最大缓存大小等
}