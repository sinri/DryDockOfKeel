package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.facade.cli.KeelCliArgsParser;
import io.github.sinri.keel.facade.cli.KeelCliOption;

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
public abstract class AircraftCarrierDeck extends Warship {

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

    @Override
    public void handleError(Throwable throwable) {
        getUnitLogger().error("Program: " + buildCliName());
        getUnitLogger().error("Description: " + buildCliDescription());
        super.handleError(throwable);
    }
}
