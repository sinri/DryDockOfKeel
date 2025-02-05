package io.github.sinri.drydock.aviation.carrier;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
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
 * A base class for a unit as a program entrance with command line options.
 * Default KeelIssueRecordCenter and KeelEventLogger provided.
 *
 * @see <a href="https://vertx.io/docs/vertx-core/java/#_vert_x_command_line_interface_api">Vert.x Command Line Interface API</a>
 * @since 1.5.0 Technical Preview
 */
public abstract class AircraftCarrierDeck implements CommonUnit {

    protected KeelIssueRecordCenter issueRecordCenter;
    private KeelEventLogger unitLogger;

    /**
     * Launch this unit in `main` method with `args`.
     *
     * @param args the arguments provided in `main` method.
     */
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

        // as of 1.5.11: not to catch exception
        var commandLine = cli.parse(List.of(args));
        runWithCommandLine(commandLine);
    }

    /**
     * @return a list of Argument to be defined for command line parsing.
     */
    @Nullable
    protected List<Argument> buildCliArguments() {
        return null;
    }

    /**
     * @return a list of Option to be defined for command line parsing.
     */
    @Nullable
    protected List<Option> buildCliOptions() {
        return null;
    }

    /**
     * @return The command line program name.
     */
    @Nonnull
    protected abstract String buildCliName();

    /**
     * @return The command line program description.
     */
    @Nonnull
    protected abstract String buildCliDescription();

    /**
     * The handler with the parsed command line parameters, carries the whole program lifecycle.
     *
     * @param commandLine a CommandLine instance contains the parsed command line parameters.
     */
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
