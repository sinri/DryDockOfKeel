package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.facade.cli.KeelCliArgsParser;
import io.github.sinri.keel.facade.cli.KeelCliOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Aircraft carrier deck management system for naval operations.
 * <p>
 * Provides command line interface capabilities for aircraft carrier deck operations,
 * extending the base warship functionality with specialized deck management features.
 * <p>
 * This class handles CLI argument parsing and provides abstract methods for
 * defining program name and description specific to carrier deck operations.
 *
 * @since 3.0.0
 */
public abstract class AircraftCarrierDeck extends Warship {

    /**
     * Builds the CLI argument parser with configured options.
     * <p>
     * Creates a new parser instance and adds all options returned by
     * {@link #buildCliOptions()} method.
     *
     * @return configured CLI argument parser
     */
    @Nonnull
    @Override
    protected final KeelCliArgsParser buildCliArgParser() {
        KeelCliArgsParser keelCliArgsParser = KeelCliArgsParser.create();

        List<KeelCliOption> keelCliOptions = buildCliOptions();
        if (keelCliOptions != null) {
            keelCliOptions.forEach(keelCliArgsParser::addOption);
        }

        return keelCliArgsParser;
    }

    /**
     * Builds the list of CLI options for the carrier deck operations.
     * <p>
     * Subclasses should override this method to provide specific command line
     * options for their carrier deck implementation.
     *
     * @return list of CLI options, or null if no options are needed
     */
    @Nullable
    protected List<KeelCliOption> buildCliOptions() {
        return null;
    }

    /**
     * Builds the command line program name for carrier deck operations.
     *
     * @return the command line program name
     */
    @Nonnull
    protected abstract String buildCliName();

    /**
     * Builds the command line program description for carrier deck operations.
     *
     * @return the command line program description
     */
    @Nonnull
    protected abstract String buildCliDescription();

    /**
     * Handles errors during carrier deck operations.
     * <p>
     * Logs the program name and description before delegating to the parent
     * error handling mechanism.
     *
     * @param throwable the error to handle
     */
    @Override
    public void handleError(Throwable throwable) {
        getUnitLogger().error("Program: " + buildCliName());
        getUnitLogger().error("Description: " + buildCliDescription());
        super.handleError(throwable);
    }
}
