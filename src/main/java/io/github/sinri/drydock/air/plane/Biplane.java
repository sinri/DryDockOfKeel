package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.air.base.Plane;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;

@TechnicalPreview
public abstract class Biplane extends Plane {

    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    @Override
    public void fly() {
        flyAsBiplane();
    }

    abstract protected void flyAsBiplane();

}
