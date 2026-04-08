package com.example.xxljoblearning.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 分片任务处理器 - 模拟大数据量处理场景
 *
 * 场景：假设有 10000 个用户需要发送通知，使用分片任务将任务分配给多个执行器并行处理
 */
@Slf4j
@Component
public class ShardingJobHandler {

    // 模拟数据库中的总数据量
    private static final int TOTAL_DATA_COUNT = 10000;

    /**
     * 分片广播任务 - 广播到所有执行器，每个执行器处理一部分数据
     */
    @XxlJob("shardingBroadcastJob")
    public void shardingBroadcastJob() throws Exception {
        // 获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片广播任务 - 当前分片序号: {}, 总分片数: {}", shardIndex, shardTotal);
        log.info("分片广播任务 - 当前分片序号: {}, 总分片数: {}", shardIndex, shardTotal);

        if (shardTotal == 0) {
            XxlJobHelper.log("警告：总分片数为0，无法执行分片任务");
            XxlJobHelper.handleFail("总分片数不能为0");
            return;
        }

        // 计算当前分片需要处理的数据范围
        List<Integer> myDataIds = calculateShardData(shardIndex, shardTotal, TOTAL_DATA_COUNT);

        XxlJobHelper.log("本分片需要处理的数据量: {}", myDataIds.size());
        XxlJobHelper.log("数据范围: {} 到 {}", myDataIds.get(0), myDataIds.get(myDataIds.size() - 1));

        // 模拟处理数据
        int successCount = 0;
        int failCount = 0;

        for (Integer dataId : myDataIds) {
            try {
                // 模拟业务处理
                boolean result = processData(dataId);

                if (result) {
                    successCount++;
                    XxlJobHelper.log("处理成功 - 数据ID: {}", dataId);
                } else {
                    failCount++;
                    XxlJobHelper.log("处理失败 - 数据ID: {}", dataId);
                }

                // 每处理100条记录一次进度
                if ((successCount + failCount) % 100 == 0) {
                    XxlJobHelper.log("处理进度: {}/{} ({:.1f}%)",
                            successCount + failCount, myDataIds.size(),
                            (successCount + failCount) * 100.0 / myDataIds.size());
                }

            } catch (Exception e) {
                failCount++;
                XxlJobHelper.log("处理异常 - 数据ID: {}, 错误: {}", dataId, e.getMessage());
            }
        }

        // 输出处理结果
        XxlJobHelper.log("========================================");
        XxlJobHelper.log("分片 {} 处理完成", shardIndex);
        XxlJobHelper.log("成功: {}, 失败: {}, 总计: {}", successCount, failCount, myDataIds.size());
        XxlJobHelper.log("========================================");

        // 如果有失败，标记任务失败
        if (failCount > 0) {
            XxlJobHelper.handleFail(String.format("存在 %d 条数据处理失败", failCount));
        } else {
            XxlJobHelper.handleSuccess();
        }
    }

    /**
     * 简单的分片任务 - 使用取模方式分配数据
     */
    @XxlJob("simpleShardingJob")
    public void simpleShardingJob() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("简单分片任务 - 分片: {}/{}", shardIndex, shardTotal);

        // 模拟处理 ID 对总分片数取模等于当前分片序号的数据
        int processedCount = 0;

        for (int i = 1; i <= TOTAL_DATA_COUNT; i++) {
            if (i % shardTotal == shardIndex) {
                // 处理数据
                XxlJobHelper.log("处理数据 ID: {}, 计算: {} % {} = {}", i, i, shardTotal, i % shardTotal);
                processedCount++;

                // 模拟处理时间
                TimeUnit.MILLISECONDS.sleep(10);
            }
        }

        XxlJobHelper.log("分片 {} 共处理 {} 条数据", shardIndex, processedCount);
        XxlJobHelper.handleSuccess();
    }

    /**
     * 计算当前分片需要处理的数据ID列表
     *
     * @param shardIndex 当前分片序号
     * @param shardTotal 总分片数
     * @param totalCount 总数据量
     * @return 当前分片需要处理的数据ID列表
     */
    private List<Integer> calculateShardData(int shardIndex, int shardTotal, int totalCount) {
        List<Integer> result = new ArrayList<>();

        // 平均分配，余数分配给前几个分片
        int avgCount = totalCount / shardTotal;
        int remainder = totalCount % shardTotal;

        // 计算当前分片的起始和结束位置
        int start;
        int end;

        if (shardIndex < remainder) {
            // 前 remainder 个分片多处理一条数据
            start = shardIndex * (avgCount + 1) + 1;
            end = start + avgCount;
        } else {
            start = remainder * (avgCount + 1) + (shardIndex - remainder) * avgCount + 1;
            end = start + avgCount - 1;
        }

        // 生成数据ID列表
        for (int i = start; i <= end && i <= totalCount; i++) {
            result.add(i);
        }

        return result;
    }

    /**
     * 模拟数据处理
     *
     * @param dataId 数据ID
     * @return 处理是否成功
     */
    private boolean processData(int dataId) {
        try {
            // 模拟处理耗时
            TimeUnit.MILLISECONDS.sleep(5);

            // 模拟随机失败（1% 概率）
            if (Math.random() < 0.01) {
                return false;
            }

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
