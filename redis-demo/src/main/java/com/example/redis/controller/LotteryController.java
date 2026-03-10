package com.example.redis.controller;

import com.example.redis.model.Prize;
import com.example.redis.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/lottery")
public class LotteryController {

    @Autowired
    private LotteryService lotteryService;

    /**
     * 默认初始化一些奖品
     */
    @PostMapping("/init")
    public String init() {
        List<Prize> prizes = new ArrayList<>();
        prizes.add(new Prize("1", "一等奖：iPhone 15 Pro", 1)); // 权重 1
        prizes.add(new Prize("2", "二等奖：iPad Air", 10)); // 权重 10
        prizes.add(new Prize("3", "三等奖：AirPods Pro", 30)); // 权重 30
        prizes.add(new Prize("4", "参与奖：10元优惠券", 60)); // 权重 60

        lotteryService.initPrizes(prizes);
        return "奖品初始化成功！总权重 101 (1+10+30+60)";
    }

    /**
     * 执行单次抽奖
     */
    @GetMapping("/draw")
    public String draw() {
        return lotteryService.draw();
    }

    /**
     * 批量模拟抽奖，验证权重分布
     */
    @GetMapping("/batch-draw")
    public Map<String, Integer> batchDraw(@RequestParam(defaultValue = "1000") int times) {
        Map<String, Integer> stats = new HashMap<>();
        for (int i = 0; i < times; i++) {
            String prize = lotteryService.draw();
            stats.put(prize, stats.getOrDefault(prize, 0) + 1);
        }
        return stats;
    }
}
