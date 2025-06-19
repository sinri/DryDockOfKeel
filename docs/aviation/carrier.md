# Aviation Carrier Package

航母（AircraftCarrier）包提供了基于 Vert.x 的微服务应用程序启动框架。该包实现了一个完整的应用程序生命周期管理系统，包含健康监控、队列处理、定时任务和HTTP服务功能。

## 包概览

| 类名 | 描述 |
|------|------|
| `AircraftCarrierDeck` | 基础抽象类，提供命令行参数解析和程序入口点 |
| `AircraftCarrier` | 核心实现类，提供完整的微服务启动和管理功能 |

## 核心组件

### 1. AircraftCarrierDeck

**位置**: `src/main/java/io/github/sinri/drydock/aviation/carrier/AircraftCarrierDeck.java`

基础抽象类，实现了 `CommonUnit` 接口，提供：

#### 主要功能
- **命令行解析**: 基于 Vert.x CLI API 的命令行参数处理
- **程序入口**: 通过 `launch(String[] args)` 方法启动应用程序
- **日志系统**: 内置 `KeelEventLog` 日志记录器
- **问题记录中心**: 提供 `KeelIssueRecordCenter` 支持

#### 关键方法
```java
// 程序启动入口
public final void launch(String[] args)

// 抽象方法，需要子类实现
protected abstract String buildCliName()
protected abstract String buildCliDescription()
protected abstract void runWithCommandLine(CommandLine commandLine)

// 可选重写的方法
protected List<Option> buildCliOptions()
protected List<Argument> buildCliArguments()
```

### 2. AircraftCarrier

**位置**: `src/main/java/io/github/sinri/drydock/aviation/carrier/AircraftCarrier.java`

核心实现类，继承自 `AircraftCarrierDeck` 并实现 `HealthMonitorMixin` 接口。

#### 内置组件

##### 飞机组件 (Aircraft Components)
- **Bomber (轰炸机)**: 定时任务执行器，基于 Sundial 实现
- **Drone (无人机)**: 队列任务处理器，基于 Queue 实现  
- **Fighter (战斗机)**: HTTP服务提供器

##### 监控组件
- **HealthMonitor**: 健康状况监控
- **MetricRecorder**: 指标记录器，默认使用阿里云 SLS

#### 命令行选项

| 选项 | 类型 | 描述 |
|------|------|------|
| `--disableQueue` | Flag | 禁用队列组件 |
| `--disableSundial` | Flag | 禁用定时任务组件 |
| `--disableReceptionist` | Flag | 禁用HTTP服务组件 |
| `--receptionistPort` | Value | 指定HTTP服务端口 |

#### 启动流程

应用程序按以下顺序初始化：

1. **本地配置加载** - 从 `config.properties` 文件加载配置
2. **Vert.x 初始化** - 根据 `buildVertxOptions()` 初始化 Vert.x 实例
3. **远程配置加载** - 异步加载远程配置
4. **日志系统配置** - 配置问题记录中心和日志旁路
5. **指标记录器启动** - 启动阿里云 SLS 指标记录器
6. **健康监控加载** - 初始化健康监控系统
7. **业务准备** - 调用 `prepare()` 方法
8. **组件初始化**:
   - Queue组件 (如果未禁用)
   - Sundial组件 (如果未禁用)  
   - HTTP服务组件 (如果未禁用)
9. **就绪状态** - 调用 `ready()` 方法

#### 抽象方法

子类必须实现以下抽象方法：

```java
// 构造器方法
protected abstract Bomber constructBomber()
protected abstract Drone constructDrone()
protected abstract Fighter constructFighter(@Nullable Integer port)

// 配置方法
protected abstract VertxOptions buildVertxOptions(CommandLine commandLine)
protected abstract Future<Void> loadRemoteConfiguration(CommandLine commandLine)

// 生命周期方法
protected abstract Future<Void> prepare(CommandLine commandLine)
protected abstract Future<Void> ready(CommandLine commandLine)

// CLI方法
protected abstract String buildCliName()
protected abstract String buildCliDescription()
```

#### 可重写方法

```java
// 配置加载
protected void loadLocalConfiguration(CommandLine commandLine)

// 问题记录中心构建
protected KeelIssueRecordCenter buildIssueRecordCenter()

// 健康监控构建
public HealthMonitor<?> buildHealthMonitor()
```

## 使用示例

### 基本实现

```java
public class MyApplication extends AircraftCarrier {
    
    @Override
    protected String buildCliName() {
        return "my-app";
    }
    
    @Override
    protected String buildCliDescription() {
        return "My microservice application";
    }
    
    @Override
    protected VertxOptions buildVertxOptions(CommandLine commandLine) {
        return new VertxOptions();
    }
    
    @Override
    protected Future<Void> loadRemoteConfiguration(CommandLine commandLine) {
        // 实现远程配置加载逻辑
        return Future.succeededFuture();
    }
    
    @Override
    protected Bomber constructBomber() {
        return new MyBomber();
    }
    
    @Override
    protected Drone constructDrone() {
        return new MyDrone();
    }
    
    @Override
    protected Fighter constructFighter(Integer port) {
        return new MyFighter(port);
    }
    
    @Override
    protected Future<Void> prepare(CommandLine commandLine) {
        // 业务准备逻辑
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> ready(CommandLine commandLine) {
        // 应用就绪后的逻辑
        return Future.succeededFuture();
    }
    
    public static void main(String[] args) {
        new MyApplication().launch(args);
    }
}
```

### 启动命令示例

```bash
# 启动完整服务
java -jar my-app.jar

# 禁用队列组件
java -jar my-app.jar --disableQueue

# 禁用定时任务组件
java -jar my-app.jar --disableSundial

# 禁用HTTP服务
java -jar my-app.jar --disableReceptionist

# 指定HTTP服务端口
java -jar my-app.jar --receptionistPort=9090

# 组合使用
java -jar my-app.jar --disableQueue --receptionistPort=8080
```

## 依赖关系

该包依赖以下主要组件：

- **Keel Framework**: 核心基础设施
- **Vert.x**: 异步应用平台
- **Aircraft Components**: 
  - `Bomber` (Sundial定时任务)
  - `Drone` (Queue队列处理)
  - `Fighter` (HTTP服务)
- **Health Monitor**: 健康监控系统
- **Logging System**: 日志和指标记录
- **Aliyun SLS**: 阿里云日志服务适配器

## 设计模式

- **模板方法模式**: `AircraftCarrier` 定义启动流程模板，子类实现具体步骤
- **工厂方法模式**: 通过 `construct*()` 方法创建组件实例
- **Mixin模式**: 通过 `HealthMonitorMixin` 提供健康监控能力
- **命令模式**: 基于 Vert.x CLI 的命令行参数处理

## 配置说明

### 本地配置文件

默认读取 `config.properties` 文件，可通过重写 `loadLocalConfiguration()` 方法自定义。

### 远程配置

通过实现 `loadRemoteConfiguration()` 方法加载远程配置，支持异步加载。

### 健康监控配置

支持两种健康监控模式：
- **基于问题记录**: `HealthMonitorWithIssueRecorder`
- **基于指标记录**: `HealthMonitorWithMetricRecorder`

## 注意事项

1. **生命周期管理**: 确保在 `prepare()` 和 `ready()` 方法中正确处理异步操作
2. **异常处理**: 启动过程中的异常会导致程序退出 (System.exit(1))
3. **组件依赖**: 各组件之间存在启动顺序依赖，不可随意调整
4. **资源管理**: 注意及时释放资源，避免内存泄漏
5. **集群模式**: 当前版本未完全支持集群模式（代码中有 TODO 注释）

## 版本历史

- **1.5.0**: 初始版本，提供基础航母框架
- **1.5.2**: 增加组件获取方法和配置检查方法
- **1.5.11**: 移除异常捕获，允许异常向上传播

该包提供了一个强大而灵活的微服务应用程序框架，通过模块化设计支持各种业务场景的快速开发和部署。
