# Aircraft Package (aviation.aircraft)

## 概述

`io.github.sinri.drydock.aviation.aircraft` 包提供了一套舰载机类的抽象框架，这些舰载机类必须与航母甲板 (`AircraftCarrierDeck`) 配合使用。aircraft包中的所有类都设计为在 `AircraftCarrierDeck` 类（或其子类）中被初始化和管理，依附于 `AircraftCarrierDeck` 实例运行。

## 核心架构

### 基础类 - Biplane

`Biplane` 是所有舰载机的抽象基类，提供了与航母甲板的基础连接功能。

**主要特性：**
- 实现 `CommonUnit` 接口
- 与 `AircraftCarrierDeck` 强耦合，通过构造函数注入
- 通过组合模式共享航母甲板的问题记录中心和日志记录器

**关键方法：**
- `getIssueRecordCenter()`: 获取问题记录中心（来自AircraftCarrierDeck）
- `getUnitLogger()`: 获取单元日志记录器（来自AircraftCarrierDeck）

**在AircraftCarrierDeck中的使用模式：**
```java
public class MyAircraftCarrierDeck extends AircraftCarrierDeck {
    
    @Override
    protected void runWithCommandLine(CommandLine commandLine) {
        // 在runWithCommandLine方法中初始化和管理aircraft实例
        CustomBiplane biplane = new CustomBiplane(this, "侦察机-001");
        biplane.performMission();
    }
    
    // ... 其他必要的抽象方法实现
}

// 自定义Biplane实现
public class CustomBiplane extends Biplane {
    private final String name;
    
    public CustomBiplane(@Nonnull AircraftCarrierDeck deck, String name) {
        super(deck);
        this.name = name;
    }
    
    public void performMission() {
        // 使用来自AircraftCarrierDeck的日志记录器
        getUnitLogger().info("开始执行任务", "aircraft", name);
        
        try {
            doSomething();
            getUnitLogger().info("任务执行完成", "aircraft", name);
        } catch (Exception e) {
            // 使用来自AircraftCarrierDeck的问题记录中心
            getIssueRecordCenter()
                .generateIssueRecorder("CustomBiplane", CustomIssueRecord::new)
                .error("任务执行失败", e, "aircraft", name);
        }
    }
    
    private void doSomething() {
        System.out.println("执行自定义舰载机任务: " + name);
    }
}
```

## 具体实现类

### Bomber - 轰炸机

**用途：** 定时任务执行器，用于按照战术设计定时发起轰炸操作。

**核心功能：**
- 基于 `KeelSundial` 实现定时任务调度
- 支持自定义任务计划的动态获取
- 工作线程模型部署

**关键方法：**
- `buildSundial()`: 构建日晷实例
- `fetchSundialPlans()`: 抽象方法，需要子类实现具体的任务计划获取逻辑
- `loadSundial()`: 启动定时任务服务

**在AircraftCarrierDeck中的初始化方式：**
```java
public class MyAircraftCarrierDeck extends AircraftCarrierDeck {
    
    @Override
    protected void runWithCommandLine(CommandLine commandLine) {
        // 在runWithCommandLine方法中初始化Bomber
        DataSyncBomber bomber = new DataSyncBomber(this);
        
        // 启动定时任务服务
        bomber.loadSundial()
            .onSuccess(deploymentId -> {
                getUnitLogger().info("定时任务服务启动成功", "deploymentId", deploymentId);
            })
            .onFailure(error -> {
                getUnitLogger().error("定时任务服务启动失败", error);
            });
    }
}

// 自定义Bomber实现
public class DataSyncBomber extends Bomber {
    public DataSyncBomber(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
    }

    @Override
    protected Future<Collection<KeelSundialPlan>> fetchSundialPlans(
            KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
        List<KeelSundialPlan> plans = new ArrayList<>();
        
        // 每天凌晨2点执行数据同步
        plans.add(new KeelSundialPlan()
            .setCronExpression("0 0 2 * * ?")
            .setTaskHandler(() -> {
                sundialIssueRecorder.info("开始执行数据同步");
                return performDataSync();
            }));
        
        return Future.succeededFuture(plans);
    }
    
    private Future<Void> performDataSync() {
        // 实现具体的数据同步逻辑
        getUnitLogger().info("执行数据同步逻辑");
        return Future.succeededFuture();
    }
}
```

### Drone - 无人机

**用途：** 任务队列处理器，用于按需执行任务，支持弹性限度下的密集任务处理。

**核心功能：**
- 基于 `KeelQueue` 实现任务队列管理
- 支持可配置的工作线程池大小
- 提供任务信号读取和任务搜索机制

**关键方法：**
- `buildQueue()`: 构建队列实例
- `readSignal()`: 抽象方法，读取队列信号
- `seekNextTask()`: 抽象方法，寻找下一个待执行任务
- `loadQueue()`: 启动队列服务
- `configuredQueueWorkerPoolSize()`: 配置工作线程池大小（默认为0，表示无限制）
- `beforeLoadingQueue()`: 队列加载前的清理工作（自1.5.8版本引入）
- `getQueueManageIssueRecorder()`: 获取队列管理问题记录器（自2.0.4版本引入）

**在AircraftCarrierDeck中的初始化方式：**
```java
public class MyAircraftCarrierDeck extends AircraftCarrierDeck {
    
    @Override
    protected void runWithCommandLine(CommandLine commandLine) {
        // 在runWithCommandLine方法中初始化Drone
        EmailProcessingDrone drone = new EmailProcessingDrone(this);
        
        // 启动队列服务
        drone.loadQueue()
            .onSuccess(deploymentId -> {
                getUnitLogger().info("邮件处理队列启动成功", "deploymentId", deploymentId);
            })
            .onFailure(error -> {
                getUnitLogger().error("邮件处理队列启动失败", error);
            });
    }
}

// 自定义Drone实现
public class EmailProcessingDrone extends Drone {
    
    public EmailProcessingDrone(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
    }
    
    @Override
    protected int configuredQueueWorkerPoolSize() {
        // 配置10个工作线程处理邮件任务
        return 10;
    }
    
    @Override
    protected Future<KeelQueueSignal> readSignal() {
        // 从外部系统检查是否有新的邮件任务
        return checkForNewEmailTasks()
            .map(hasNewTasks -> hasNewTasks ? 
                KeelQueueSignal.CONTINUE : KeelQueueSignal.WAIT);
    }
    
    @Override
    protected Future<KeelQueueTask> seekNextTask() {
        return getNextEmailTask()
            .map(emailTask -> {
                if (emailTask == null) {
                    return null;
                }
                
                return new KeelQueueTask() {
                    @Override
                    public String getTaskId() {
                        return emailTask.getId();
                    }
                    
                    @Override
                    public Future<Void> execute() {
                        return processEmailTask(emailTask)
                            .compose(result -> {
                                getQueueManageIssueRecorder().info(
                                    "邮件任务处理完成", 
                                    "taskId", emailTask.getId()
                                );
                                return Future.succeededFuture();
                            });
                    }
                };
            });
    }
    
    @Override
    protected Future<Void> beforeLoadingQueue() {
        // 队列启动前的清理工作
        getUnitLogger().info("清理未完成的任务");
        return Future.succeededFuture();
    }
    
    private Future<Boolean> checkForNewEmailTasks() {
        // 实现检查新任务的逻辑
        return Future.succeededFuture(true);
    }
    
    private Future<EmailTask> getNextEmailTask() {
        // 实现获取下一个任务的逻辑
        return Future.succeededFuture(new EmailTask("task-001"));
    }
    
    private Future<Void> processEmailTask(EmailTask task) {
        // 实现邮件处理逻辑
        getUnitLogger().info("处理邮件任务", "taskId", task.getId());
        return Future.succeededFuture();
    }
    
    // 简单的EmailTask类定义
    private static class EmailTask {
        private final String id;
        
        public EmailTask(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
    }
}
```

### Fighter - 战斗机

**用途：** HTTP服务器，用于应对来犯的外部HTTP请求。

**核心功能：**
- 基于 `KeelHttpServer` 实现HTTP服务
- 支持可配置的端口号
- 提供路由配置和服务启动前的预处理

**关键方法：**
- `buildHttpServer()`: 构建HTTP服务器实例
- `configureHttpServerRoutes()`: 抽象方法，配置HTTP路由
- `beforeStartHttpServer()`: 抽象方法，服务启动前的预处理
- `loadHttpServer()`: 启动HTTP服务
- `stopServer()`: 停止服务器（自1.4.17版本引入）
- `isToStopServer()`: 检查是否需要停止服务器
- `configuredHttpServerPort()`: 获取配置的HTTP服务端口

**在AircraftCarrierDeck中的初始化方式：**
```java
public class MyAircraftCarrierDeck extends AircraftCarrierDeck {
    
    @Override
    protected void runWithCommandLine(CommandLine commandLine) {
        // 从命令行参数获取端口号，或使用默认值
        int port = commandLine.getOptionValue("port") != null ? 
            Integer.parseInt(commandLine.getOptionValue("port")) : 8080;
            
        // 在runWithCommandLine方法中初始化Fighter
        UserApiFighter fighter = new UserApiFighter(this, port);
        
        // 启动HTTP服务
        fighter.loadHttpServer()
            .onSuccess(deploymentId -> {
                getUnitLogger().info("HTTP服务启动成功", 
                    "port", port, "deploymentId", deploymentId);
            })
            .onFailure(error -> {
                getUnitLogger().error("HTTP服务启动失败", error);
            });
    }
    
    @Override
    protected List<Option> buildCliOptions() {
        List<Option> options = new ArrayList<>();
        options.add(new Option()
            .setLongName("port")
            .setShortName("p")
            .setDescription("HTTP服务端口号")
            .setDefaultValue("8080"));
        return options;
    }
}

// 自定义Fighter实现
public class UserApiFighter extends Fighter {
    
    public UserApiFighter(@Nonnull AircraftCarrierDeck deck, int port) {
        super(deck, port);
    }
    
    @Override
    protected void configureHttpServerRoutes(Router router, 
                                           KeelIssueRecorder<KeelEventLog> httpServerLogger) {
        // 配置REST API路由
        
        // 健康检查接口
        router.get("/health").handler(ctx -> {
            httpServerLogger.info("健康检查请求");
            ctx.response()
               .putHeader("Content-Type", "application/json")
               .end("{\"status\":\"UP\",\"timestamp\":\"" + Instant.now() + "\"}");
        });
        
        // 用户信息接口
        router.get("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            httpServerLogger.info("获取用户信息请求", "userId", userId);
            
            // 模拟用户数据
            String userJson = String.format(
                "{\"id\":\"%s\",\"name\":\"用户%s\",\"timestamp\":\"%s\"}", 
                userId, userId, Instant.now()
            );
            
            ctx.response()
               .putHeader("Content-Type", "application/json")
               .end(userJson);
        });
        
        // 优雅停机接口
        router.post("/admin/shutdown").handler(ctx -> {
            httpServerLogger.info("收到停机请求");
            stopServer();
            ctx.response().end("Shutdown initiated");
        });
    }
    
    @Override
    protected Future<Void> beforeStartHttpServer() {
        // 服务启动前的初始化工作
        getUnitLogger().info("正在初始化HTTP服务");
        return Future.succeededFuture();
    }
}
```

## 综合使用示例

以下是一个完整的 `AircraftCarrier` 子类示例，展示如何同时管理多种aircraft类。`AircraftCarrier` 已经提供了完整的aircraft管理框架，只需要实现构造方法即可：

```java
public class ComprehensiveCarrier extends AircraftCarrier {
    
    @Override
    protected String buildCliName() {
        return "comprehensive-service";
    }
    
    @Override
    protected String buildCliDescription() {
        return "综合服务，包含HTTP API、定时任务和队列处理";
    }
    
    @Override
    protected VertxOptions buildVertxOptions(@Nonnull CommandLine.ParseResult parseResult) {
        // 配置Vert.x选项
        return new VertxOptions()
            .setWorkerPoolSize(50)
            .setEventLoopPoolSize(4);
    }
    
    @Override
    protected Future<Void> loadRemoteConfiguration(@Nonnull CommandLine.ParseResult parseResult) {
        // 加载远程配置（如需要）
        getUnitLogger().info("加载远程配置完成");
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> prepare(@Nonnull CommandLine.ParseResult parseResult) {
        // 业务初始化准备工作
        getUnitLogger().info("业务准备工作完成");
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> ready(@Nonnull CommandLine.ParseResult parseResult) {
        // 服务就绪后的处理
        getUnitLogger().info("所有服务已就绪，系统正常运行");
        return Future.succeededFuture();
    }
    
    // 构造定时任务组件（Bomber）
    @Override
    protected Bomber constructBomber() {
        return new DataSyncBomber(this);
    }
    
    // 构造队列处理组件（Drone）
    @Override
    protected Drone constructDrone() {
        return new EmailProcessingDrone(this);
    }
    
    // 构造HTTP服务组件（Fighter）
    @Override
    protected Fighter constructFighter(@Nullable Integer port) {
        // 如果没有指定端口，使用默认端口8080
        int httpPort = port != null ? port : 8080;
        return new UserApiFighter(this, httpPort);
    }
    
    public static void main(String[] args) {
        new ComprehensiveCarrier().launch(args);
    }
}
```

## 设计模式

### 模板方法模式
每个具体的飞机类都使用了模板方法模式：
- 基类提供了完整的服务构建和启动流程
- 子类只需实现特定的抽象方法来定制行为

### 组合模式
- 所有飞机类都依赖于 `AircraftCarrierDeck` 来获取共享资源
- 通过组合而非继承来实现资源共享

### 依赖注入模式
- 所有aircraft类都通过构造函数接收 `AircraftCarrierDeck` 实例
- 确保aircraft类与航母甲板的紧密耦合关系

## 使用指南

1. **创建AircraftCarrierDeck子类：**
   - 继承 `AircraftCarrierDeck` 并实现必要的抽象方法
   - 在 `runWithCommandLine` 方法中管理aircraft实例的生命周期

2. **选择合适的飞机类型：**
   - 需要定时任务？使用 `Bomber`
   - 需要队列处理？使用 `Drone`
   - 需要HTTP服务？使用 `Fighter`

3. **实现aircraft子类：**
   - 继承相应的aircraft类型并实现必要的抽象方法
   - 通过构造函数接收 `AircraftCarrierDeck` 实例

4. **在AircraftCarrierDeck中初始化和启动：**
   - 在 `runWithCommandLine` 方法中创建aircraft实例
   - 调用相应的 `load*()` 方法来启动服务
   - 处理启动成功和失败的回调

## 版本历史

- **1.3.4**: 引入问题记录中心
- **1.4.17**: Fighter类增加服务器停止开关
- **1.5.0**: 包的初始版本，引入所有核心类
- **1.5.2**: Fighter类增加服务启动前处理
- **1.5.8**: Drone类增加队列加载前的清理功能
- **2.0.4**: 增加日志记录器参数
- **2.0.5**: 重构Drone类的信号和任务方法

## 注意事项

- 所有aircraft类都必须在 `AircraftCarrierDeck` 的上下文中使用，不能独立运行
- aircraft类通过组合模式共享 `AircraftCarrierDeck` 的资源（问题记录中心、日志记录器等）
- 所有aircraft类都使用工作线程模型进行部署
- 遵循异步编程模式，所有启动方法都返回 `Future`
- 在 `AircraftCarrierDeck` 的 `runWithCommandLine` 方法中统一管理aircraft实例的生命周期
