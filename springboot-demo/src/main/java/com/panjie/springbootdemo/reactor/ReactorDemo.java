package com.panjie.springbootdemo.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactor 模型学习 Demo
 *
 * 包含 Mono（0或1个元素的异步序列）和 Flux（0到N个元素的异步序列）的基础示例。
 */
public class ReactorDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("====== 1. 基本的 Mono 示例 ======");
        demoMono();

        System.out.println("\n====== 2. 基本的 Flux 示例 ======");
        demoFlux();

        System.out.println("\n====== 3. Flux 操作符示例 (map, filter) ======");
        demoOperators();

        System.out.println("\n====== 4. 异步延迟示例 ======");
        demoAsyncFlux();

        // 主线程等待一会，以便观察异步输出
        Thread.sleep(2500);
        System.out.println("\n====== 运行结束 ======");
    }

    private static void demoMono() {
        // 创建一个包含单个元素的 Mono，并订阅它
        Mono<String> mono = Mono.just("Hello Reactor");

        mono.subscribe(
                value -> System.out.println("收到 Mono 数据: " + value),
                error -> System.err.println("发生错误: " + error),
                () -> System.out.println("Mono 处理完成"));
    }

    private static void demoFlux() {
        // 创建一个包含多个元素的 Flux
        Flux<String> flux = Flux.just("Java", "Spring", "Reactor", "Redis");

        flux.subscribe(
                value -> System.out.println("收到 Flux 数据: " + value),
                error -> System.err.println("发生错误: " + error),
                () -> System.out.println("Flux 处理完成"));
    }

    private static void demoOperators() {
        Flux.range(1, 10)
                .filter(i -> i % 2 == 0) // 过滤出偶数
                .map(i -> "偶数: " + i) // 转换数据格式
                .subscribe(System.out::println);
    }

    private static void demoAsyncFlux() {
        // 每隔500毫秒发射一个递增的数字，取前4个
        Flux.interval(Duration.ofMillis(500))
                .take(4)
                .subscribe(
                        value -> System.out
                                .println("异步收到数据: " + value + " (Thread: " + Thread.currentThread().getName() + ")"));
    }
}
