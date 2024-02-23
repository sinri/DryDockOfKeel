package io.github.sinri.drydock.naval.melee;

import io.github.sinri.drydock.common.HttpServerMixin;
import io.vertx.core.Future;

/**
 * 铁甲舰。
 * 用于实现 SAE APP 环境下支持横向弹性扩展的HTTP服务应用，核心能力是一门主炮（HTTP服务器）。
 * Based on Caravel.
 * HTTP Server Supported.
 *
 * @since 1.0.0
 * @since 1.1.0 Use IroncladHttpServer to ease
 */
public abstract class Ironclad extends Caravel implements HttpServerMixin {

    /**
     * 航海日志已可报告给应用日志中心。
     * 数据源等已加载。
     * 在桨帆船的基础上增加了健康检查模块。
     * 先加载必要模块如数据缓存服务等，才能启动对外网络服务。
     */
    @Override
    final protected Future<Void> launchAsCaravel() {
        return launchAsIronclad()
                .compose(coalLoaded -> {
                    getUnitLogger().info("Coal Loaded. Ensured equipments as Ironclad.");
                    return loadHttpServer();
                });
    }

    /**
     * TO LOAD COAL.
     * 铁甲舰先加载煤才能启动锅炉来启动引擎。
     */
    abstract protected Future<Void> launchAsIronclad();

}
