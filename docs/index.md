# DryDock 框架文档总览

DryDock 是一个基于 Vert.x 和 Keel 框架构建的 Java 微服务应用程序框架，采用军舰类比的设计模式，提供了完整的应用程序生命周期管理和基础设施支持。

## 📖 框架概述

DryDock 框架以海军舰船和航空兵的概念来抽象不同类型的应用程序，提供了从简单控制台应用到复杂微服务的完整解决方案。框架核心特性包括：

- **统一的应用程序生命周期管理**
- **内置健康监控和日志记录**
- **HTTP服务器、队列处理、定时任务支持**
- **阿里云SLS日志服务集成**
- **灵活的配置管理（本地+远程）**
- **代码生成和快速开发工具**

## 🏗️ 架构模块

### 🚢 Naval Package - 海军包
> **传统Web应用和后台服务的核心模块**

海军包提供了基于舰船概念的应用程序架构，适用于传统的Web应用和后台服务。

- **📋 [Naval 总览](./naval/index.md)** - 海军包完整文档和选择指南
- **⚓ [Base Package](./naval/boat.md)** - 基础船只接口和军舰抽象类
- **⚔️ [Melee Package](./naval/melee.md)** - 近战舰船：桨帆船→轻快帆船→铁甲舰→驱逐舰（已弃用）
- **🎯 [Ranged Package](./naval/ranged.md)** - 远程舰船：四段帆船→护卫舰（已弃用）
- **🏴‍☠️ [Raider Package](./naval/raider.md)** - 突袭舰船：私掠船和代码生成工具

**适用场景：**
- Web API服务 → `Ironclad`（铁甲舰）
- 需要监控的应用 → `Caravel`（轻快帆船）
- 简单控制台应用 → `Galley`（桨帆船）
- 一次性任务/代码生成 → `Privateer`（私掠船）

### ✈️ Aviation Package - 航空包
> **现代微服务架构的推荐模块**

航空包采用航母-舰载机设计模式，是2.0版本推荐的微服务架构，替代了Naval包中已弃用的组件。

- **📋 [Aviation 总览](./aviation/index.md)** - 航空包完整文档和快速开始
- **🛩️ [Carrier Package](./aviation/carrier.md)** - 航母甲板和航母核心实现
- **✈️ [Aircraft Package](./aviation/aircraft.md)** - 舰载机：轰炸机、无人机、战斗机

**核心组件：**
- **AircraftCarrier** - 微服务应用启动框架
- **Bomber** - 定时任务执行器（替代Sundial）
- **Drone** - 队列任务处理器（替代Queue）
- **Fighter** - HTTP服务提供器

### 🔧 Common Package - 通用包
> **所有模块的基础设施和工具**

通用包提供了框架的基础能力和混入接口，为所有其他模块提供统一的基础设施。

- **📋 [Common 总览](./common/index.md)** - 通用包完整文档
- **💚 [Health Monitor](./common/health.md)** - 健康监控系统详解
- **📝 [Logging System](./common/logging.md)** - 日志记录和阿里云SLS集成

**核心接口：**
- **CommonUnit** - 所有单元的基础接口
- **HttpServerMixin** - HTTP服务器能力
- **QueueMixin** - 队列处理能力
- **SundialMixin** - 定时任务能力
- **HealthMonitorMixin** - 健康监控能力

## 🚀 快速开始

### 选择合适的基础类

| 应用类型 | 推荐方案 | 主要特性 |
|---------|---------|---------|
| **现代微服务** | `AircraftCarrier` | 完整微服务框架，支持HTTP/队列/定时任务 |
| **Web API服务** | `Ironclad` | HTTP服务器 + 健康监控 |
| **需要监控的应用** | `Caravel` | 健康监控 + 阿里云SLS |
| **简单控制台应用** | `Galley` | 基础功能，完全自定义 |
| **一次性任务** | `Privateer` | 快速启动，轻量级 |
| **代码生成工具** | `ClassFileGeneratorForMySQLTables` | MySQL表类自动生成 |

### 基本使用示例

#### 微服务应用（推荐）
```java
public class MyMicroservice extends AircraftCarrier {
    @Override
    protected String buildCliName() { return "my-service"; }
    
    @Override
    protected Bomber constructBomber() { return new MyBomber(this); }
    
    @Override
    protected Drone constructDrone() { return new MyDrone(this); }
    
    @Override
    protected Fighter constructFighter(Integer port) { 
        return new MyFighter(this, port); 
    }
    
    public static void main(String[] args) {
        new MyMicroservice().launch(args);
    }
}
```

#### Web API服务
```java
public class MyWebService extends Ironclad {
    @Override
    protected Future<Void> launchAsIronclad() {
        // 初始化业务逻辑
        return Future.succeededFuture();
    }
    
    @Override
    public void configureHttpServerRoutes(Router router, KeelIssueRecorder<KeelEventLog> logger) {
        router.get("/api/health").handler(ctx -> ctx.response().end("OK"));
        router.get("/api/users").handler(this::handleGetUsers);
    }
}
```

## 📊 版本兼容性

| 版本 | 状态 | 主要变更 | 推荐使用 |
|------|------|----------|----------|
| **2.0.x** | 当前版本 | 引入Aviation包，弃用部分Naval组件 | ✅ AircraftCarrier系列 |
| **1.x** | 维护中 | Naval包的稳定版本 | ⚠️ 逐步迁移到2.0 |

### 迁移指南

**从弃用组件迁移：**
- `Destroyer` → `AircraftCarrier`
- `Frigate` → `AircraftCarrier`
- 队列和定时任务 → 使用 `Drone` 和 `Bomber`

## 🔧 配置和部署

### 基础配置文件
```properties
# config.properties
app.name=my-application
app.version=1.0.0

# 阿里云SLS配置（可选）
aliyun.sls.project=your-project
aliyun.sls.logstore=your-logstore
aliyun.sls.endpoint=your-endpoint

# 数据库配置（如需要）
mysql.host=localhost
mysql.port=3306
mysql.database=your_db
```

### 启动命令
```bash
# 启动完整微服务
java -jar my-app.jar

# 禁用特定组件
java -jar my-app.jar --disableQueue --disableSundial

# 指定HTTP端口
java -jar my-app.jar --receptionistPort=8080
```

## 🏆 最佳实践

### 1. 架构选择
- **新项目**：优先使用 `AircraftCarrier` 系列
- **现有项目**：制定从Naval到Aviation的迁移计划
- **简单需求**：选择功能最接近的最简单实现

### 2. 配置管理
- 本地配置使用 `config.properties`
- 敏感信息通过环境变量管理
- 支持远程配置动态加载

### 3. 监控和日志
- 启用健康监控提高可观测性
- 配置阿里云SLS进行集中日志管理
- 合理设置监控间隔和告警阈值

### 4. 错误处理
- 实现完善的异常处理逻辑
- 使用Future链式调用处理异步错误
- 记录详细错误信息便于调试

## 🤝 贡献和反馈

DryDock框架持续演进中，欢迎：
- 报告问题和Bug
- 提出功能建议
- 贡献代码和文档
- 分享使用经验

---

**快速导航：**
- [🚢 Naval Package](./naval/index.md) - 传统Web应用架构
- [✈️ Aviation Package](./aviation/index.md) - 现代微服务架构  
- [🔧 Common Package](./common/index.md) - 基础设施和工具

**版本信息：** DryDock 2.0.x | 文档更新时间：2025年
