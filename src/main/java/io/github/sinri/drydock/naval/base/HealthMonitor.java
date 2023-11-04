package io.github.sinri.drydock.naval.base;

import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * @since 1.0.0
 */
public class HealthMonitor extends KeelVerticleBase {
    private final KeelEventLogger topicEventLogger;

    public HealthMonitor(KeelEventLogger topicEventLogger) {
        this.topicEventLogger = topicEventLogger;
    }

    protected KeelEventLogger getTopicEventLogger() {
        return this.topicEventLogger;
    }

    /**
     * 如果需要初始化一些数据记录器等，就在这里。
     */
    protected void prepare() {

    }

    @Nullable
    protected JsonObject moreEntriesForSnapshot() {
        return null;
    }

    @Override
    public void start() {
        prepare();
        new KeelRuntimeMonitor().startRuntimeMonitor(
                60_000L,
                monitorSnapshot -> {
                    topicEventLogger.info(eventLog -> {
                        var snapshot = new JsonObject()
                                .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                                .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                                .put("memory", monitorSnapshot.getMemory().toJsonObject());
                        JsonObject entries = moreEntriesForSnapshot();
                        if (entries != null) {
                            entries.forEach(entry -> snapshot.put(entry.getKey(), entry.getValue()));
                        }
                        eventLog.put("snapshot", snapshot);
                    });
                }
        );
    }
}
