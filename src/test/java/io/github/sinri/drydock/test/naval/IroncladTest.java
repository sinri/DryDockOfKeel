package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.melee.Ironclad;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import javax.annotation.Nonnull;

public class IroncladTest extends Ironclad {
    public static void main(String[] args) {
        new IroncladTest().launch();
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> launchAsIronclad() {
        return Future.succeededFuture();
    }

    @Override
    public void configureHttpServerRoutes(Router router) {
        router.route("/").handler(routingContext -> {
            routingContext.json(new JsonObject().put("a", "b"));
        });
    }

    @Nonnull
    @Override
    protected Future<Void> prepareDataSources() {
        return Future.succeededFuture();
    }
}
