package com.panjie.springbootdemo.reactor.scenario;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 组合服务：使用 Reactor 异步并行地聚合多个微服务的数据
 */
@Service
public class DashboardAggregatorService {

    private final RemoteDataClient remoteDataClient;

    public DashboardAggregatorService(RemoteDataClient remoteDataClient) {
        this.remoteDataClient = remoteDataClient;
    }

    /**
     * 获取 Dashboard 数据：
     * 需要调用三个接口：用户信息 (500ms)、订单列表 (800ms)、积分信息 (300ms)
     * 如果同步调用，总时长约为 1600ms。
     * 使用 Reactor 进行 Mono.zip 并行组合，总时长约等于最长的那个接口耗时 (800ms)。
     */
    public Mono<DashboardVO> getDashboardData(String userId) {
        long startTime = System.currentTimeMillis();

        Mono<RemoteDataClient.UserProfile> userMono = remoteDataClient.getUserProfile(userId);

        // Flux 需要收集成 List 才能方便地组合进单个聚合对象中
        Mono<List<RemoteDataClient.OrderInfo>> ordersMono = remoteDataClient.getRecentOrders(userId).collectList();

        Mono<Integer> scoreMono = remoteDataClient.getUserScore(userId);

        // 使用 Mono.zip 将三个异步任务并行执行并合并结果
        return Mono.zip(userMono, ordersMono, scoreMono)
                .map(tuple -> {
                    // tuple.getT1() 是 UserProfile, getT2() 是 orders List, getT3() 是 score
                    DashboardVO vo = new DashboardVO();
                    vo.user = tuple.getT1();
                    vo.orders = tuple.getT2();
                    vo.score = tuple.getT3();
                    vo.aggregatorCostMs = System.currentTimeMillis() - startTime;
                    return vo;
                });
    }

    /* -- 聚合视图对象 -- */
    public static class DashboardVO {
        public RemoteDataClient.UserProfile user;
        public List<RemoteDataClient.OrderInfo> orders;
        public Integer score;
        public long aggregatorCostMs; // 记录聚合耗时
    }
}
