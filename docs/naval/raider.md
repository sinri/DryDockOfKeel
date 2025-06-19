# Naval Raider Package Documentation

## 概述

`io.github.sinri.drydock.naval.raider` 包提供了用于快速开发和代码生成的工具类。该包主要包含两个核心类：`Privateer`（私掠船）作为快速运行器基类，以及 `ClassFileGeneratorForMySQLTables`（MySQL表类文件生成器）用于自动生成MySQL表对应的Java类文件。

## 包结构

| 类名 | 描述 |
|------|------|
| `Privateer` | 私掠船基类，提供快速启动和配置加载功能 |
| `ClassFileGeneratorForMySQLTables` | MySQL表类文件生成器，自动生成表对应的Java类 |

## 核心组件

### 1. Privateer (私掠船)

**位置**: `src/main/java/io/github/sinri/drydock/naval/raider/Privateer.java`

私掠船是一个抽象基类，继承自 `KeelInstantRunner`，专为快速开发和一次性任务设计。

#### 主要特点
- **快速启动**: 基于 `KeelInstantRunner` 的即时运行能力
- **配置管理**: 自动加载本地 `config.properties` 配置文件
- **环境准备**: 提供可重写的环境准备方法
- **轻量级**: 最小化的依赖和初始化流程

#### 核心方法

```java
// 加载本地配置文件
protected void loadLocalConfiguration()

// 环境准备（可重写）
protected Future<Void> prepareEnvironment()

// 最终启动方法（不可重写）
protected final Future<Void> starting()
```

#### 启动流程

1. **本地配置加载** - 自动加载 `config.properties` 文件
2. **环境准备** - 调用 `prepareEnvironment()` 方法进行自定义准备工作
3. **任务执行** - 执行具体的业务逻辑

#### 使用示例

```java
public class DataMigrationPrivateer extends Privateer {
    
    @Override
    protected Future<Void> prepareEnvironment() {
        // 准备数据库连接等资源
        getUnitLogger().info("准备数据迁移环境");
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> work() {
        // 执行数据迁移任务
        getUnitLogger().info("开始执行数据迁移");
        return performDataMigration()
            .onSuccess(v -> getUnitLogger().info("数据迁移完成"))
            .onFailure(error -> getUnitLogger().error("数据迁移失败", error));
    }
    
    private Future<Void> performDataMigration() {
        // 实现具体的数据迁移逻辑
        return Future.succeededFuture();
    }
    
    public static void main(String[] args) {
        new DataMigrationPrivateer().run();
    }
}
```

### 2. ClassFileGeneratorForMySQLTables (MySQL表类文件生成器)

**位置**: `src/main/java/io/github/sinri/drydock/naval/raider/ClassFileGeneratorForMySQLTables.java`

这是一个抽象类，继承自 `Privateer`，专门用于自动生成MySQL数据库表对应的Java类文件。

#### 主要特点
- **代码生成**: 基于 `TableRowClassSourceCodeGenerator` 自动生成Java类
- **多数据源支持**: 支持多个MySQL数据源的表类生成
- **包结构管理**: 自动创建和管理包目录结构
- **安全备份**: 生成前备份旧文件，失败时可回滚
- **灵活配置**: 支持严格枚举包、信封包等配置选项

#### 核心配置方法

```java
// 必须实现的抽象方法
protected abstract String getTablePackage();           // 表类根包名
public abstract String getStrictEnumPackage();         // 严格枚举包名
public abstract String getEnvelopePackage();           // 信封包名

// 可重写的配置方法
protected String getTablePackagePath()                 // 表类文件路径
public boolean isProvideConstSchema()                  // 是否提供Schema常量
public boolean isProvideConstTable()                   // 是否提供Table常量  
public boolean isProvideConstSchemaAndTable()          // 是否提供Schema和Table常量
```

#### 核心生成方法

```java
// 重建指定数据源和Schema的所有表类
protected <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
    String dataSourceName,
    Function<SqlConnection, C> sqlConnectionWrapper,
    String schemaName
)

// 重建指定数据源、Schema和表列表的类
protected <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
    String dataSourceName,
    Function<SqlConnection, C> sqlConnectionWrapper,
    String schemaName,
    List<String> tables
)
```

#### 文件管理机制

该类实现了安全的文件管理机制：

1. **暂存旧文件** (`stashOldClassFiles`): 将现有的Java类文件重命名为 `.stash` 后缀
2. **生成新文件**: 使用 `TableRowClassSourceCodeGenerator` 生成新的类文件
3. **清理旧文件** (`removeOldClassFiles`): 成功后删除 `.stash` 文件
4. **回滚机制** (`callbackOldClassFiles`): 失败时恢复 `.stash` 文件

#### 使用示例

```java
public class MyTableClassGenerator extends ClassFileGeneratorForMySQLTables {
    
    @Override
    protected String getTablePackage() {
        return "com.example.entity";
    }
    
    @Override
    public String getStrictEnumPackage() {
        return "com.example.enums";
    }
    
    @Override
    public String getEnvelopePackage() {
        return "com.example.dto";
    }
    
    @Override
    protected Future<Void> prepareEnvironment() {
        // 初始化数据库连接等
        return Future.succeededFuture();
    }
    
    @Override
    protected Future<Void> work() {
        // 生成用户数据库的所有表类
        return rebuildTablesInSchema(
            "userdb",                    // 数据源名称
            NamedMySQLConnection::new,   // 连接包装器
            "user_system"                // Schema名称
        ).compose(v -> {
            // 生成订单数据库的特定表类
            return rebuildTablesInSchema(
                "orderdb",
                NamedMySQLConnection::new,
                "order_system",
                List.of("orders", "order_items", "payments")  // 指定表列表
            );
        });
    }
    
    public static void main(String[] args) {
        new MyTableClassGenerator().run();
    }
}
```

#### 配置文件示例

在 `config.properties` 中需要配置：

```properties
# 表类文件生成路径
table.package.path=/path/to/your/project/src/main/java

# 数据库连接配置
userdb.mysql.host=localhost
userdb.mysql.port=3306
userdb.mysql.database=user_system
userdb.mysql.username=user
userdb.mysql.password=password

orderdb.mysql.host=localhost
orderdb.mysql.port=3306
orderdb.mysql.database=order_system
orderdb.mysql.username=user
orderdb.mysql.password=password

# 阿里云SLS配置（如果需要日志记录）
aliyun.sls.endpoint=your-endpoint
aliyun.sls.access.key.id=your-access-key-id
aliyun.sls.access.key.secret=your-access-key-secret
aliyun.sls.project=your-project-name
```

#### 生成的文件结构

生成的Java类文件将按以下结构组织：

```
src/main/java/
└── com/example/entity/
    ├── userdb/
    │   └── usersystem/
    │       ├── User.java
    │       ├── UserProfile.java
    │       └── package-info.java
    └── orderdb/
        └── ordersystem/
            ├── Order.java
            ├── OrderItem.java
            ├── Payment.java
            └── package-info.java
```

## 版本历史

- **1.2.0**: 引入 `Privateer` 基类
- **1.2.4**: 添加 `ClassFileGeneratorForMySQLTables` 类
- **1.5.11**: 改进文件管理机制，添加暂存和回滚功能
- **2.0.0**: 提供默认的 `prepareEnvironment()` 实现

## 最佳实践

### 1. 选择合适的基类

- **Privateer**: 用于一次性任务、数据迁移、批处理等场景
- **ClassFileGeneratorForMySQLTables**: 用于自动生成数据库表对应的Java类

### 2. 配置管理

- 将数据库连接信息放在 `config.properties` 中
- 使用环境变量或配置中心管理敏感信息
- 为不同环境（开发、测试、生产）准备不同的配置文件

### 3. 错误处理

- 重写 `prepareEnvironment()` 方法进行环境检查
- 在 `work()` 方法中实现具体的业务逻辑和错误处理
- 利用 Vert.x Future 的链式调用进行异步错误处理

### 4. 日志记录

- 使用内置的 `getUnitLogger()` 进行日志记录
- 配置阿里云SLS进行集中日志管理
- 在关键步骤记录详细的执行信息

## 适用场景

### Privateer 适用场景
- 数据迁移脚本
- 批量数据处理任务
- 系统初始化脚本
- 一次性维护任务
- 快速原型开发

### ClassFileGeneratorForMySQLTables 适用场景  
- 数据库表结构变更后重新生成实体类
- 新项目的初始代码生成
- 多数据源项目的统一代码生成
- 持续集成中的自动化代码生成

## 注意事项

1. **配置文件**: 确保 `config.properties` 文件在类路径中
2. **数据库权限**: 生成器需要对目标数据库有读取权限
3. **文件权限**: 确保对目标目录有写入权限
4. **备份机制**: 生成前会自动备份现有文件，但建议使用版本控制系统
5. **包名规范**: 遵循Java包命名规范，避免使用特殊字符
