package io.github.sinri.drydock.common;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

public interface HttpServerMixin extends CommonUnit {

    default Future<Void> loadHttpServer() {
        return Future.succeededFuture(buildHttpServer())
                .compose(server -> {
                    if (server == null) return Future.succeededFuture();
                    return server.deployMe(new DeploymentOptions())
                            .onFailure(ironcladFailure -> {
                                getUnitLogger().exception(ironcladFailure, "Failed to start HTTP service.");
                            })
                            .compose(httpServerDeployed -> {
                                getUnitLogger().info("HTTP Service Started: " + httpServerDeployed);
                                return Future.succeededFuture();
                            });
                });
    }

    default KeelHttpServer buildHttpServer() {
        KeelEventLogger eventLogger = this.generateEventLogger(DryDockLogTopics.TopicHttpServer);
        return new KeelHttpServer() {

            @Override
            protected int getHttpServerPort() {
                return configuredHttpServerPort();
            }

            @Override
            protected void configureRoutes(Router router) {
                configureHttpServerRoutes(router);
            }

            @Override
            protected KeelEventLogger buildEventLogger() {
                return eventLogger;
            }
        };
    }

    /**
     * @return HTTP 服务的监听端口。
     */
    default int configuredHttpServerPort() {
        return 8080;
    }

    /**
     * 最简单的情况下，铁甲舰仅需提供一份战术指南即可自动部署武器接敌。
     * 其实就是设定 Vertx Web Server 的路由啦。
     */
    void configureHttpServerRoutes(Router router);
}
