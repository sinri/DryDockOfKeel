package io.github.sinri.drydock.common;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

import javax.annotation.Nonnull;

/**
 * The mixin interface for a unit for HTTP Server.
 */
public interface HttpServerMixin extends CommonUnit {

    /**
     * Try to build a KeelHttpServer instance and start it up. Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
    default Future<String> loadHttpServer() {
        return Future.succeededFuture(buildHttpServer())
                     .compose(server -> {
                         if (server == null) return Future.succeededFuture();
                         return Future.succeededFuture()
                                      .compose(v -> {
                                          return beforeStartHttpServer();
                                      })
                                      .compose(v -> {
                                          return server.deployMe(new DeploymentOptions());
                                      });
                     });
    }

    /**
     * To generate a KeelHttpServer instance
     *
     * @return A built KeelHttpServer instance.
     */
    default KeelHttpServer buildHttpServer() {
        KeelIssueRecorder<KeelEventLog> issueRecorder = this.generateIssueRecorder(DryDockLogTopics.TopicHttpServer);
        return new KeelHttpServer() {

            @Override
            protected int getHttpServerPort() {
                return configuredHttpServerPort();
            }

            @Override
            protected void configureRoutes(Router router) {
                KeelIssueRecorder<KeelEventLog> httpServerLogger = getHttpServerLogger();
                configureHttpServerRoutes(router, httpServerLogger);
            }

            @Nonnull
            @Override
            protected KeelIssueRecorder<KeelEventLog> buildHttpServerIssueRecorder() {
                return issueRecorder;
            }
        };
    }

    /**
     * @return The port which HTTP Server listens to.
     */
    default int configuredHttpServerPort() {
        return 8080;
    }

    /**
     * Provide the Router instance of HTTP Server, configure it.
     * <p>
     * 最简单的情况下，铁甲舰仅需提供一份战术指南即可自动部署武器接敌。 其实就是设定 Vertx Web Server 的路由啦。
     * @param httpServerLogger 自2.0.4新增
     */
    void configureHttpServerRoutes(Router router, KeelIssueRecorder<KeelEventLog> httpServerLogger);

    /**
     * An asynchronous routine to be handled before starting HTTP Service.
     *
     * @since 1.5.2
     */
    @Nonnull
    Future<Void> beforeStartHttpServer();

    /**
     * Declare the HTTP Server should be stopped. Note that, the HTTP Server is not ought to be actually stopped, but a
     * declaration to be ready to close it.
     *
     * @since 1.4.17
     */
    void stopServer();

    /**
     * Check whether the HTTP Server has been declared to be stopped.
     *
     * @since 1.4.17
     */
    boolean isToStopServer();
}
