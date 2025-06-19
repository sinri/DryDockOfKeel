# 健康监控模块 (Health Monitor Module)

## 概述

健康监控模块提供了应用程序运行时健康状态的监控功能，包括系统资源使用情况、JVM性能指标、垃圾回收统计等信息的收集和记录。该模块基于Vert.x和Keel框架构建，支持两种监控模式：问题记录模式和指标记录模式。

## 核心组件

### 1. HealthMonitor (抽象基类)

`HealthMonitor<X>` 是健康监控的抽象基类，继承自 `KeelVerticleImplPure`。

#### 关键特性
- **泛型设计**: 支持不同类型的数据记录格式
- **周期性监控**: 默认60秒间隔进行系统监控
- **可扩展**: 通过抽象方法支持自定义监控项和处理逻辑

#### 核心方法
```java
protected abstract void prepare();                                    // 初始化准备工作
protected abstract void moreMonitorItems(@Nonnull final X draft);    // 添加自定义监控项
protected abstract X createDraft();                                   // 创建数据草稿对象
protected abstract void handleRecord(@Nonnull MonitorSnapshot monitorSnapshot, @Nonnull X moreDraft); // 处理监控记录
protected long interval();                                           // 监控间隔(默认60秒)
```

### 2. HealthMonitorWithIssueRecorder (问题记录实现)

基于问题日志的健康监控实现，当系统指标超过阈值时记录警告或错误日志。

#### 核心特性
- **数据格式**: 使用 `JsonObject` 存储监控数据
- **阈值监控**: 
  - CPU使用率 ≥ 50% 或堆内存使用率 ≥ 50% → WARNING级别
  - CPU使用率 ≥ 75% 或堆内存使用率 ≥ 75% 或发生Major GC → ERROR级别
- **监控内容**:
  - 应用存活时间 (survived)
  - GC统计信息 (gc)
  - CPU时间统计 (cpu_time)
  - JVM内存统计 (jvm_memory_stat)

#### 告警策略
```java
// WARNING 级别触发条件
CPU使用率 >= 50% || 堆内存使用率 >= 50%

// ERROR 级别触发条件  
CPU使用率 >= 75% || 堆内存使用率 >= 75% || Major GC次数 > 0
```

### 3. HealthMonitorWithMetricRecorder (指标记录实现)

基于指标数据的健康监控实现，定期收集和记录系统性能指标。

#### 核心特性
- **数据格式**: 使用 `List<HealthMonitorMetricRecord>` 存储指标数据
- **高频监控**: 默认10秒间隔进行指标收集
- **丰富指标**: 收集10种不同类型的系统和JVM指标

#### 收集的指标类型
1. **存活时间** (`survived`): 应用运行时长(毫秒)
2. **硬件内存使用率** (`hardware_memory_usage`): 物理内存使用百分比
3. **JVM内存使用率** (`jvm_memory_usage`): 堆内存使用百分比  
4. **JVM堆内存使用量** (`jvm_heap_memory_used_bytes`): 堆内存使用字节数
5. **JVM非堆内存使用量** (`jvm_non_heap_memory_used_bytes`): 非堆内存使用字节数
6. **CPU使用率** (`cpu_usage`): CPU使用百分比
7. **Major GC次数** (`major_gc_count`): Major垃圾回收次数
8. **Major GC时间** (`major_gc_time`): Major垃圾回收耗时
9. **Minor GC次数** (`minor_gc_count`): Minor垃圾回收次数  
10. **Minor GC时间** (`minor_gc_time`): Minor垃圾回收耗时

### 4. HealthMonitorMixin (混入接口)

提供便捷的健康监控集成接口，实现了CommonUnit接口。

#### 主要功能
```java
// 构建默认的健康监控器(问题记录模式)
HealthMonitor<?> buildHealthMonitor()

// 加载并部署健康监控器  
Future<String> loadHealthMonitor()
```

#### 部署配置
- **线程模型**: WORKER线程模型
- **默认实现**: 使用HealthMonitorWithIssueRecorder

## 数据模型

### HealthMonitorIssueRecord
问题记录数据模型，扩展了 `KeelIssueRecord`。

```java
public static final String TopicHealthMonitor = "HealthMonitor";
public static final String AttributeSnapshot = "snapshot";

// 记录监控快照数据
HealthMonitorIssueRecord snapshot(@Nonnull JsonObject snapshot)
```

### HealthMonitorMetricRecord  
指标记录数据模型，扩展了 `KeelMetricRecord`。

提供静态工厂方法创建各种类型的指标记录：
```java
HealthMonitorMetricRecord.asSurvived(long value)
HealthMonitorMetricRecord.asCpuUsage(double value)  
HealthMonitorMetricRecord.asJvmMemoryUsage(double value)
// ... 等等
```

## 使用方式

### 1. 基本集成 (推荐)

实现 `HealthMonitorMixin` 接口：

```java
public class MyApplication implements HealthMonitorMixin {
    
    public void start() {
        // 加载健康监控
        loadHealthMonitor()
            .onSuccess(deploymentId -> {
                logger.info("健康监控启动成功: " + deploymentId);
            })
            .onFailure(error -> {
                logger.error("健康监控启动失败", error);
            });
    }
}
```

### 2. 自定义监控实现

继承 `HealthMonitor` 实现自定义监控逻辑：

```java
public class CustomHealthMonitor extends HealthMonitor<CustomData> {
    
    @Override
    protected void prepare() {
        // 初始化自定义组件
    }
    
    @Override  
    protected CustomData createDraft() {
        return new CustomData();
    }
    
    @Override
    protected void moreMonitorItems(@Nonnull CustomData draft) {
        // 添加自定义监控项
        draft.setCustomMetric(getCustomMetricValue());
    }
    
    @Override
    protected void handleRecord(@Nonnull MonitorSnapshot snapshot, @Nonnull CustomData data) {
        // 处理监控数据
    }
    
    @Override
    protected long interval() {
        return 30_000L; // 自定义监控间隔30秒
    }
}
```

### 3. 指标监控模式

使用指标记录器进行详细的性能监控：

```java
KeelMetricRecorder metricRecorder = // 初始化指标记录器
HealthMonitor<List<HealthMonitorMetricRecord>> monitor = 
    new HealthMonitorWithMetricRecorder(metricRecorder);

// 部署监控器
monitor.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
```

## 架构设计

### 设计模式
1. **模板方法模式**: `HealthMonitor` 抽象类定义监控流程框架
2. **策略模式**: 不同的监控实现(`WithIssueRecorder` vs `WithMetricRecorder`)
3. **混入模式**: `HealthMonitorMixin` 提供便捷集成
4. **工厂模式**: 静态工厂方法创建各种指标记录

### 扩展点
- **自定义监控项**: 通过 `moreMonitorItems()` 添加业务特定监控
- **自定义处理逻辑**: 通过 `handleRecord()` 实现特定的数据处理
- **自定义监控间隔**: 通过 `interval()` 调整监控频率
- **自定义数据格式**: 通过泛型 `<X>` 支持不同的数据结构

## 注意事项

1. **性能影响**: 健康监控会占用一定的系统资源，建议根据实际需求调整监控间隔
2. **存储考虑**: 指标记录模式会产生大量数据，需要考虑存储和清理策略  
3. **线程模型**: 默认使用WORKER线程模型，避免阻塞主EventLoop
4. **告警阈值**: 问题记录模式的阈值可能需要根据具体应用场景调整
5. **依赖关系**: 模块依赖Keel框架的运行时监控和日志记录功能

## 版本历史

- **1.0.0**: 初始版本
- **1.4.0**: 重构为抽象类，提供两种实现方式
- **1.4.2**: 添加可配置的监控间隔
