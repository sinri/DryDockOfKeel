package io.github.sinri.drydock.naval.base;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogItem;
import io.github.sinri.keel.facade.KeelConfiguration;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.adapter.AliyunSLSAdapter;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 1.0.0
 */
public class AliyunSLSAdapterImpl implements AliyunSLSAdapter {
    public static final String TopicNaval = "DryDock::Naval";
    public static final String TopicFlight = "DryDock::Flight";
    public static final String TopicHealthMonitor = "HealthMonitor";
    public static final String TopicSundial = "Sundial";
    public static final String TopicFunnel = "Funnel";
    public static final String TopicQueue = "Queue";
    public static final String TopicReceptionist = "Receptionist";

    public static final String RESERVED_KEY_LEVEL = "level";
    private static boolean disabled;
    private final String project;
    private final String logstore;
    private final String source;
    private final Producer producer;
    private final String endpoint;

    public AliyunSLSAdapterImpl() {
        KeelConfiguration aliyunSlsConfig = Keel.getConfiguration().extract("aliyun", "sls");
        Objects.requireNonNull(aliyunSlsConfig);

        String disabledString = aliyunSlsConfig.readString("disabled");
        disabled = ("YES".equals(disabledString));

        this.project = aliyunSlsConfig.readString("project");
        this.logstore = aliyunSlsConfig.readString("logstore");
        this.endpoint = aliyunSlsConfig.readString("endpoint");
        this.source = buildSource(aliyunSlsConfig.readString("source"));

        if (!disabled) {
            String accessKeyId = aliyunSlsConfig.readString("accessKeyId");
            String accessKeySecret = aliyunSlsConfig.readString("accessKeySecret");

            producer = new LogProducer(new ProducerConfig());
            Objects.requireNonNull(project);
            Objects.requireNonNull(endpoint);
            Objects.requireNonNull(accessKeyId);
            Objects.requireNonNull(accessKeySecret);
            producer.putProjectConfig(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));

            //KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).info("Aliyun SLS Producer relied aliyunSlsConfig: " + aliyunSlsConfig.toJsonObject());
        } else {
            producer = null;
        }
    }

    /**
     * Build source from configuration.
     * Source Expression should be:
     * - EMPTY/BLANK STRING or NULL: use SLS default source generation;
     * - A TEMPLATED STRING
     * --- Rule 1: Replace [IP] to local address;
     *
     * @since 1.2.7 add source template and rule 1.
     */
    private static String buildSource(@Nullable String configuredSourceExpression) {
        if (configuredSourceExpression == null || configuredSourceExpression.isBlank()) {
            return "";
        }
        // Rule 1: Replace [IP] to local address
        String localHostAddress = KeelHelpers.netHelper().getLocalHostAddress();
        if (localHostAddress == null) {
            Keel.getLogger().warning("Could not get local host address for SLS source!");
            return "";
        }
        return configuredSourceExpression.replaceAll("\\[IP]", localHostAddress);
    }

    public static final Set<String> ignorableStackPackageSet = new HashSet<>();

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        if (!disabled) {
            try {
                this.producer.close();
                promise.complete();
            } catch (InterruptedException | ProducerException e) {
                promise.fail(e);
            }
        } else {
            promise.complete();
        }
    }

    static {
        ignorableStackPackageSet.add("io.vertx.");
        ignorableStackPackageSet.add("io.netty.");
        ignorableStackPackageSet.add("java.lang.");
    }

    @Override
    @Nonnull
    public Future<Void> dealWithLogsForOneTopic(@Nonnull String topic, @Nonnull List<KeelEventLog> list) {
        if (list.isEmpty()) return Future.succeededFuture();

//        System.out.println("dealWithLogsForOneTopic<"+topic+"> handling "+list.size()+" logs");

        if (disabled) {
            list.forEach(item -> {
                KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).log(log -> {
                    log.reloadDataFromJsonObject(item.toJsonObject());
                });
            });
            return Future.succeededFuture();
        }

        Promise<Void> promise = Promise.promise();

        try {
            List<LogItem> logItems = new ArrayList<>();

            list.forEach(eventLog -> {
                LogItem logItem = new LogItem(Math.toIntExact(eventLog.timestamp() / 1000));
                //logItem.PushBack("_timestamp_in_ms", eventLog.timestampExpression());
                logItem.PushBack(RESERVED_KEY_LEVEL, eventLog.level().name());
                eventLog.forEach(entry -> {
                    if (entry.getValue() == null) {
                        logItem.PushBack(entry.getKey(), null);
                    } else {
                        logItem.PushBack(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                });
                logItems.add(logItem);
            });

            producer.send(project, logstore, topic, source, logItems, result -> {
                if (!result.isSuccessful()) {
                    KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).error(eventLog -> {
                        eventLog.topic(getClass().getName()).message("Producer Send Error: " + result);
                    });
                }
                promise.complete(null);
            });
        } catch (Throwable e) {
            KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).exception(e, "Aliyun SLS Producer Exception");
            promise.fail(e);
        }

        return promise.future();
    }

    @Nullable
    @Override
    public Object processThrowable(@Nullable Throwable throwable) {
        return KeelHelpers.jsonHelper().renderThrowableChain(throwable, ignorableStackPackageSet);
    }
}
