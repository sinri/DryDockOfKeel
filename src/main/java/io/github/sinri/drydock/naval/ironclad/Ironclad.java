package io.github.sinri.drydock.naval.ironclad;

import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

/**
 * 铁甲舰。
 * Based on Caravel.
 * HTTP Server Supported.
 *
 * @since 1.0.0
 */
public abstract class Ironclad extends Caravel {
    public Ironclad() {
        super();
    }

    public Ironclad(String configPropertiesFile) {
        super(configPropertiesFile);
    }

    @Override
    final protected void launchAsCaravel() {
        Promise<Void> promiseIronclad = Promise.promise();
        launchAsIronclad(promiseIronclad);
        promiseIronclad.future()
                .onSuccess(ironcladDone -> {
                    getNavalLogger().info("Ensured equipments as Ironclad");

                    buildHttpServer().deployMe(new DeploymentOptions());
                }).onFailure(ironcladFailure -> {
                    getNavalLogger().exception(ironcladFailure, "Failed to ensure equipments as Ironclad");
                });
    }

    /**
     * @since 1.0.1 given a default implement
     */
    @Deprecated(since = "1.0.1")
    protected void launchAsIronclad() {
        // to be override for sync impl
    }

    protected void launchAsIronclad(Promise<Void> promise) {
        // to be override for async impl
        launchAsIronclad();
        promise.complete();
    }

    abstract protected KeelHttpServer buildHttpServer();
}
