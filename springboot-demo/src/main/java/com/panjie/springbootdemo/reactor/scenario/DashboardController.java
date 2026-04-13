package com.panjie.springbootdemo.reactor.scenario;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardAggregatorService aggregatorService;

    public DashboardController(DashboardAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    /**
     * WebFlux 控制器方法直接返回 Mono。
     * Spring WebFlux 会自动处理异步非阻塞流，并在数据准备好后将其写入 HTTP 响应。
     */
    @GetMapping("/{userId}")
    public Mono<DashboardAggregatorService.DashboardVO> getDashboard(@PathVariable String userId) {
        return aggregatorService.getDashboardData(userId);
    }
}
