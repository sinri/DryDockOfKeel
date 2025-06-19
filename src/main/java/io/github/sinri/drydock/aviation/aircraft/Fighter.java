package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 和AircraftCarrierDeck配合使用的舰载战斗机类，用于应对来犯的外部请求。
 *
 * @since 1.5.0
 */
public abstract class Fighter extends Biplane {

    private final int port;
    /**
     * @since 1.4.17
     */
    private final AtomicBoolean stopServerSwitch = new AtomicBoolean(false);

    public Fighter(@Nonnull AircraftCarrierDeck deck, int port) {
        super(deck);
        this.port = port;
    }

    public int configuredHttpServerPort() {
        return port;
    }

    public final void stopServer() {
        this.stopServerSwitch.set(true);
    }

    public final boolean isToStopServer() {
        return stopServerSwitch.get();
    }

    /**
     * Try to build a KeelHttpServer instance and start it up. Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
    public Future<String> loadHttpServer() {
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
    protected KeelHttpServer buildHttpServer() {
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
     * Provide the Router instance of HTTP Server, configure it.
     * <p>
     * 最简单的情况下，铁甲舰仅需提供一份战术指南即可自动部署武器接敌。 其实就是设定 Vertx Web Server 的路由啦。
     *
     * @param httpServerLogger 自2.0.4新增
     */
    abstract protected void configureHttpServerRoutes(Router router, KeelIssueRecorder<KeelEventLog> httpServerLogger);

    /**
     * An asynchronous routine to be handled before starting HTTP Service.
     *
     * @since 1.5.2
     */
    @Nonnull
    abstract protected Future<Void> beforeStartHttpServer();


}
