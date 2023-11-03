package io.github.sinri.drydock.naval.ironclad;

import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

/**
 * 铁甲舰。
 * 用于实现 SAE APP 环境下支持横向弹性扩展的HTTP服务应用，核心能力是一门主炮（HTTP服务器）。
 * Based on Caravel.
 * HTTP Server Supported.
 *
 * @since 1.0.0
 * @since 1.1.0 Use IroncladHttpServer to ease
 */
public abstract class Ironclad extends Caravel {

    /**
     * 先加载必要模块如数据缓存服务等，才能启动对外网络服务。
     */
    @Override
    final protected void launchAsCaravel() {
        launchAsIronclad()
                .compose(coalLoaded -> {
                    getNavalLogger().info("Coal Loaded. Ensured equipments as Ironclad.");

                    return buildHttpServer().deployMe(new DeploymentOptions());
                })
                .onSuccess(httpServerDeployer -> {
                    getNavalLogger().info("Ironclad HTTP Service Started.");
                })
                .onFailure(ironcladFailure -> {
                    getNavalLogger().exception(ironcladFailure, "Failed to ensure equipments as Ironclad to provide HTTP service.");
                    this.sink();
                });
    }

    /**
     * TO LOAD COAL.
     * 铁甲舰先加载煤才能启动锅炉来启动引擎。
     */
    abstract protected Future<Void> launchAsIronclad();

    /**
     * 铁甲舰的核心武器：HTTP 服务。
     */
    protected KeelHttpServer buildHttpServer() {
        return new IroncladHttpServer(this);
    }

    /**
     * @return HTTP 服务的监听端口。
     */
    protected int getHttpServerPort() {
        return 8080;
    }

    /**
     * 最简单的情况下，铁甲舰仅需提供一份战术指南即可自动部署武器接敌。
     * 其实就是设定 Vertx Web Server 的路由啦。
     */
    abstract protected void getHttpServerRouteHandler(Router router);
}
