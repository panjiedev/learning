package com.example.redis.service;

import com.example.redis.model.Prize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
public class LotteryService {

    private static final String LOTTERY_KEY = "lottery:prizes";
    private final Random random = new Random();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 初始化奖品池
     * 将权重转换为累加值（前缀和）存储在 Sorted Set 中
     */
    public void initPrizes(List<Prize> prizes) {
        // 先清理旧数据
        redisTemplate.delete(LOTTERY_KEY);

        double currentSum = 0;
        for (Prize prize : prizes) {
            currentSum += prize.getWeight();
            // Member 是奖品名称或 ID，Score 是当前的累加权重
            redisTemplate.opsForZSet().add(LOTTERY_KEY, prize.getName(), currentSum);
        }
        log.info("奖品池初始化完成，总权重: {}", currentSum);
    }

    /**
     * 权重抽奖逻辑
     */
    public String draw() {
        // 1. 获取总权重（即 ZSet 中最大的 score）
        Set<ZSetOperations.TypedTuple<Object>> lastElement = redisTemplate.opsForZSet()
                .reverseRangeWithScores(LOTTERY_KEY, 0, 0);
        if (lastElement == null || lastElement.isEmpty()) {
            return "奖品池为空";
        }

        Double totalWeight = lastElement.iterator().next().getScore();
        if (totalWeight == null || totalWeight <= 0) {
            return "无效的权重配置";
        }

        // 2. 生成 [0, totalWeight) 范围内的随机数
        double randomNumber = random.nextDouble() * totalWeight;

        // 3. 寻找第一个 score >= randomNumber 的成员
        // 使用 Redis 的 ZRANGEBYSCORE 指令获取
        Set<Object> result = redisTemplate.opsForZSet().rangeByScore(LOTTERY_KEY, randomNumber, Double.MAX_VALUE, 0, 1);

        if (result == null || result.isEmpty()) {
            return "抽奖失败";
        }

        String prizeName = (String) result.iterator().next();
        log.debug("随机数: {}, 抽中奖品: {}", randomNumber, prizeName);
        return prizeName;
    }
}
