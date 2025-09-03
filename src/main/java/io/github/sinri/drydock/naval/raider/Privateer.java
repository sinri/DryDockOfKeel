package io.github.sinri.drydock.naval.raider;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * 私掠船 - 用于快速原型开发和测试的轻量级战舰实现。
 * <p>
 * 私掠船类提供了一个简化的战舰实现，主要用于：
 * <ul>
 *   <li>快速原型开发和测试</li>
 *   <li>简单的应用程序启动</li>
 *   <li>开发过程中的调试和验证</li>
 * </ul>
 *
 * <p>
 * 版本演进：
 * <ul>
 *   <li>2.1.0: 重构实现，与之前版本不兼容</li>
 *   <li>2.1.1: 支持 JDK 21+ 虚拟线程特性</li>
 * </ul>
 *
 * <p>
 * 使用方式：
 * <ol>
 *   <li>继承此类并实现 {@link #launchAsPrivateer()} 方法</li>
 *   <li>在IDE中通过 {@link #main(String[])} 方法启动，其会调用 {@link #launch()} 方法。注意不要自行重写 main 方法或直接调用 {@link #launch()} 方法。</li>
 *   <li>可选择性重写 {@link #starting()} 和 {@link #ending()} 方法自定义生命周期</li>
 * </ol>
 *
 * @since 2.1.0
 */
public abstract class Privateer extends Warship {
    /**
     * 用于控制程序退出的同步锁。
     * <p>
     * 在 main 方法中创建，在启动完成后通过 {@link #launchAsWarship()} 方法释放，
     * 确保程序能够正确退出。
     */
    private CountDownLatch countDownLatch;

    /**
     * 私掠船的主入口点。
     * <p>
     * 通过反射机制动态创建调用类的实例并启动。
     * 这种设计允许子类直接使用 main 方法启动而无需重复编写启动逻辑。
     *
     * @param args 命令行参数（当前未使用）
     * @throws ClassNotFoundException    当无法找到调用类时抛出
     * @throws NoSuchMethodException     当调用类缺少无参构造函数时抛出
     * @throws InvocationTargetException 当构造函数调用失败时抛出
     * @throws InstantiationException    当无法实例化调用类时抛出
     * @throws IllegalAccessException    当访问构造函数被拒绝时抛出
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        // 获取调用此 main 方法的类名
        String calledClass = System.getProperty("sun.java.command");
        // Keel.getLogger().debug(r -> r.message("Privateer Class: " + calledClass));

        // 通过反射加载调用类
        Class<?> aClass = Class.forName(calledClass);
        // Keel.getLogger().debug(r -> r.message("Reflected Class: " + aClass));

        // 获取无参构造函数并创建实例
        Constructor<?> constructor = aClass.getConstructor();
        Privateer testInstance = (Privateer) constructor.newInstance();

        testInstance.countDownLatch = new CountDownLatch(1);

        // 启动私掠船实例
        testInstance.launch();

        testInstance.countDownLatch.await();
    }

    /**
     * 构建事件日志记录中心。
     * <p>
     * 私掠船提供默认实现：使用简单的输出中心，将日志直接输出到控制台，
     * 适用于开发和测试环境。
     * <p>
     * 子类可以重写此方法以提供自定义的日志记录中心实现。
     *
     * @return 输出类型的事件日志记录中心
     */
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }

    /**
     * 构建 Vert.x 选项配置。
     * <p>
     * 私掠船提供默认实现：使用默认的 Vert.x 配置，适用于大多数开发和测试场景。
     * <p>
     * 子类可以重写此方法以提供自定义的 Vert.x 配置。
     *
     * @return 默认的 VertxOptions 实例
     */
    @Override
    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    /**
     * 加载本地配置文件。
     * <p>
     * 私掠船提供默认实现：尝试加载 "config.properties" 配置文件。
     * 如果文件不存在，操作会静默失败。
     * <p>
     * 子类可以重写此方法以实现自定义的本地配置加载逻辑。
     */
    @Override
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    /**
     * 加载远程配置。
     * <p>
     * 私掠船提供默认实现：不加载任何远程配置，直接返回成功的 Future。
     * <p>
     * 子类可以重写此方法以实现自定义的远程配置加载逻辑。
     *
     * @return 表示加载完成的成功 Future
     */
    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    /**
     * 作为战舰启动的具体实现。
     * <p>
     * 启动流程：
     * <ol>
     *   <li>设置日志级别为 DEBUG</li>
     *   <li>执行启动前的准备工作 {@link #starting()}</li>
     *   <li>调用子类实现的 {@link #launchAsPrivateer()}</li>
     *   <li>无论成功或失败都执行清理工作 {@link #ending()}</li>
     * </ol>
     * <p>
     * 自 2.1.1 版本起，在 JDK 21+ 环境下将自动应用虚拟线程特性。
     *
     * @return 表示启动完成的 Future
     */
    @Override
    protected final Future<Void> launchAsWarship() {
        // 设置调试级别的日志输出
        getUnitLogger().setVisibleLevel(KeelLogLevel.DEBUG);

        return starting()
                .compose(v -> {
                    if (Keel.reflectionHelper().isVirtualThreadsAvailable()) {
                        return Keel.runInVerticleOnVirtualThread(this::launchAsPrivateer);
                    } else {
                        return launchAsPrivateer();
                    }
                })
                .onFailure(e -> {
                    getUnitLogger().exception(e, "Thrown from launchAsPrivateer");
                })
                .eventually(this::ending)
                .andThen(ar -> {
                    countDownLatch.countDown();
                });
    }

    /**
     * 私掠船特定的启动逻辑。
     * <p>
     * 子类必须实现此方法以定义具体的业务逻辑。
     * 此方法在完成基础设施初始化后被调用。
     * <p>
     * 自 2.1.1 版本起，在 JDK 21+ 环境下将自动应用虚拟线程特性。
     *
     * @return 表示私掠船启动完成的 Future
     */
    abstract protected Future<Void> launchAsPrivateer();

    /**
     * 启动前的准备工作。
     * <p>
     * 私掠船提供默认实现：记录启动日志并返回成功的 Future。
     * <p>
     * 子类可以重写此方法以添加自定义的启动前准备工作。
     *
     * @return 表示准备工作完成的 Future
     */
    protected Future<Void> starting() {
        getUnitLogger().debug("starting...");
        return Future.succeededFuture();
    }

    /**
     * 结束时的清理工作。
     * <p>
     * 私掠船提供默认实现：记录结束日志并返回成功的 Future。
     * 无论启动成功或失败，此方法都会被调用。
     * <p>
     * 子类可以重写此方法以添加自定义的清理逻辑。
     *
     * @return 表示清理工作完成的 Future
     */
    protected Future<Void> ending() {
        getUnitLogger().debug("ending...");
        return Future.succeededFuture();
    }

    /**
     * 战舰启航后的处理逻辑。
     * <p>
     * 私掠船在完成所有任务后，通过调用 {@link System#exit(int)} 强制退出程序。
     * 这种设计确保了私掠船作为快速本地测试工具能够及时释放资源并退出。
     *
     * @param startTime 战舰启航的时间戳
     */
    @Override
    protected void whenWarshipSetOff(long startTime) {
        super.whenWarshipSetOff(startTime);
        System.exit(0);
    }
}
