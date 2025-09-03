package io.github.sinri.drydock.aviation.carrier;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.facade.cli.*;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

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
 * <p>
 * As of 2.1.1, the CLI module use {@link KeelCliArgsParser} to parse command line arguments.
 *
 * @since 1.5.0
 */
public abstract class AircraftCarrierDeck implements CommonUnit {

    private KeelIssueRecordCenter issueRecordCenter;
    private KeelIssueRecorder<KeelEventLog> unitLogger;
    private KeelCliArgs cliArgs;

    /**
     * Launch this unit in `main` method with `args`.
     *
     * @param args the arguments provided in `main` method.
     */
    public final void launch(String[] args) {
        issueRecordCenter = KeelIssueRecordCenter.outputCenter();
        unitLogger = generateIssueRecorder(DryDockLogTopics.TopicDryDock, KeelEventLog::new);

        try {
            var argsParser = KeelCliArgsParser.create();
            // Add options if any
            var options = buildCliOptions();
            if (options != null) {
                for (var option : options) {
                    argsParser.addOption(option);
                }
            }
            this.cliArgs = argsParser.parse(args);
            runWithCommandLine();
        } catch (KeelCliArgsDefinitionError e) {
            unitLogger.exception(e, "Failed to build command line parser.");
            System.exit(1);
        } catch (KeelCliArgsParseError e) {
            // unitLogger.exception(e, "Failed to parse command line.");
            System.out.printf("=== %s ===%n", buildCliName());
            System.out.printf(buildCliDescription());
            System.out.println("=== Arguments Error ===");
            System.out.println(e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Retrieves the parsed command line arguments for the program.
     * This method ensures that the command line arguments have been initialized
     * during the execution of the `launch` method.
     *
     * @return the {@link KeelCliArgs} object containing parsed command line arguments.
     * @throws IllegalStateException if the command line arguments are not initialized yet.
     * @since 2.1.1
     */
    @Nonnull
    protected KeelCliArgs getCliArgs() {
        if (cliArgs == null) {
            throw new IllegalStateException("The command line arguments are not initialized yet.");
        }
        return cliArgs;
    }

    @Nullable
    protected List<KeelCliOption> buildCliOptions() {
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
     * Executes the main functionality of the program using parsed command-line arguments.
     * This method is invoked after the command-line arguments have been prepared and initialized.
     * Subclasses should provide the specific implementation to execute the desired tasks
     * based on the command-line arguments parsed, which could be gotten with {@link #getCliArgs()}.
     * <p>
     * The method should run in the main thread and keep awaiting any sync and async tasks done.
     */
    protected abstract void runWithCommandLine();

    @Override
    public KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    /**
     * @since 2.1.3
     */
    protected void replaceIssueRecordCenter(@Nonnull KeelIssueRecordCenter issueRecordCenter) {
        this.issueRecordCenter = issueRecordCenter;
        this.unitLogger = generateIssueRecorder(DryDockLogTopics.TopicDryDock, KeelEventLog::new);
    }

    public KeelIssueRecorder<KeelEventLog> getUnitLogger() {
        return unitLogger;
    }
}
