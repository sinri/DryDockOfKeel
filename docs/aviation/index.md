# Aviation

航空包 (`io.github.sinri.drydock.aviation`) 提供了基于 Vert.x 的微服务应用程序启动框架，采用航母-舰载机的设计模式，实现了完整的微服务生命周期管理系统。

## 包架构概览

Aviation包采用军舰类比的设计模式：
- **航母 (Carrier)**: 提供应用程序启动框架和基础设施
- **舰载机 (Aircraft)**: 提供具体的功能模块（HTTP服务、定时任务、队列处理等）

```
aviation/
├── carrier/           # 航母包 - 应用启动框架
│   ├── AircraftCarrierDeck.java    # 基础抽象类
│   └── AircraftCarrier.java        # 核心实现类
└── aircraft/          # 舰载机包 - 功能模块
    ├── Biplane.java               # 舰载机基类
    ├── Bomber.java                # 定时任务执行器
    ├── Drone.java                 # 队列任务处理器
    └── Fighter.java               # HTTP服务提供器
```

## 核心组件

### 1. Carrier Package - 航母包

航母包提供微服务应用程序的启动框架和基础设施管理。

| 组件 | 功能 | 文档链接 |
|------|------|----------|
| **AircraftCarrierDeck** | 基础抽象类，提供命令行参数解析和程序入口点 | [详细文档](carrier.md#1-aircraftcarrierdeck) |
| **AircraftCarrier** | 核心实现类，提供完整的微服务启动和管理功能 | [详细文档](carrier.md#2-aircraftcarrier) |

**主要特性:**
- 命令行参数解析和处理
- Vert.x 实例管理
- 本地和远程配置加载
- 健康监控系统
- 日志和指标记录
- 组件生命周期管理

### 2. Aircraft Package - 舰载机包

舰载机包提供各种功能模块的抽象框架，所有舰载机必须与航母甲板配合使用。

| 组件 | 功能 | 用途 | 文档链接 |
|------|------|------|----------|
| **Biplane** | 舰载机基类 | 提供与航母甲板的基础连接功能 | [详细文档](aircraft.md#基础类---biplane) |
| **Bomber** | 轰炸机 | 定时任务执行器，基于Sundial实现 | [详细文档](aircraft.md#bomber---轰炸机) |
| **Drone** | 无人机 | 队列任务处理器，基于Queue实现 | [详细文档](aircraft.md#drone---无人机) |
| **Fighter** | 战斗机 | HTTP服务器，处理外部HTTP请求 | [详细文档](aircraft.md#fighter---战斗机) |

## 快速开始

### 1. 基本应用程序结构

```java
import io.github.sinri.drydock.naval.carrier.AircraftCarrier;
import io.github.sinri.drydock.aviation.Bomber;
import io.github.sinri.drydock.aviation.Drone;
import io.github.sinri.drydock.aviation.Fighter;

import javax.annotation.Nullable;

public class MyApplication extends AircraftCarrier {

    // 实现必要的抽象方法
    @Override
    protected String buildCliName() {
        return "my-microservice";
    }

    @Override
    protected String buildCliDescription() {
        return "My microservice application";
    }

    // 构造舰载机组件
    @Override
    protected Bomber constructBomber() {
        return new MyBomber(this);
    }

    @Override
    protected Drone constructDrone() {
        return new MyDrone(this);
    }

    @Override
    protected Fighter constructFighter(@Nullable Integer port) {
        return new MyFighter(this, port);
    }

    // 应用程序入口
    static void main(String[] args) {
        new MyApplication().launch(args);
    }
}
```

### 2. 启动命令示例

```bash
# 启动完整服务
java -jar my-app.jar

# 禁用特定组件
java -jar my-app.jar --disableQueue --disableSundial

# 指定HTTP服务端口
java -jar my-app.jar --receptionistPort=9090
```

## 设计模式

### 1. 模板方法模式
- `AircraftCarrier` 定义完整的启动流程模板
- 子类实现具体的业务逻辑步骤
- 各Aircraft类提供服务构建和启动的模板

### 2. 工厂方法模式
- 通过 `construct*()` 方法创建具体的舰载机实例
- 支持依赖注入和配置定制

### 3. 组合模式
- 舰载机通过组合模式共享航母甲板的资源
- 问题记录中心、日志记录器等基础设施统一管理

### 4. Mixin模式
- 通过 `HealthMonitorMixin` 提供健康监控能力
- 通过各种Mixin接口扩展功能

## 应用场景

### 1. Web API服务
使用 `Fighter` 组件提供RESTful API服务：
- HTTP路由配置
- 请求处理和响应
- 优雅停机支持

### 2. 定时任务服务
使用 `Bomber` 组件执行定时任务：
- Cron表达式支持
- 任务调度管理
- 错误处理和重试

### 3. 异步任务处理
使用 `Drone` 组件处理队列任务：
- 任务队列管理
- 工作线程池配置
- 弹性任务处理

### 4. 混合服务
组合使用多种舰载机组件：
- 同时提供API和后台任务处理
- 统一的监控和日志管理
- 灵活的组件启用/禁用

## 配置管理

### 1. 本地配置
- 通过 `config.properties` 文件配置
- 支持自定义配置加载逻辑

### 2. 远程配置
- 支持异步远程配置加载
- 配置动态更新支持

### 3. 命令行配置
- 丰富的命令行选项支持
- 组件启用/禁用控制
- 端口和参数配置

## 监控和日志

### 1. 健康监控
- 内置健康监控系统
- 支持问题记录和指标收集
- 阿里云SLS集成支持

### 2. 日志系统
- 统一的日志记录器
- 结构化日志支持
- 多级别日志管理

### 3. 指标收集
- 性能指标自动收集
- 业务指标支持
- 实时监控能力

## 版本兼容性

| 版本 | 主要变更 | 兼容性 |
|------|----------|--------|
| **1.5.x** | 初始版本，基础框架 | 稳定 |
| **2.0.x** | 重构优化，增强功能 | 向后兼容 |

## 相关文档

- [**Carrier Package 详细文档**](carrier.md) - 航母包完整使用指南
- [**Aircraft Package 详细文档**](aircraft.md) - 舰载机包完整使用指南
- [**Common Package 文档**](../common/index.md) - 通用组件文档

## 依赖和要求

### 主要依赖
- **Keel Framework**: 核心基础设施
- **Vert.x**: 异步应用平台
- **Aliyun SLS**: 日志服务（可选）

### 系统要求
- Java 8+
- 支持异步编程模型
- 工作线程池支持

## 最佳实践

1. **组件设计**: 根据业务需求选择合适的舰载机组件
2. **配置管理**: 合理使用本地配置和远程配置
3. **错误处理**: 充分利用问题记录中心进行错误追踪
4. **性能优化**: 合理配置工作线程池大小
5. **监控运维**: 利用健康监控系统保障服务稳定性

---

*更多详细信息请参考各组件的具体文档。*
