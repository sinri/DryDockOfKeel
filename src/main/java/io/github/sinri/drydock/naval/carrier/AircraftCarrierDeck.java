package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @since 1.5.0 Technical Preview
 */
@TechnicalPreview(since = "1.5.0")
public abstract class AircraftCarrierDeck implements CommonUnit {

    protected KeelIssueRecordCenter issueRecordCenter;
    private KeelEventLogger unitLogger;

    public final void launch(String[] args) {
        issueRecordCenter = KeelIssueRecordCenter.outputCenter();
        unitLogger = generateEventLogger(DryDockLogTopics.TopicDryDock);

        var cli = CLI.create(buildCliName())
                .setDescription(buildCliDescription());

        List<Option> cliOptions = buildCliOptions();
        if (cliOptions != null) {
            cli.addOptions(cliOptions);
        }

        List<Argument> cliArguments = buildCliArguments();
        if (cliArguments != null) {
            cli.addArguments(cliArguments);
        }

        try {
            var commandLine = cli.parse(List.of(args));
            runWithCommandLine(commandLine);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Nullable
    protected List<Argument> buildCliArguments() {
        return null;
    }

    @Nullable
    protected List<Option> buildCliOptions() {
        return null;
    }

    @Nonnull
    protected abstract String buildCliName();

    @Nonnull
    protected abstract String buildCliDescription();

    abstract protected void runWithCommandLine(@Nonnull CommandLine commandLine);

    @Override
    public KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    @Override
    public KeelEventLogger getLogger() {
        return unitLogger;
    }
}
