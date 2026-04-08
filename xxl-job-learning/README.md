# XXL-Job Learning 项目

## 项目结构

```
xxl-job-learning/
├── src/main/java/com/example/xxljoblearning/
│   ├── XxlJobLearningApplication.java    # Spring Boot 启动类
│   ├── config/
│   │   └── XxlJobConfig.java              # XXL-Job 执行器配置
│   └── job/
│       └── ShardingJobHandler.java        # 分片任务处理器
├── src/main/resources/
│   └── application.yml                    # 应用配置文件
├── docker/
│   ├── docker-compose.yml                 # Docker Compose 部署文件
│   ├── Dockerfile.executor                # 执行器 Dockerfile
│   └── init-db/
│       └── xxl_job.sql                    # XXL-Job 数据库初始化脚本
└── pom.xml                                # Maven 配置文件
```

## 快速开始

### 1. 启动调度中心（Docker 方式）

```bash
cd docker
docker-compose up -d xxl-job-mysql xxl-job-admin
```

等待 MySQL 和 XXL-Job Admin 启动完成后，访问：
- 调度中心地址：http://localhost:8080/xxl-job-admin
- 默认账号：admin / 123456

### 2. 本地启动执行器

```bash
cd xxl-job-learning
mvn spring-boot:run
```

或使用 IDE 直接运行 `XxlJobLearningApplication`

### 3. Docker 方式启动执行器

```bash
cd docker
docker-compose up -d xxl-job-executor-1 xxl-job-executor-2
```

这将启动两个执行器实例，用于测试分片任务。

## 分片任务说明

### 任务一：shardingBroadcastJob（分片广播任务）

- **路由策略**：分片广播
- **功能**：将 10000 条数据平均分配给所有执行器处理
- **分片算法**：平均分配，余数分配给前几个分片

示例：
- 如果有 2 个执行器：
  - 执行器 0：处理 ID 1-5000
  - 执行器 1：处理 ID 5001-10000

### 任务二：simpleShardingJob（简单分片任务）

- **路由策略**：分片广播
- **功能**：使用取模方式分配数据
- **分片算法**：ID % 总分片数 == 当前分片序号

示例：
- 如果有 3 个执行器：
  - 执行器 0：处理 ID 3,6,9,12...
  - 执行器 1：处理 ID 1,4,7,10...
  - 执行器 2：处理 ID 2,5,8,11...

## 配置调度中心任务

1. 登录调度中心 http://localhost:8080/xxl-job-admin
2. 点击左侧菜单"执行器管理" -> "新增执行器"
   - AppName：xxl-job-learning-executor
   - 名称：学习项目执行器
   - 注册方式：自动注册
3. 点击左侧菜单"任务管理" -> "新增任务"
   - 执行器：学习项目执行器
   - 任务描述：分片广播任务示例
   - 负责人：你的名字
   - 调度类型：CRON
   - Cron：0/30 * * * * ?（每30秒执行一次）
   - 运行模式：BEAN
   - JobHandler：shardingBroadcastJob
   - 路由策略：分片广播
   - 阻塞处理策略：单机串行
4. 点击"操作" -> "执行一次" 测试任务

## 查看执行日志

1. 在调度中心"任务管理"页面，点击任务后的"日志"按钮
2. 可以查看每次调度的执行结果和详细日志
3. 点击"执行日志"可以查看具体输出

## 清理资源

```bash
cd docker
docker-compose down -v
```

这将停止并删除所有容器和卷。
