package io.github.sinri.drydock.naval.ironclad;

import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;

/**
 * 铁甲舰。
 * Based on Caravel.
 * HTTP Server Supported.
 *
 * @since 1.0.0
 */
public abstract class Ironclad extends Caravel {
    public Ironclad(){
        super();
    }

    public Ironclad(String configPropertiesFile) {
        super(configPropertiesFile);
    }

    @Override
    final protected void launchAsCaravel() {
        buildHttpServer().deployMe(new DeploymentOptions());
        launchAsIronclad();
    }

    abstract protected void launchAsIronclad();

    abstract protected KeelHttpServer buildHttpServer();
}
