package io.github.sinri.drydock.test;

import io.github.sinri.drydock.destroyer.Destroyer;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class DestroyerTest extends Destroyer {
    public DestroyerTest(String configPropertiesFile, boolean useSundial, boolean useFunnel) {
        super(configPropertiesFile, useSundial, useFunnel);
    }

    public static void main(String[] args) {
        new DestroyerTest("config.properties",true,true)
                .launch();
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
