# Naval

## 概述

DryDock Naval Package 是 DryDock 框架的核心海军模块，以海军舰船的概念构建了一套完整的应用程序架构体系。该包提供了从基础船只到复杂战舰的完整继承层次，支持各种应用场景的需求。

## 架构设计理念

Naval Package 采用海军舰船的比喻来抽象不同类型的应用程序：
- **船只 (Boat)**: 所有应用程序的基础接口，定义生命周期管理
- **军舰 (Warship)**: 应用程序的抽象基类，提供完整的启动和运行框架
- **各类舰船**: 针对不同应用场景的专门实现

## 包结构总览

```
naval/
├── base/           # 基础包 - 核心接口和抽象类
├── melee/          # 近战包 - 传统Web应用舰船
├── ranged/         # 远程包 - 队列和定时任务舰船
└── raider/         # 突袭包 - 快速开发和代码生成工具
```

## 核心模块详解

### 1. [Base Package - 基础包](./boat.md)

**核心组件**: `Boat` 接口 + `Warship` 抽象类

提供所有应用程序的基础架构：
- **Boat 接口**: 定义应用程序生命周期（启航、海难处理、沉没）
- **Warship 抽象类**: 实现完整的应用启动框架，包括配置加载、日志系统、异常处理

**适用场景**: 所有类型应用的基础，不直接使用，通过子类继承

### 2. [Melee Package - 近战包](./melee.md)

**继承层次**: `Warship` → `Galley` → `Caravel` → `Ironclad` → `Destroyer`（已弃用）

专为传统Web应用和HTTP服务设计：

#### 🚣 Galley (桨帆船)
- 最基础的舰船实现
- 完全可定制的配置
- 适用于简单应用场景

#### ⛵ Caravel (轻快帆船)  
- 增加配置管理和健康监控
- 集成阿里云SLS日志服务
- 支持数据源管理
- 适用于需要监控的应用

#### 🛡️ Ironclad (铁甲舰)
- 增加HTTP服务器功能
- 支持横向弹性扩展
- 适用于Web应用和API服务
- SAE APP环境优化

#### ⚔️ Destroyer (驱逐舰) - 已弃用
- 增加定时任务和队列功能
- 2.0.0版本起弃用，建议使用AircraftCarrier系列

### 3. [Ranged Package - 远程包](./ranged.md)

**继承层次**: `Warship` → `Quadrireme` → `Frigate`（已弃用）

专为后台任务处理设计：

#### 🏛️ Quadrireme (四段帆船)
- 远程作战舰船基础类
- 64线程工作池配置
- 本地配置加载
- 适用于单节点服务

#### 🚢 Frigate (护卫舰) - 已弃用
- 队列和定时任务处理器
- 健康监控集成
- 2.0.0版本起弃用，建议使用AircraftCarrier系列

### 4. [Raider Package - 突袭包](./raider.md)

**核心组件**: `Privateer` + `ClassFileGeneratorForMySQLTables`

专为快速开发和代码生成设计：

#### 🏴‍☠️ Privateer (私掠船)
- 快速启动器基类
- 一次性任务执行器
- 轻量级配置加载
- 适用于数据迁移、批处理等场景

#### 🛠️ ClassFileGeneratorForMySQLTables
- MySQL表类文件自动生成器
- 多数据源支持
- 安全备份和回滚机制
- 适用于代码生成和数据库同步

## 选择指南

### 按应用类型选择

| 应用类型 | 推荐舰船 | 主要特性 |
|---------|---------|---------|
| 简单控制台应用 | Galley | 基础功能，完全自定义 |
| 需要监控的应用 | Caravel | 健康监控 + 日志服务 |
| Web应用/API服务 | Ironclad | HTTP服务器 + 弹性扩展 |
| 单节点后台服务 | Quadrireme | 多线程 + 配置管理 |
| 一次性任务 | Privateer | 快速启动 + 轻量级 |
| 代码生成工具 | ClassFileGeneratorForMySQLTables | 自动化代码生成 |

### 按功能需求选择

| 功能需求 | 相关舰船 | 说明 |
|---------|---------|-----|
| HTTP服务 | Ironclad | 支持Web服务和API |
| 健康监控 | Caravel, Ironclad | 集成健康检查 |
| 日志服务 | Caravel, Ironclad | 阿里云SLS集成 |
| 队列处理 | Frigate（弃用） | 建议使用AircraftCarrier |
| 定时任务 | Frigate（弃用） | 建议使用AircraftCarrier |
| 数据库操作 | Caravel, Ironclad | 数据源管理 |
| 快速开发 | Privateer | 最小化依赖 |

## 版本演进

### 当前版本 (2.0.x)
- 弃用Destroyer和Frigate类
- 推荐使用AircraftCarrier系列（航空包）
- 改进健康监控和日志系统

### 历史版本
- **1.x**: 初始版本，建立基础架构
- **2.0**: 架构重构，引入新的舰船类型

## 最佳实践

### 1. 继承选择
- 优先选择功能最接近需求的舰船类型
- 避免过度设计，选择最简单满足需求的类

### 2. 配置管理
- 使用`config.properties`进行本地配置
- 敏感信息通过环境变量管理
- 不同环境使用不同配置文件

### 3. 日志和监控
- 充分利用内置的日志记录功能
- 配置阿里云SLS进行集中日志管理
- 启用健康监控提高系统可观测性

### 4. 错误处理
- 实现适当的异常处理逻辑
- 使用Future链式调用处理异步错误
- 记录详细的错误信息便于调试

## 迁移指南

### 从弃用类迁移
- **Destroyer** → 推荐使用 **AircraftCarrier** 系列
- **Frigate** → 推荐使用 **AircraftCarrier** 系列
- 现有项目建议逐步迁移到新架构

### 升级注意事项
- 检查弃用警告并制定迁移计划
- 测试新版本的兼容性
- 更新依赖和配置文件

## 相关文档

- [Aviation Package](../aviation/index.md) - 航空包文档
- [Common Package](../common/index.md) - 通用组件文档
- [项目主文档](../index.md) - DryDock框架总览

---

**注意**: Destroyer和Frigate类已在2.0.0版本中标记为弃用，建议新项目使用AircraftCarrier系列，现有项目制定迁移计划。
