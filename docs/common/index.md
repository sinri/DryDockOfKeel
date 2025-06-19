# DryDock Common 模块文档

## 概述

`io.github.sinri.drydock.common` 包是 DryDock 项目的核心公共模块，提供了通用的基础设施组件和混入接口。该模块为所有其他模块提供了统一的基础能力，包括日志记录、健康监控、HTTP服务器、队列处理、定时任务等核心功能。

## 核心接口

### CommonUnit

`CommonUnit` 是整个模块的基础接口，定义了所有单元都应该具备的通用能力：

- **日志记录中心**: 提供 `KeelIssueRecordCenter` 用于统一的日志管理
- **问题记录器**: 支持创建不同主题的 `KeelIssueRecorder` 实例
- **事件日志器**: 提供 `KeelEventLogger` 用于事件记录（已标记为弃用）
- **指标记录器**: 提供 `KeelMetricRecorder` 用于性能指标记录

## 混入接口 (Mixin Interfaces)

### HttpServerMixin

HTTP服务器混入接口，为实现类提供Web服务能力：

- **服务器构建**: `buildHttpServer()` 方法创建 `KeelHttpServer` 实例
- **端口配置**: `configuredHttpServerPort()` 配置监听端口（默认8080）
- **路由配置**: `configureHttpServerRoutes()` 配置路由规则
- **生命周期管理**: 支持启动前回调和停止声明
- **异步加载**: `loadHttpServer()` 异步启动HTTP服务

### QueueMixin

队列处理混入接口，提供异步任务队列功能：

- **队列构建**: `buildQueue()` 创建 `KeelQueue` 实例
- **工作池管理**: 支持配置工作线程池大小
- **信号读取**: `readSignal()` 读取队列信号
- **任务查找**: `seekNextTask()` 查找下一个待处理任务
- **异步加载**: `loadQueue()` 以Worker线程模式部署队列服务

### SundialMixin

定时任务混入接口，提供日晷（Sundial）定时调度功能：

- **日晷构建**: `buildSundial()` 创建 `KeelSundial` 实例
- **计划获取**: `fetchSundialPlans()` 异步获取定时计划
- **异步加载**: `loadSundial()` 以Worker线程模式部署定时服务

### HealthMonitorMixin

健康监控混入接口，提供系统健康状态监控：

- **监控器构建**: `buildHealthMonitor()` 创建健康监控实例
- **异步加载**: `loadHealthMonitor()` 以Worker线程模式部署监控服务

## 子模块

### health 子包

健康监控相关组件：

- **HealthMonitor**: 抽象的健康监控基类，提供系统运行时监控
- **HealthMonitorWithIssueRecorder**: 基于问题记录器的健康监控实现
- **HealthMonitorWithMetricRecorder**: 基于指标记录器的健康监控实现
- **HealthMonitorMixin**: 健康监控混入接口

特性：
- 支持自定义监控间隔（默认60秒）
- 集成运行时监控器 `KeelRuntimeMonitor`
- 提供可扩展的监控项和记录处理

参见 [健康监控](./health.md)。

### baiyatan 子包

远程配置加载组件：

- **BaiyatanConfigurationLoader**: 基于 Kumori-Baiyatan 协议的远程配置加载器

功能：
- 支持从远程服务加载配置文件
- 使用MD5校验确保安全性
- 支持多层级作用域配置
- 自动集成到 Keel 配置系统

### logging 子包

日志记录相关组件：

- **DryDockLogTopics**: 定义标准日志主题常量
  - `TopicDryDock`: DryDock主题
  - `TopicSundial`: 日晷主题  
  - `TopicHttpServer`: HTTP服务器主题

参见 [日志记录](./logging.md)。

#### logging.issue 子包

问题记录相关：
- **HealthMonitorIssueRecord**: 健康监控问题记录，包含快照属性

#### logging.metric 子包

指标记录相关组件

#### logging.adapter 子包

日志适配器实现：
- **AliyunSLSIssueAdapterImpl**: 阿里云SLS问题适配器
- **AliyunSLSMetricRecorder**: 阿里云SLS指标记录器

## 设计模式

### 混入模式 (Mixin Pattern)

该模块大量使用混入模式，通过接口提供可组合的功能模块：

```java
public class MyService implements CommonUnit, HttpServerMixin, QueueMixin {
    // 自动获得HTTP服务器和队列处理能力
}
```

### 模板方法模式

抽象类如 `HealthMonitor` 使用模板方法模式，定义算法框架，允许子类自定义具体实现：

- `prepare()`: 初始化准备
- `createDraft()`: 创建草稿对象
- `moreMonitorItems()`: 添加更多监控项
- `handleRecord()`: 处理记录

## 集成与依赖

该模块依赖于以下核心库：
- **Keel**: 提供基础的垂直化、配置、日志等能力
- **Vert.x**: 提供异步编程和HTTP服务器支持
- **javax.annotation**: 提供注解支持

## 使用示例

### 创建带有多种能力的服务

```java
public class MyApplicationService implements CommonUnit, 
                                           HttpServerMixin, 
                                           QueueMixin, 
                                           SundialMixin,
                                           HealthMonitorMixin {
    
    @Override
    public void configureHttpServerRoutes(Router router, KeelIssueRecorder<KeelEventLog> logger) {
        router.get("/health").handler(ctx -> ctx.response().end("OK"));
    }
    
    @Override
    public Future<KeelQueueTask> seekNextTask(KeelIssueRecorder<QueueManageIssueRecord> logger) {
        // 实现任务查找逻辑
        return Future.succeededFuture(null);
    }
    
    @Override
    public Future<Collection<KeelSundialPlan>> fetchSundialPlans(KeelIssueRecorder<SundialIssueRecord> logger) {
        // 实现定时计划获取逻辑
        return Future.succeededFuture(Collections.emptyList());
    }
}
```

## 版本历史

- **1.0.0**: 初始版本，提供基础的CommonUnit接口
- **1.1.0**: 增加QueueMixin和SundialMixin
- **1.3.4**: 优化IssueRecordCenter集成
- **1.4.0**: 重构HealthMonitor为抽象类
- **1.4.9**: 增加BaiyatanConfigurationLoader
- **2.0.3**: 更新日志记录接口，标记部分方法为弃用
- **2.0.4**: 增加更多日志记录器参数支持
- **2.0.5**: 优化队列处理接口

## 注意事项

1. **生命周期管理**: 所有混入接口都提供了异步的 `load*()` 方法，应该在应用启动时调用
2. **线程模型**: 队列和定时任务默认使用Worker线程模型，HTTP服务器使用事件循环线程
3. **配置管理**: BaiyatanConfigurationLoader需要配置相应的Kumori服务参数
4. **日志集成**: 所有组件都集成了统一的日志记录体系，支持不同的适配器实现
