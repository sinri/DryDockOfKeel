# Naval Base Package Documentation

## 概述

`io.github.sinri.drydock.naval.base` 包是 DryDock 框架中以"船只航行"为比喻的应用程序基础包，提供了所有应用单元的核心接口和基础实现。该包以海军舰船的概念来抽象程序的生命周期管理和运行框架。

## 包结构

### 核心接口

#### Boat 接口
```java
public interface Boat extends CommonUnit
```

`Boat` 是所有单体应用程序（非集群模式）的基础接口，继承自 `CommonUnit`，以船只的概念定义了应用程序的基本生命周期方法。

**主要方法：**
- `void launch()` - 启动应用程序（比喻为船只启航）
- `void shipwreck(Throwable throwable)` - 异常处理（比喻为海难处理）
- `void sink()` - 关闭应用程序（比喻为船只沉没）

**设计理念：**
- 作为所有单体应用程序的统一接口
- 提供标准的应用程序生命周期管理
- 继承 `CommonUnit` 获得日志记录和事件处理能力

### 抽象基类

#### Warship 抽象类
```java
abstract public class Warship implements Boat
```

`Warship` 是所有应用程序的抽象基类，实现了 `Boat` 接口，以军舰的概念提供了完整的应用程序启动和运行框架。

**核心功能：**

1. **日志系统**
   - 运行日志记录器 (`KeelIssueRecorder<KeelEventLog>`)（比喻为航海日志）
   - 应用级事件日志中心 (`KeelIssueRecordCenter`)

2. **启动流程**
   - 本地配置加载
   - Keel 引擎初始化
   - 远程配置加载
   - 事件通信中心建立
   - 应用程序特定启动逻辑

3. **生命周期管理**
   - 标准化的应用程序启动序列
   - 异常处理和故障恢复
   - 优雅关闭机制

**关键常量：**
- `EXIT_CODE_FOR_KEEL_INIT_FAILED = 1` - Keel 初始化失败退出码
- `EXIT_CODE_FOR_SELF_SINK = 0` - 自沉退出码

**抽象方法（子类必须实现）：**
- `buildIssueRecordCenter()` - 构建事件记录中心
- `buildVertxOptions()` - 构建 Vertx 配置选项
- `loadLocalConfiguration()` - 加载本地配置
- `loadRemoteConfiguration()` - 加载远程配置
- `launchAsWarship()` - 应用程序特定的启动逻辑

**核心方法实现：**

##### launch() - 启动方法（比喻为启航）
完整的应用程序启动流程：
1. 记录启动时间
2. 加载本地配置
3. 构建并初始化 Vertx 选项
4. 初始化 Keel 引擎
5. 加载远程配置
6. 构建事件记录中心
7. 执行应用程序特定启动逻辑
8. 记录启动完成时间

##### shipwreck(Throwable) - 异常处理（比喻为海难处理）
- 记录异常信息到运行日志
- 使用故障退出码终止程序

##### sink() - 关闭方法（比喻为自沉）
- 记录关闭日志
- 优雅关闭 Keel 引擎
- 使用正常退出码终止程序

## 设计模式

### 模板方法模式
`Warship` 类使用模板方法模式定义了应用程序的标准启动流程，子类通过实现抽象方法来定制具体行为。

### 依赖注入
通过抽象方法允许子类注入特定的配置和组件，如事件记录中心和 Vertx 选项。

## 依赖关系

### 外部依赖
- **Keel 框架**: 提供底层引擎和日志功能
- **Vert.x**: 异步应用平台
- **DryDock Common**: 通用组件和日志主题

### 内部依赖
- `CommonUnit`: 提供通用单位接口
- `DryDockLogTopics`: 日志主题定义

## 使用示例

```java
public class MyWarship extends Warship {
    
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        // 构建自定义事件记录中心
        return KeelIssueRecordCenter.outputCenter();
    }
    
    @Override
    public VertxOptions buildVertxOptions() {
        // 构建 Vertx 配置
        return new VertxOptions().setWorkerPoolSize(10);
    }
    
    @Override
    protected void loadLocalConfiguration() {
        // 加载本地配置文件
    }
    
    @Override
    protected Future<Void> loadRemoteConfiguration() {
        // 异步加载远程配置
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> launchAsWarship() {
        // 启动应用程序特定功能
        return Future.succeededFuture();
    }
}
```

## 版本历史

- **1.1.0**: 初始版本，定义基础接口和抽象类
- **1.3.4**: 增加事件记录中心支持
- **1.4.18**: 增加启动时间统计
- **2.0.3**: 更新日志系统接口

## 最佳实践

1. **继承 Warship 类**：所有应用程序应继承 `Warship` 而不是直接实现 `Boat`
2. **异常处理**：在 `launchAsWarship()` 中正确处理异常，避免触发 `shipwreck()`
3. **配置管理**：合理分离本地和远程配置加载逻辑
4. **日志使用**：充分利用运行日志记录器记录关键操作
5. **优雅关闭**：在需要时调用 `sink()` 方法进行优雅关闭
