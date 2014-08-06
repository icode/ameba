package ameba.util;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
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
            .appendSeconds().appendSuffix("s")
            .toFormatter();

    private Times() {
    }

    public static Period parsePeriod(String duration) {
        if (StringUtils.isBlank(duration)) return null;
        return formatter.parsePeriod(duration);
    }


    public static Duration parseDuration(String duration) {
        if (StringUtils.isBlank(duration)) return null;
        return parsePeriod(duration).toStandardDuration();
    }

    public static long parseToMillis(String duration) {
        if (StringUtils.isBlank(duration)) return 0;
        return parseDuration(duration).getMillis();
    }

    public static int parseToSeconds(String duration) {
        if (StringUtils.isBlank(duration)) return 0;
        return Ints.checkedCast(parseDuration(duration).getStandardSeconds());
    }
}
