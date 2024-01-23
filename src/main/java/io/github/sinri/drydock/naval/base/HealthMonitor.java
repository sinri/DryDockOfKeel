package io.github.sinri.drydock.naval.base;

import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;

/**
 * @since 1.0.0
 */
public class HealthMonitor extends KeelVerticleBase {
//    private final KeelEventLogger topicEventLogger;

//    public HealthMonitor(KeelEventLogger topicEventLogger) {
//        this.topicEventLogger = topicEventLogger;
//    }

    public HealthMonitor() {
//        this.topicEventLogger = KeelOutputEventLogCenter.getInstance().createLogger(AliyunSLSAdapterImpl.TopicHealthMonitor);
    }

//    protected KeelEventLogger getTopicEventLogger() {
//        return this.topicEventLogger;
//    }

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
                    getLogger().info(eventLog -> {
                        var snapshot = new JsonObject()
                                .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                                .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                                .put("hardware_memory", monitorSnapshot.getHardwareMemory().toJsonObject())
                                .put("jvm_memory", monitorSnapshot.getJvmMemory().toJsonObject())
                                .put("jvm_heap_memory", monitorSnapshot.getJvmHeapMemory().toJsonObject());

                        // removed since 1.2.6
                        //snapshot.put("memory", monitorSnapshot.getHardwareMemory().toJsonObject());

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
