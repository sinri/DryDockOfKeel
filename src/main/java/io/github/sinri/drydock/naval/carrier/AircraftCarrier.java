package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @since 1.2.2 Technical Preview
 */
@TechnicalPreview(since = "1.2.2")
public abstract class AircraftCarrier {

    public final void launch(String[] args) {
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

        var commandLine = cli.parse(List.of(args));
        runWithCommandLine(commandLine);
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
}
