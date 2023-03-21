package io.github.sinri.drydock.ironclad;

import io.github.sinri.drydock.caravel.Caravel;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;

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
