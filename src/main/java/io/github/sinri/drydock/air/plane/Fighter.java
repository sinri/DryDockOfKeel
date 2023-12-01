package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.naval.base.AliyunSLSAdapterImpl;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;

@TechnicalPreview
abstract public class Fighter extends Biplane {

    @Override
    protected KeelEventLogCenter buildLogCenter() {
        try {
            return new KeelAsyncEventLogCenter(new AliyunSLSAdapterImpl());
        } catch (Throwable e) {
            getLogger().exception(e, "Failed in io.github.sinri.drydock.air.plane.Fighter.buildLogCenter");
            throw e;
        }
    }

    @Override
    protected void flyAsBiplane() {
        // 飞行日志共享大计
        var bypassLogger = generateLogger(
                AliyunSLSAdapterImpl.TopicFlight,
                log -> log.put("plane", getClass().getName())
        );
        this.getLogger().addBypassLogger(bypassLogger);
    }
}
