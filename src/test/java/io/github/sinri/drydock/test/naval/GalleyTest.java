package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.galley.Galley;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;

public class GalleyTest extends Galley {
    @Override
    protected void launchAsGalley() {
        KeelEventLogger logger = generateLogger("GalleyTest", null);
        logger.info("launched");
        Keel.getVertx().close();
    }

    public static void main(String[] args) {
        new GalleyTest().launch();
    }
}
