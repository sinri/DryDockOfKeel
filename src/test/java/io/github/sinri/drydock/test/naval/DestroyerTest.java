package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.melee.Destroyer;
import io.github.sinri.keel.core.servant.queue.KeelQueueSignal;
import io.github.sinri.keel.core.servant.queue.KeelQueueTask;
import io.github.sinri.keel.core.servant.queue.QueueManageIssueRecord;
import io.github.sinri.keel.core.servant.sundial.KeelSundialPlan;
import io.github.sinri.keel.core.servant.sundial.SundialIssueRecord;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import javax.annotation.Nonnull;
import java.util.Collection;

public class DestroyerTest extends Destroyer {

    public static void main(String[] args) {
        new DestroyerTest().launch();
    }

    @Override
    public Future<Collection<KeelSundialPlan>> fetchSundialPlans(KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder) {
        return Future.succeededFuture();
    }

    @Override
    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> launchAsDestroyer() {
        return Future.succeededFuture();
    }

    @Override
    public void configureHttpServerRoutes(Router router, KeelIssueRecorder<KeelEventLog> httpServerLogger) {
        router.route("/").handler(routingContext -> {
            routingContext.json(new JsonObject().put("a", "b"));
        });
    }

    /**
     * @since 1.5.2
     */
    @Nonnull
    @Override
    public Future<Void> beforeStartHttpServer() {
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> prepareDataSources() {
        return Future.succeededFuture();
    }

    @Override
    public Future<KeelQueueSignal> readSignal(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder) {
        return Future.succeededFuture(KeelQueueSignal.RUN);
    }

    @Override
    public Future<KeelQueueTask> seekNextTask(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder) {
        return Future.succeededFuture(null);
    }
}
