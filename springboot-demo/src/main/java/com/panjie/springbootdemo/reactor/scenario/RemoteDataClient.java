package com.panjie.springbootdemo.reactor.scenario;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 模拟远程数据服务的客户端（例如原本通过 HTTP/RPC 调用的其他微服务）
 */
@Component
public class RemoteDataClient {

    /**
     * 模拟耗时 500ms 获取用户信息
     */
    public Mono<UserProfile> getUserProfile(String userId) {
        return Mono.just(new UserProfile(userId, "张三", "VIP会员"))
                .delayElement(Duration.ofMillis(500));
    }

    /**
     * 模拟耗时 800ms 获取用户的近期订单列表
     */
    public Flux<OrderInfo> getRecentOrders(String userId) {
        return Flux.just(
                new OrderInfo("O1001", "MacBook Pro", 14999.00),
                new OrderInfo("O1002", "iPhone 15", 5999.00))
                .delayElements(Duration.ofMillis(400)); // 每个元素延迟 400ms，总共800ms
    }

    /**
     * 模拟耗时 300ms 获取积分信息
     */
    public Mono<Integer> getUserScore(String userId) {
        return Mono.just(8850)
                .delayElement(Duration.ofMillis(300));
    }

    /* -- 内部模型类，为了演示方便放在此处 -- */

    public static class UserProfile {
        public String id;
        public String name;
        public String level;

        public UserProfile(String id, String name, String level) {
            this.id = id;
            this.name = name;
            this.level = level;
        }
    }

    public static class OrderInfo {
        public String orderId;
        public String productName;
        public Double amount;

        public OrderInfo(String orderId, String productName, Double amount) {
            this.orderId = orderId;
            this.productName = productName;
            this.amount = amount;
        }
    }
}
