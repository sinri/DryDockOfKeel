package io.github.sinri.drydock.test;

import io.github.sinri.drydock.ironclad.Ironclad;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class IroncladTest extends Ironclad {
    public static void main(String[] args) {
        new IroncladTest().launch();
    }

    @Override
    protected void launchAsIronclad() {

    }

    @Override
    protected KeelHttpServer buildHttpServer() {
        return new HS();
    }

    public static class HS extends KeelHttpServer {

        // default port is 8080

        @Override
        protected void configureRoutes(Router router) {
            router.routeWithRegex("/.+").handler(routingContext -> {
                routingContext.json(new JsonObject().put("path", routingContext.request().path()));
            });
        }
    }
}
