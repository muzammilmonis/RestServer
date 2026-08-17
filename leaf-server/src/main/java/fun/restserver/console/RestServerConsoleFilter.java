package fun.restserver.console;

import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

/**
 * Conservative clean-console filter. It only removes known non-actionable INFO noise;
 * WARN, ERROR and FATAL events are never hidden.
 */
public final class RestServerConsoleFilter extends AbstractFilter {
    public static final RestServerConsoleFilter INSTANCE = new RestServerConsoleFilter();

    private static final List<String> EXACT_NOISE = List.of(
        "Timings Reset"
    );

    private RestServerConsoleFilter() {
        super(Filter.Result.NEUTRAL, Filter.Result.NEUTRAL);
    }

    @Override
    public Result filter(final LogEvent event) {
        if (event == null || event.getLevel().isMoreSpecificThan(Level.WARN)) {
            return Result.NEUTRAL;
        }
        if (event.getLevel() != Level.INFO || event.getMessage() == null) {
            return Result.NEUTRAL;
        }

        final String message = event.getMessage().getFormattedMessage();
        if (EXACT_NOISE.contains(message)) {
            return Result.DENY;
        }
        return Result.NEUTRAL;
    }
}
