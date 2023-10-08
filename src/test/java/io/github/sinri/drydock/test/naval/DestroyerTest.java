package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.destroyer.Destroyer;
import io.github.sinri.keel.core.KeelCronExpression;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class DestroyerTest extends Destroyer {
    public DestroyerTest(String configPropertiesFile, boolean useSundial, boolean useFunnel) {
        super(configPropertiesFile, useSundial, useFunnel);
    }

    public static void main(String[] args) {
        new DestroyerTest("config.properties", true, true)
                .launch();
    }

    @Override
    protected Future<Collection<KeelSundialPlan>> reloadSundialPlans() {
        return Future.succeededFuture(List.of(new KeelSundialPlan() {
            @Override
            public String key() {
                return "each minute event";
            }

            @Override
            public KeelCronExpression cronExpression() {
                return new KeelCronExpression("* * * * *");
            }

            @Override
            public void execute(Calendar calendar) {
                KeelEventLogger logger = generateLogger("DestroyerTest-Sundial", null);
                logger.info("sundial event");
            }
        }));
    }

    @Override
    protected void launchAsDestroyer() {
        KeelEventLogger funnelLogger = generateLogger("DestroyerTest-Funnel", null);
        Keel.getVertx().setPeriodic(30_000L, timer -> {
            this.funnel(() -> {
                funnelLogger.info("funnel event");
                return Future.succeededFuture();
            });
        });
    }

    @Override
    protected KeelHttpServer buildHttpServer() {
        return new HS();
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
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
