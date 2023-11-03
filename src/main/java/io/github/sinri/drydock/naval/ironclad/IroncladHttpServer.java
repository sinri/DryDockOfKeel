package io.github.sinri.drydock.naval.ironclad;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.ext.web.Router;

/**
 * @since 1.1.0
 */
public class IroncladHttpServer extends KeelHttpServer {
    private final Ironclad ironclad;

    public IroncladHttpServer(Ironclad ironclad) {
        this.ironclad = ironclad;
    }

    @Override
    protected KeelEventLogger createLogger() {
        return this.ironclad.getNavalLogger();
    }

    @Override
    protected int getHttpServerPort() {
        return this.ironclad.getHttpServerPort();
    }

    @Override
    protected void configureRoutes(Router router) {
        this.ironclad.getHttpServerRouteHandler(router);
    }
}
