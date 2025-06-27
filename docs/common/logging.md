# DryDock Logging Package Documentation

## 概述

DryDock 项目的日志包 (`io.github.sinri.drydock.common.logging`) 提供了完整的日志记录解决方案，支持问题记录 (Issue Recording) 和指标记录 (Metric Recording)。该包基于 Keel 框架构建，提供了与阿里云 SLS (Simple Log Service) 的集成支持。

## 包结构

```
logging/
├── DryDockLogTopics.java           # 日志主题定义
├── issue/                          # 问题记录相关
│   ├── HealthMonitorIssueRecord.java    # 健康监控问题记录
│   └── package-info.java
├── metric/                         # 指标记录相关
│   ├── HealthMonitorMetricRecord.java    # 健康监控指标记录
│   └── package-info.java
└── package-info.java

注：阿里云SLS适配器实现位于 plugin/aliyun/sls/writer/ 目录下：
├── AliyunSLSIssueAdapterImpl.java    # 阿里云SLS问题日志适配器
├── AliyunSLSMetricRecorder.java      # 阿里云SLS指标记录器
├── AliyunSlsConfigElement.java       # 阿里云SLS配置元素
└── AliyunSLSLogPutter.java          # 阿里云SLS日志推送器
```

## 核心组件

### 1. DryDockLogTopics

定义系统中使用的日志主题常量：

```java
public class DryDockLogTopics {
    public static final String TopicDryDock = "DryDock";
    public static final String TopicSundial = "Sundial";
    public static final String TopicHttpServer = "HttpServer";
}
```

**用途：**
- `TopicDryDock`：系统核心日志主题
- `TopicSundial`：定时任务相关日志主题
- `TopicHttpServer`：HTTP服务器相关日志主题

### 2. 阿里云SLS适配器 (位于 plugin/aliyun/sls/writer/ 包)

#### AliyunSLSIssueAdapterImpl

阿里云 SLS 问题日志适配器的实现，继承自 `AliyunSLSIssueAdapter`（来自Keel框架）。

**主要功能：**
- 将问题记录异步发送到阿里云 SLS
- 支持配置禁用，禁用时回退到标准输出
- 支持 IP 地址模板替换
- 自动处理连接管理

**配置路径：** `aliyun.sls`

**配置项：**
```properties
aliyun.sls.disabled=NO/YES
aliyun.sls.project=your-project
aliyun.sls.logstore=your-logstore
aliyun.sls.endpoint=your-endpoint
aliyun.sls.source=[IP]  # 支持模板，[IP]会被替换为本机IP
aliyun.sls.accessKeyId=your-access-key-id
aliyun.sls.accessKeySecret=your-access-key-secret
```

**核心方法：**
- `handleIssueRecordsForTopic()`: 处理特定主题的问题记录
- `buildProducer()`: 构建SLS日志推送器
- `isDisabled()`: 检查是否禁用

#### AliyunSLSMetricRecorder

阿里云 SLS 指标记录器，继承自 `KeelMetricRecorder`（来自Keel框架）。

**主要功能：**
- 将指标数据发送到阿里云 SLS
- 支持标签排序和格式化
- 自动添加时间戳和源信息
- 支持配置禁用，禁用时回退到调试日志

**配置路径：** `aliyun.sls_metric`

**配置项：**
```properties
aliyun.sls_metric.disabled=NO/YES
aliyun.sls_metric.project=your-project
aliyun.sls_metric.logstore=your-logstore
aliyun.sls_metric.endpoint=your-endpoint
aliyun.sls_metric.source=[IP]
aliyun.sls_metric.accessKeyId=your-access-key-id
aliyun.sls_metric.accessKeySecret=your-access-key-secret
```

### 3. Issue 层

#### HealthMonitorIssueRecord

健康监控问题记录类，扩展了 `KeelIssueRecord`。

**关键属性：**
- `TopicHealthMonitor`: 健康监控主题常量
- `AttributeSnapshot`: 快照属性名

**主要方法：**
- `snapshot(JsonObject)`: 添加监控快照数据

**使用示例：**
```java
HealthMonitorIssueRecord record = new HealthMonitorIssueRecord()
    .snapshot(monitorSnapshot)
    .level(KeelLogLevel.WARNING);
```

### 4. Metric 层

#### HealthMonitorMetricRecord

健康监控指标记录类，扩展了 `KeelMetricRecord`。

**关键属性：**
- `TopicHealthMonitor`: 健康监控主题常量

**预定义指标和工厂方法：**
- `survived`: 存活时间 → `asSurvived(long value)`
- `minor_gc_count`: Minor GC 次数 → `asMinorGCCount(long value)`
- `minor_gc_time`: Minor GC 时间 → `asMinorGCTime(long value)`
- `major_gc_count`: Major GC 次数 → `asMajorGCCount(long value)`
- `major_gc_time`: Major GC 时间 → `asMajorGCTime(long value)`
- `cpu_usage`: CPU 使用率 → `asCpuUsage(double value)`
- `hardware_memory_usage`: 硬件内存使用率 → `asHardwareMemoryUsage(double value)`
- `jvm_memory_usage`: JVM 内存使用率 → `asJvmMemoryUsage(double value)`
- `jvm_heap_memory_used_bytes`: JVM 堆内存使用字节数 → `asJvmHeapMemoryUsedBytes(long value)`
- `jvm_non_heap_memory_used_bytes`: JVM 非堆内存使用字节数 → `asJvmNonHeapMemoryUsedBytes(long value)`

**工厂方法示例：**
```java
// 创建各种指标记录
HealthMonitorMetricRecord.asSurvived(upTime);
HealthMonitorMetricRecord.asCpuUsage(0.75);
HealthMonitorMetricRecord.asJvmMemoryUsage(0.60);
HealthMonitorMetricRecord.asMinorGCCount(10);
HealthMonitorMetricRecord.asJvmHeapMemoryUsedBytes(1024 * 1024 * 100L);
```

## 使用模式

### 1. 在 AircraftCarrier 中的集成

```java
public class YourCarrier extends AircraftCarrier {
    @Override
    protected void prepare(CommandLine commandLine) {
        // 日志记录器自动初始化
        // 健康监控自动启动
        return Future.succeededFuture();
    }
}
```

### 2. 在 Caravel 中的使用

```java
public abstract class YourCaravel extends Caravel implements HealthMonitorMixin {
    // 自动获得健康监控能力
    // 自动获得阿里云SLS日志记录能力
}
```

### 3. 自定义健康监控

#### 使用问题记录器
```java
public class HealthMonitorWithIssueRecorder extends HealthMonitor<JsonObject> {
    private final KeelIssueRecorder<HealthMonitorIssueRecord> healthMonitorIssueRecorder;
    
    @Override
    protected void handleRecord(MonitorSnapshot monitorSnapshot, JsonObject moreEvent) {
        this.healthMonitorIssueRecorder.record(t -> {
            var snapshot = new JsonObject()
                .put("survived", System.currentTimeMillis() - startTimestamp)
                .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                .put("jvm_memory_stat", monitorSnapshot.getJvmMemoryResult().toJsonObject());
            
            t.snapshot(snapshot);
            
            // 根据阈值设置日志级别
            double heapUsage = 1.0 * monitorSnapshot.getJvmMemoryResult().getRuntimeHeapUsedBytes() 
                             / monitorSnapshot.getJvmMemoryResult().getRuntimeHeapMaxBytes();
            if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.50 || heapUsage >= 0.50) {
                t.level(KeelLogLevel.WARNING);
            }
            if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.75 || heapUsage >= 0.75) {
                t.level(KeelLogLevel.ERROR);
            }
        });
    }
}
```

#### 使用指标记录器
```java
public class HealthMonitorWithMetricRecorder extends HealthMonitor<List<HealthMonitorMetricRecord>> {
    protected KeelMetricRecorder metricRecorder;
    
    @Override
    protected void handleRecord(MonitorSnapshot monitorSnapshot, List<HealthMonitorMetricRecord> moreMetricRecords) {
        long now = System.currentTimeMillis();
        
        // 记录存活时间
        metricRecorder.recordMetric(
            HealthMonitorMetricRecord.asSurvived(System.currentTimeMillis() - startTimestamp)
                .timestamp(now)
        );
        
        // 记录内存使用率
        metricRecorder.recordMetric(
            HealthMonitorMetricRecord.asHardwareMemoryUsage(
                1.0 * monitorSnapshot.getJvmMemoryResult().getPhysicalUsedBytes() 
                / monitorSnapshot.getJvmMemoryResult().getPhysicalMaxBytes()
            ).timestamp(monitorSnapshot.getJvmMemoryResult().getStatTime())
        );
        
        // 记录CPU使用率
        metricRecorder.recordMetric(
            HealthMonitorMetricRecord.asCpuUsage(monitorSnapshot.getCPUTime().getCpuUsage())
                .timestamp(monitorSnapshot.getCPUTime().getStatTime())
        );
        
        // 记录自定义指标
        moreMetricRecords.forEach(metricRecorder::recordMetric);
    }
}
```

## 配置指南

### 启用阿里云 SLS 日志记录

1. **问题日志配置** (`aliyun.sls`)：
```properties
aliyun.sls.disabled=NO
aliyun.sls.project=my-project
aliyun.sls.logstore=my-logstore
aliyun.sls.endpoint=cn-hangzhou.log.aliyuncs.com
aliyun.sls.source=app-server-[IP]
aliyun.sls.accessKeyId=YOUR_ACCESS_KEY_ID
aliyun.sls.accessKeySecret=YOUR_ACCESS_KEY_SECRET
```

2. **指标日志配置** (`aliyun.sls_metric`)：
```properties
aliyun.sls_metric.disabled=NO
aliyun.sls_metric.project=my-metric-project
aliyun.sls_metric.logstore=my-metric-logstore
aliyun.sls_metric.endpoint=cn-hangzhou.log.aliyuncs.com
aliyun.sls_metric.source=metric-collector-[IP]
aliyun.sls_metric.accessKeyId=YOUR_ACCESS_KEY_ID
aliyun.sls_metric.accessKeySecret=YOUR_ACCESS_KEY_SECRET
```

### 禁用远程日志记录

设置 `disabled=YES` 将回退到本地标准输出：

```properties
aliyun.sls.disabled=YES
aliyun.sls_metric.disabled=YES
```

## 最佳实践

1. **合理设置监控间隔**：默认为60秒，可通过重写 `interval()` 方法调整
2. **使用适当的日志级别**：根据指标阈值动态设置 WARNING 或 ERROR 级别
3. **自定义指标扩展**：通过 `moreMonitorItems()` 方法添加业务特定的监控指标
4. **优雅降级**：当 SLS 不可用时，系统自动回退到标准输出，保证可观测性
5. **IP 模板使用**：在集群环境中使用 `[IP]` 模板来区分不同节点的日志源

## 依赖关系

该日志包依赖于：
- Keel 框架的日志组件（`KeelIssueRecord`、`KeelMetricRecord`、`AliyunSLSIssueAdapter`、`KeelMetricRecorder`等）
- 阿里云 SLS Java SDK（通过 `AliyunSLSLogPutter` 封装）
- Vert.x 异步框架
- JsonObject 序列化支持

## 架构说明

DryDock 的日志架构分为两个主要层次：

1. **核心日志层** (`common.logging`)：定义了日志主题、问题记录和指标记录的基础结构
2. **插件适配层** (`plugin.aliyun.sls.writer`)：提供具体的阿里云SLS集成实现

这种分层设计使得日志系统既保持了核心的简洁性，又提供了灵活的扩展能力。

通过这套日志系统，DryDock 应用可以实现完整的可观测性，支持问题追踪和性能监控。
