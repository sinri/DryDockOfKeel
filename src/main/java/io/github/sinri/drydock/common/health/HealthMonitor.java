package io.github.sinri.drydock.common.health;

import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.verticles.KeelVerticleImplPure;

import javax.annotation.Nonnull;

/**
 * @since 1.0.0
 * @since 1.4.0 be abstract for two implementations.
 */
public abstract class HealthMonitor<X> extends KeelVerticleImplPure {
    protected final long startTimestamp;

    public HealthMonitor() {
        startTimestamp = System.currentTimeMillis();
    }

    /**
     * 如果需要初始化一些数据记录器等，就在这里。
     * - modify the value of recordWithMetric and recordWithEvent;
     * - initialize metricRecorder (but not start)
     */
    abstract protected void prepare();

    abstract protected void moreMonitorItems(@Nonnull final X draft);

    abstract protected X createDraft();

    abstract protected void handleRecord(@Nonnull MonitorSnapshot monitorSnapshot, @Nonnull X moreDraft);


    @Override
    protected void startAsPureKeelVerticle() {
        prepare();
        new KeelRuntimeMonitor().startRuntimeMonitor(
                interval(),
                monitorSnapshot -> {
                    X draft = createDraft();
                    moreMonitorItems(draft);

                    handleRecord(monitorSnapshot, draft);
                }
        );
    }

    /**
     * @since 1.4.2
     */
    protected long interval() {
        return 60_000L;
    }
}
