package io.github.sinri.drydock.air.base;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.3.0 Technical Preview
 */
@Deprecated(since = "1.4.7")
public abstract class Biplane extends Plane {
    /**
     * @since 1.3.1
     */
    private static Biplane plane;

    public Biplane() {

    }

    /**
     * @since 1.3.1
     */

    private static <T extends Biplane> void setPlane(T plane) {
        Biplane.plane = plane;
    }

    /**
     * @since 1.3.1
     */

    public static <T extends Biplane> T getPlane(Class<T> tClass) {
        if (tClass.isInstance(plane)) {
            return tClass.cast(plane);
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    protected void loadLocalConfiguration() {
        // load the local config file
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    abstract protected Future<Void> loadRemoteConfiguration();

    abstract protected Future<Void> flyAsBiplane();

    @Nonnull
    abstract protected Future<Void> prepareDataSources();

    @Override
    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        // since 1.3.1 add this;
        // todo If the verticle is running parallel, it may be overwritten.
        setPlane(this);
        // now local config has been loaded
        Future.succeededFuture()
                .compose(v -> {
                    return loadRemoteConfiguration();
                })
                .compose(remoteConfigurationLoaded -> {
                    setIssueRecordCenter(buildIssueRecordCenter());
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return prepareDataSources();
                })
                .compose(v -> {
                    return flyAsBiplane();
                })
                .andThen(ar -> {
                    if (ar.failed()) {
                        getLogger().exception(ar.cause(), "Failed to start flying");
                        startPromise.fail(ar.cause());
                    } else {
                        startPromise.complete();
                    }
                });
    }
}
