# Naval Melee Package Documentation

## 概述

`io.github.sinri.drydock.naval.melee` 包包含了一系列近战型海军舰船类，这些类构成了一个完整的继承层次结构，每个层级都在前一个基础上增加了新的功能和能力。

## 继承层次结构

```
Warship (base)
    ↓
  Galley (桨帆船)
    ↓
  Caravel (轻快帆船)
    ↓
  Ironclad (铁甲舰)
    ↓
  Destroyer (驱逐舰) [已弃用]
```

## 舰船类详细说明

### 1. Galley (桨帆船)

**位置**: `io.github.sinri.drydock.naval.melee.Galley`

桨帆船是最基础的舰船实现，提供了最大的自定义灵活性。

**主要特点**:
- 继承自 `Warship` 基类
- 完全可定制的 `VertxOptions` 初始化
- 支持独立或集群模式
- 使用纯 STDOUT 日志记录
- 基础的 Keel 初始化

**核心方法**:
- `buildVertxOptions()`: 构建 Vertx 配置选项
- `buildIssueRecordCenter()`: 构建问题记录中心（使用输出中心）
- `launchAsGalley()`: 抽象方法，用于在桨帆船基础上安装模块

**适用场景**: 需要完全自定义配置的简单应用场景

### 2. Caravel (轻快帆船)

**位置**: `io.github.sinri.drydock.naval.melee.Caravel`

轻快帆船在桨帆船基础上增加了配置管理、日志服务和健康监控功能。

**主要特点**:
- 继承自 `Galley`
- 实现 `HealthMonitorMixin` 接口
- 支持阿里云 SLS 日志服务
- 自动加载 `config.properties` 配置文件
- 集成健康监控模块
- 支持指标记录

**核心方法**:
- `loadLocalConfiguration()`: 加载本地配置文件
- `prepareDataSources()`: 抽象方法，准备数据源（如 MySQL）
- `launchAsCaravel()`: 抽象方法，为轻快帆船加载模块
- `buildIssueRecordCenter()`: 构建阿里云 SLS 问题记录中心
- `getMetricRecorder()`: 获取指标记录器

**启动流程**:
1. 设置航海日志共享
2. 启动指标记录器
3. 准备数据源
4. 加载健康检查模块
5. 执行自定义启动逻辑

**适用场景**: 需要日志服务、健康监控和数据源管理的应用

### 3. Ironclad (铁甲舰)

**位置**: `io.github.sinri.drydock.naval.melee.Ironclad`

铁甲舰在轻快帆船基础上增加了 HTTP 服务器功能，是支持横向弹性扩展的 HTTP 服务应用的核心。

**主要特点**:
- 继承自 `Caravel`
- 实现 `HttpServerMixin` 接口
- 支持 HTTP 服务器（主炮）
- 适用于 SAE APP 环境
- 支持横向弹性扩展
- 包含服务器停止控制机制

**核心方法**:
- `launchAsIronclad()`: 抽象方法，加载煤炭（初始化资源）
- `stopServer()`: 停止服务器
- `isToStopServer()`: 检查是否需要停止服务器

**启动流程**:
1. 执行铁甲舰初始化（加载煤炭）
2. 加载 HTTP 服务器
3. 完成部署

**适用场景**: 需要 HTTP 服务的 Web 应用和 API 服务

### 4. Destroyer (驱逐舰) [已弃用]

**位置**: `io.github.sinri.drydock.naval.melee.Destroyer`

**状态**: `@Deprecated(since = "2.0.0")`

驱逐舰在铁甲舰基础上增加了异步服务支持，包括定时任务和队列服务。

**主要特点**:
- 继承自 `Ironclad`
- 实现 `SundialMixin` 和 `QueueMixin` 接口
- 支持 Sundial（定时任务）
- 支持队列服务
- 支持 Funnel（漏斗）功能
- 为单节点服务器设计，不支持横向扩展

**核心方法**:
- `loadRemoteConfiguration()`: 对于驱逐舰，配置文件可以打包
- `launchAsDestroyer()`: 抽象方法，驱逐舰特定的启动逻辑

**启动流程**:
1. 加载 Sundial（定时任务服务）
2. 加载队列服务
3. 执行驱逐舰特定启动逻辑

**弃用原因**: 在 2.0.0 版本中，推荐使用 AircraftCarrier 系列替代

## 使用建议

### 选择合适的舰船类型

1. **Galley**: 如果需要完全自定义的简单应用
2. **Caravel**: 如果需要日志服务和健康监控
3. **Ironclad**: 如果需要 HTTP 服务功能
4. **Destroyer**: 已弃用，建议迁移到 AircraftCarrier 系列

### 实现步骤

1. 选择合适的基类
2. 实现必要的抽象方法
3. 根据需要重写配置方法
4. 在启动方法中初始化应用特定的组件

### 注意事项

- Destroyer 类已在 2.0.0 版本中弃用
- 所有类都支持完整的生命周期管理
- 健康监控和日志服务是可选的，取决于选择的基类
- HTTP 服务器功能从 Ironclad 开始提供

## 相关接口

- `HealthMonitorMixin`: 健康监控功能
- `HttpServerMixin`: HTTP 服务器功能  
- `SundialMixin`: 定时任务功能
- `QueueMixin`: 队列服务功能

## 版本历史

- **1.0.0**: 初始版本，包含基础舰船类
- **1.1.0**: 改进继承结构，增强 Warship 基类
- **2.0.0**: 弃用 Destroyer，推荐使用 AircraftCarrier 系列
