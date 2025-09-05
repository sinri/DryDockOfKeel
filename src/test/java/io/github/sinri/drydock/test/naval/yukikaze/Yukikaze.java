package io.github.sinri.drydock.test.naval.yukikaze;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.facade.cli.KeelCliArgsParser;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class Yukikaze extends Warship {
    public static void main(String[] args) {
        new Yukikaze().launch(args);
    }

    @Nullable
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        return null;
    }

    @Nullable
    @Override
    protected KeelMetricRecorder buildMetricRecorder() {
        return null;
    }

    @Nonnull
    @Override
    protected VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    @Override
    protected Future<Void> launchAsWarship() {
        AtomicLong timerRef = new AtomicLong(0);
        AtomicInteger counter = new AtomicInteger(5);
        long timer = Keel.getVertx().setPeriodic(1000, id -> {
            getUnitLogger().info("Timer: " + id + " " + counter.getAndDecrement());
            if (counter.get() <= 0) {
                Keel.getVertx().cancelTimer(timerRef.get());
                this.finish();
            }
        });
        timerRef.set(timer);
        return Future.succeededFuture();
    }

    @Nullable
    @Override
    protected KeelCliArgsParser buildCliArgParser() {
        return null;
    }
}
