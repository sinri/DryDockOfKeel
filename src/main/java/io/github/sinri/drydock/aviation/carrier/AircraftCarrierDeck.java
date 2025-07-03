package io.github.sinri.drydock.aviation.carrier;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A base class for a unit as a program entrance with command line options.
 * Default KeelIssueRecordCenter and KeelEventLogger provided.
 * <p>
 * The start-up command line is
 * {@code java -jar X.jar [...]}
 * <p>
 * This class is designed to be the base of the Program Entrance Class,
 * which would contain the `main` method where the `launch` method should be
 * called.
 * <p>
 * As of 2.1.0, the CLI module of vert.x is deprecated, so we use Picocli instead follow vert.x docs.
 *
 * @see <a href="https://picocli.info/picocli-programmatic-api.html">Programmatic API of Picocli</a>
 * @since 1.5.0
 */
public abstract class AircraftCarrierDeck implements CommonUnit {

    protected KeelIssueRecordCenter issueRecordCenter;
    private KeelIssueRecorder<KeelEventLog> unitLogger;

    /**
     * Launch this unit in `main` method with `args`.
     *
     * @param args the arguments provided in `main` method.
     */
    public final void launch(String[] args) {
        issueRecordCenter = KeelIssueRecordCenter.outputCenter();
        unitLogger = generateIssueRecorder(DryDockLogTopics.TopicDryDock, KeelEventLog::new);

        CommandSpec spec = CommandSpec.create();
        spec.name(buildCliName());
        spec.usageMessage().description(buildCliDescription());

        // Add options if any
        List<picocli.CommandLine.Model.OptionSpec> options = buildCliOptions();
        if (options != null) {
            for (picocli.CommandLine.Model.OptionSpec option : options) {
                spec.add(option);
            }
        }

        // Add positional parameters if any
        List<picocli.CommandLine.Model.PositionalParamSpec> arguments = buildCliArguments();
        if (arguments != null) {
            for (picocli.CommandLine.Model.PositionalParamSpec argument : arguments) {
                spec.add(argument);
            }
        }

        CommandLine cmd = new CommandLine(spec);

        CommandLine.ParseResult parseResult = cmd.parseArgs(args);
        runWithCommandLine(parseResult);
    }

    @Nullable
    protected List<picocli.CommandLine.Model.OptionSpec> buildCliOptions() {
        return null;
    }

    @Nullable
    protected List<picocli.CommandLine.Model.PositionalParamSpec> buildCliArguments() {
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

    protected abstract void runWithCommandLine(CommandLine.ParseResult parseResult) throws CommandLine.ExecutionException, CommandLine.ParameterException;

    @Override
    public KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    public KeelIssueRecorder<KeelEventLog> getUnitLogger() {
        return unitLogger;
    }
}
