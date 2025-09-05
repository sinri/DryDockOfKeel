package io.github.sinri.drydock;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * A sample launcher for DryDock.
 *
 * @since 3.0.0
 */
public class DryDockerLauncher extends AircraftCarrierDeck {
    public static void main(String[] args) {
        new DryDockerLauncher().launch(args);
    }

    @Override
    @Nonnull
    protected String buildCliName() {
        return "DryDock Sample";
    }

    @Override
    @Nonnull
    protected String buildCliDescription() {
        return "A sample launcher for DryDock, would finish after 1s.";
    }

    @Override
    @Nullable
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        return null;
    }

    @Override
    @Nullable
    protected KeelMetricRecorder buildMetricRecorder() {
        return null;
    }

    @Override
    @Nonnull
    protected VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    @Override
    protected void loadLocalConfiguration() {
        // do nothing
    }

    @Override
    protected Future<Void> launchAsWarship() {
        getUnitLogger().info(buildCliName() + ": " + buildCliDescription());
        return Keel.asyncSleep(1000)
                   .onComplete(ar -> {
                       getUnitLogger().info("Finish!");
                       finish();
                   });
    }
}