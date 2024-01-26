package io.github.sinri.drydock.common;

import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;

/**
 * @since 1.0.0
 */
public class HealthMonitor extends KeelVerticleBase {
    private final long startTimestamp;

    public HealthMonitor() {
        startTimestamp = System.currentTimeMillis();
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

        // since 1.2.10 add a boot record
        getLogger().notice("BOOTED");

        new KeelRuntimeMonitor().startRuntimeMonitor(
                60_000L,
                monitorSnapshot -> {
                    getLogger().info(eventLog -> {
                        var snapshot = new JsonObject()
                                .put("survived", System.currentTimeMillis() - startTimestamp)
                                .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                                .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                                .put("hardware_memory", monitorSnapshot.getHardwareMemory().toJsonObject())
                                .put("jvm_memory", monitorSnapshot.getJvmMemory().toJsonObject())
                                .put("jvm_heap_memory", monitorSnapshot.getJvmHeapMemory().toJsonObject());
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
