package ameba.util;

import com.google.common.primitives.Ints;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * @author icode
 */
public class Times {
    private static final PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d").appendSeparatorIfFieldsAfter(" ")
            .appendHours().appendSuffix("h").appendSeparatorIfFieldsAfter(" ")
            .appendMinutes().appendSuffix("min").appendSeparatorIfFieldsAfter(" ")
            .appendSeconds().appendSuffix("s").appendSeparatorIfFieldsAfter(" ")
            .toFormatter();

    private Times() {
    }

    public static Period parsePeriod(String duration) {
        return formatter.parsePeriod(duration);
    }


    public static Duration parseDuration(String duration) {
        return parsePeriod(duration).toStandardDuration();
    }

    public static long parseDurationToMillis(String duration) {
        return parseDuration(duration).getMillis();
    }

    public static int parseDurationToSeconds(String duration) {
        return Ints.checkedCast(parseDuration(duration).getStandardSeconds());
    }
}
