# Reactor 模型实战：基于 Spring Boot WebFlux 的仪表盘服务

本文档记录了我们在 `springboot-demo` 项目中引入并学习 Java Reactor 模型的实战过程，涵盖了 Spring Boot 的版本升级和真实业务聚合接口的落地实现。

## 1. 基础环境升级

为了更好地支持并实战完整的 WebFlux 与 Reactor 体系，我们首先升级了环境依赖：
- **Spring Boot**: 从 `1.5.9.RELEASE` 升级到了 `2.7.18`（原生支持 JDK 8 的最终核心版本）。
- **Spring Cloud**: 升级至 `2021.0.8` (匹配 Spring Boot 2.7.x 的版本流)。
- **引入依赖库**: 去除了旧版 MVC，引入了基于 Reactor 的 `spring-boot-starter-webflux`。它默认集成了 Netty 作为 HTTP 服务器，实现全异步非阻塞。

*(注：详见修改后的 `pom.xml`)*

## 2. Reactor 模型与核心知识点

**Reactor 的关键概念：**
* **`Mono<T>`**：表示包含 **0 或 1 个** 元素的异步计算结果流。例如：通过用户 ID 获取一个 `UserProfile` 对象。
* **`Flux<T>`**：表示包含 **0 到 N 个** 元素的异步计算结果流。例如：获取用户的多条 `OrderInfo` 记录。

**相较于传统阻塞编程的优势：**
在以往的 `Spring MVC + Tomcat` 模式下，一个线程处理一个 HTTP 请求并执行阻塞 I/O（比如查询数据库、发起 RestTemplate 请求资源），在 I/O 等待期间线程会一直挂起，浪费系统资源。
而通过 Reactor 与 NIO 网络层结合，当发生 I/O 延迟时线程立即让出给别的请求使用；在数据返回后，触发回调链继续执行，达到了极高的吞吐量。

## 3. 业务场景实战：仪表盘并发聚合服务

我们实现了一个最经典的 Reactor 业务场景——**网关/BFF 层级的数据并发聚合（Data Aggregation）**。

### 3.1 场景描述
假设用户登录我们的 App 后，进入“我的主页”（Dashboard），前端需要在这个页面同时展示：
1. **用户信息**（由 User 服务提供，耗时 ~500ms）
2. **近期订单**（由 Order 服务提供，耗时 ~800ms）
3. **我的积分**（由 Score 服务提供，耗时 ~300ms）

如果是传统的串行写法，组装整个结果需要消耗 `500 + 800 + 300 = 1600ms`。

### 3.2 Reactor 解决方案与代码实现
我们在 `com.panjie.springbootdemo.reactor.scenario` 包下编写了具体的代码落地。

#### A: 模拟远程服务 (`RemoteDataClient.java`)
使用 Reactor 提供的 `delayElement` 和 `delayElements` 模拟了异步网络 I/O 的延迟效果，返回 `Mono<UserProfile>`、`Flux<OrderInfo>` 等等。

#### B: 组装与聚合引擎 (`DashboardAggregatorService.java`)
**核心代码示范：**
```java
public Mono<DashboardVO> getDashboardData(String userId) {
    Mono<UserProfile> userMono = remoteDataClient.getUserProfile(userId);
    // Flux可以通过 collectList() 转换为包含完整 List 集合的 Mono
    Mono<List<OrderInfo>> ordersMono = remoteDataClient.getRecentOrders(userId).collectList();
    Mono<Integer> scoreMono = remoteDataClient.getUserScore(userId);

    // 使用 Mono.zip 并行发起多个异步操作，当三个流全部得到数据时返回一个 Tuple 型的集合，最后按我们需要的结构组装
    return Mono.zip(userMono, ordersMono, scoreMono)
            .map(tuple -> {
                DashboardVO vo = new DashboardVO();
                vo.user = tuple.getT1();
                vo.orders = tuple.getT2();
                vo.score = tuple.getT3();
                return vo;
            });
}
```
> **知识点**：`Mono.zip()` 函数可以接收并订阅多个 Publisher，它会**并行执行**它们。这意味着总时间仅取决于耗时最长的那个接口（这里是 订单接口 800ms）。这以极低的学习成本完美替代了过去容易引发死锁和上下文泄漏的 `CompletableFuture/ThreadPoolExecutor`。

#### C: WebFlux 控制器接口 (`DashboardController.java`)
借助于 Spring Boot WebFlux 框架，控制器方法的返回类型直接设为 `Mono<DashboardVO>`。不用手动加任何阻塞处理。一旦引擎中的流计算出最终的 VO 对象，WebFlux 内置的机制就会自动将事件转化为 HTTP Response 响应给前端。

### 4. 运行与验证
启动应用后，访问 `http://localhost:8080/api/dashboard/123` 即可查看返回完整的聚合 JSON，同时可以观察到 `aggregatorCostMs` 的值在 800 毫秒左右，完美验证了异步并网请求的效能提升！
