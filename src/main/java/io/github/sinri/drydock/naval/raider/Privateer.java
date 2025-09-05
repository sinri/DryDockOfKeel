package io.github.sinri.drydock.naval.raider;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.facade.cli.KeelCliArgsParser;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Abstract base class for Privateer, providing a quick local testing and development startup framework.
 * <p>
 * Privateer is a special type of warship designed for rapid local testing and development scenarios.
 * It implements automatic startup of subclasses through reflection mechanism and provides complete lifecycle
 * management.
 * <p>
 * Key features:
 * <ul>
 *   <li>Automatic startup of subclass instances through reflection</li>
 *   <li>Complete configuration loading and logging management</li>
 *   <li>Virtual thread support (JDK 21+)</li>
 *   <li>Automatic program exit after task completion</li>
 * </ul>
 * <p>
 * Usage: Subclasses only need to implement the {@link #launchAsPrivateer()} method,
 * then directly run the main method to start.
 */
public abstract class Privateer extends Warship {
    /**
     * Synchronization lock used to control program exit.
     * <p>
     * Created in the main method and released after startup completion through the {@link #launchAsWarship()} method,
     * ensuring the program can exit properly.
     */
    private CountDownLatch countDownLatch;

    /**
     * Main entry point for Privateer.
     * <p>
     * Dynamically creates and starts an instance of the calling class through reflection mechanism.
     * This design allows subclasses to directly use the main method for startup without duplicating startup logic.
     *
     * @param args Command line arguments (currently unused)
     * @throws ClassNotFoundException    When the calling class cannot be found
     * @throws NoSuchMethodException     When the calling class lacks a no-argument constructor
     * @throws InvocationTargetException When constructor invocation fails
     * @throws InstantiationException    When the calling class cannot be instantiated
     * @throws IllegalAccessException    When access to constructor is denied
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
        testInstance.launch(args);

        testInstance.countDownLatch.await();
    }

    @Nullable
    @Override
    protected KeelCliArgsParser buildCliArgParser() {
        return null;
    }

    /**
     * Builds the event log record center.
     * <p>
     * Privateer provides a default implementation: uses a simple output center that directly outputs logs to console,
     * suitable for development and testing environments.
     * <p>
     * Subclasses can override this method to provide custom log record center implementation.
     *
     * @return Output-type event log record center
     */
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }

    /**
     * Builds Vert.x options configuration.
     * <p>
     * Privateer provides a default implementation: uses default Vert.x configuration, suitable for most development and
     * testing scenarios.
     * <p>
     * Subclasses can override this method to provide custom Vert.x configuration.
     *
     * @return Default VertxOptions instance
     */
    @Nonnull
    @Override
    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    /**
     * Loads local configuration file.
     * <p>
     * Privateer provides a default implementation: attempts to load "config.properties" configuration file.
     * If the file does not exist, the operation fails silently.
     * <p>
     * Subclasses can override this method to implement custom local configuration loading logic.
     */
    @Override
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }


    /**
     * Specific implementation for launching as a warship.
     * <p>
     * Startup process:
     * <ol>
     *   <li>Set log level to DEBUG</li>
     *   <li>Execute pre-startup preparation work {@link #starting()}</li>
     *   <li>Call subclass implementation of {@link #launchAsPrivateer()}</li>
     *   <li>Execute cleanup work {@link #ending()} regardless of success or failure</li>
     * </ol>
     * <p>
     * Since version 2.1.1, virtual thread features will be automatically applied in JDK 21+ environments.
     *
     * @return Future representing startup completion
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
     * Privateer-specific startup logic.
     * <p>
     * Subclasses must implement this method to define specific business logic.
     * This method is called after infrastructure initialization is complete.
     * <p>
     * Since version 2.1.1, virtual thread features will be automatically applied in JDK 21+ environments.
     *
     * @return Future representing Privateer startup completion
     */
    abstract protected Future<Void> launchAsPrivateer();

    /**
     * Pre-startup preparation work.
     * <p>
     * Privateer provides a default implementation: logs startup message and returns a successful Future.
     * <p>
     * Subclasses can override this method to add custom pre-startup preparation work.
     *
     * @return Future representing completion of preparation work
     */
    protected Future<Void> starting() {
        getUnitLogger().debug("starting...");
        return Future.succeededFuture();
    }

    /**
     * Cleanup work at the end.
     * <p>
     * Privateer provides a default implementation: logs ending message and returns a successful Future.
     * This method will be called regardless of whether startup succeeds or fails.
     * <p>
     * Subclasses can override this method to add custom cleanup logic.
     *
     * @return Future representing completion of cleanup work
     */
    protected Future<Void> ending() {
        getUnitLogger().debug("ending...");
        return Future.succeededFuture();
    }

    /**
     * Post-launch processing logic for warship.
     * <p>
     * After completing all tasks, Privateer forces program exit by calling {@link System#exit(int)}.
     * This design ensures that Privateer, as a rapid local testing tool, can timely release resources and exit.
     *
     * @param startTime Timestamp when the warship launched
     */
    @Override
    protected void whenLaunched(long startTime) {
        super.whenLaunched(startTime);
        System.exit(0);
    }
}
