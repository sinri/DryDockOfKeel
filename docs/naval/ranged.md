# Naval Ranged Package

## 概述

`io.github.sinri.drydock.naval.ranged` 包提供了远程作战舰船的实现，包含了具备远程攻击能力的海军单位。该包中的舰船类型专门设计用于处理队列任务、定时任务和健康监控，适用于单节点服务架构。

## 包结构

```
naval/ranged/
├── Frigate.java          # 护卫舰 - 队列和定时任务处理器
├── Quadrireme.java       # 四段帆船 - 基础远程舰船
└── package-info.java     # 包信息
```

## 类层次结构

```
Warship (base)
└── Quadrireme
    └── Frigate
```

## 核心类详解

### Quadrireme (四段帆船)

四段帆船是远程作战舰船的基础类，提供了基本的配置加载和启动能力。

**主要特性：**
- 继承自 `Warship` 基类
- 本地配置加载（从 `config.properties` 文件）
- 简化的远程配置加载（配置文件可打包）
- 标准输出日志记录中心
- 64线程工作池配置

**关键方法：**
- `loadLocalConfiguration()`: 加载本地配置文件
- `loadRemoteConfiguration()`: 加载远程配置（默认为空实现）
- `buildVertxOptions()`: 构建Vertx配置，设置64个工作线程
- `buildIssueRecordCenter()`: 构建标准输出日志记录中心
- `launchAsQuadrireme()`: 抽象启动方法，由子类实现

### Frigate (护卫舰) - 已弃用

> ⚠️ **弃用警告**: 自2.0.0版本起，Frigate类已被标记为弃用，建议使用AircraftCarrier系列替代。

护卫舰是专门用于处理队列和定时任务的舰船，不提供HTTP服务，为单节点服务设计。

**主要特性：**
- 继承自 `Quadrireme`
- 实现了多个Mixin接口：
  - `QueueMixin`: 队列处理能力
  - `SundialMixin`: 定时任务能力
  - `HealthMonitorMixin`: 健康监控能力
- 集成阿里云SLS日志适配器
- 支持指标记录功能

**核心功能：**

1. **数据源准备**
   - `prepareDataSources()`: 抽象方法，用于准备MySQL等数据源

2. **日志记录**
   - 使用 `AliyunSLSIssueAdapterImpl` 构建问题记录中心
   - 支持 `AliyunSLSMetricRecorder` 进行指标记录

3. **启动流程**
   ```java
   launchAsQuadrireme() -> 
   ├── 启动指标记录器
   ├── 准备数据源
   ├── 并行加载：健康监控、定时任务、队列
   └── 执行自定义启动逻辑 (launchAsFrigate)
   ```

**抽象方法：**
- `prepareDataSources()`: 准备数据源连接
- `launchAsFrigate()`: 护卫舰特定的启动逻辑

## Mixin接口集成

### QueueMixin - 队列处理
- 提供异步队列任务处理能力
- 支持可配置的工作线程池
- 实现信号读取和任务查找机制

### SundialMixin - 定时任务
- 提供定时任务调度功能
- 支持动态计划获取
- 基于Vertx的异步执行模型

### HealthMonitorMixin - 健康监控
- 提供应用健康状态监控
- 集成问题记录功能
- 支持异步部署和监控

## 版本历史

- **1.0.1**: 初始版本
- **1.1.0**: 功能增强
- **1.2.0**: 添加数据源准备功能
- **1.3.4**: 改进问题记录中心构建
- **2.0.0**: 标记为弃用，推荐使用AircraftCarrier系列
- **3.2.5**: 添加指标记录器支持

## 使用建议

1. **新项目**: 不建议使用此包中的类，应使用AircraftCarrier系列
2. **现有项目**: 计划迁移到新的架构
3. **单节点服务**: 如果必须使用，Quadrireme提供了基础功能
4. **队列处理**: 考虑直接使用QueueMixin而非Frigate

## 依赖关系

- **Keel框架**: 核心依赖，提供Vertx集成和日志功能
- **阿里云SLS**: 日志和指标记录适配器
- **Vertx**: 异步应用框架
- **Common包**: 提供Mixin接口和通用功能

## 配置要求

- `config.properties`: 本地配置文件
- 阿里云SLS配置: 用于日志和指标记录
- 数据源配置: MySQL等数据库连接配置（如使用Frigate）
