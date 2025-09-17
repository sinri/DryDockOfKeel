package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@ExtendWith(VertxExtension.class)
class AliyunSLSIssueAdapterImplTest extends KeelJUnit5Test {
    private final AliyunSLSIssueAdapterImpl aliyunSLSIssueAdapter;

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public AliyunSLSIssueAdapterImplTest(Vertx vertx) {
        super(vertx);
        try {
            aliyunSLSIssueAdapter = new AliyunSLSIssueAdapterImpl();
        } catch (AliyunSLSDisabled e) {
            throw new RuntimeException(e);
        }
    }

    // @Test
    void test(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        KeelIssueRecordCenter center = KeelIssueRecordCenter.build(aliyunSLSIssueAdapter);
        KeelIssueRecorder<KeelEventLog> issueRecorder = center.generateIssueRecorder("Debug", KeelEventLog::new);
        Keel.asyncCallStepwise(10, i -> {
                issueRecorder.info("msg: " + i);
                return Keel.asyncSleep(1000L);
            })
            .andThen(ar -> {
                checkpoint.flag();
            });
    }
}