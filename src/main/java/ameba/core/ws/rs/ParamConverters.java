package ameba.core.ws.rs;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.message.internal.HttpDateFormat;
import org.glassfish.jersey.server.internal.LocalizationMessages;

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.*;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>ParamConverters class.</p>
 *
 * @author icode
 */
public class ParamConverters {
    private static final String SYS_TZ;

    static {
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getRawOffset();
        int z = offset / (60 * 60 * 1000);
        String tzs = String.valueOf(Math.abs(z));
        if (z >= 0 && z <= 9) {
            tzs = "+0" + tzs;
        } else if (z >= 0) {
            tzs = "+" + tzs;
        } else if (z < 0 && z > -9) {
            tzs = "-0" + tzs;
        } else if (z < 0) {
            tzs = "-" + tzs;
        }
        z = (Math.abs(offset) / (60 * 1000)) % 60;
        if (z <= 9) {
            tzs += "0";
        }
        tzs += z;
        SYS_TZ = tzs;
    }

    private ParamConverters() {
    }

    /**
     * <p>parseTimestamp.</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.lang.Long} object.
     */
    public static Long parseTimestamp(String value) {
        if (value.matches("^[0-9]+$")) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * <p>parseDate.</p>
     *
     * @param value a {@link java.lang.String} object.
     * @param pos   a {@link java.text.ParsePosition} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date parseDate(String value, ParsePosition pos) {
        Long timestamp = parseTimestamp(value);
        if (timestamp != null) {
            return new Date(timestamp);
        }
        if (value.contains(" ")) {
            value = value.replace(" ", "+");
        }
        if (!(value.contains("-") || value.contains("+")) && !value.endsWith("Z")) {
            value += SYS_TZ;
        }
        try {
            return ISO8601Utils.parse(value, pos);
        } catch (ParseException e) {
            throw new ExtractorException(e);
        }
    }

    /**
     * <p>parseDate.</p>
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     */
    public static Date parseDate(String value) {
        return parseDate(value, new ParsePosition(0));
    }

    private static Instant parseInstant(String value) {
        return parseDate(value).toInstant();
    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the target Java {@link Enum enum} type instance
     * by invoking a static {@code fromString(String)} method on the target enum type.
     */
    @Singleton
    public static class TypeFromStringEnum implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            return (!Enum.class.isAssignableFrom(rawType)) ? null : new ParamConverter<T>() {
                @Override
                public T fromString(String value) {
                    if (value == null) {
                        throw new IllegalArgumentException(
                                LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                        );
                    }

                    try {
                        return rawType.getEnumConstants()[Integer.parseInt(value)];
                    } catch (NumberFormatException e) {
                        Enum[] result = (Enum[]) rawType.getEnumConstants();
                        for (Enum r : result) {
                            if (r.name().equalsIgnoreCase(value)) {
                                return rawType.cast(r);
                            }
                        }
                        return null;
                    }
                }

                @Override
                public String toString(T value) {
                    if (value == null) {
                        throw new IllegalArgumentException(
                                LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                        );
                    }
                    return value.toString();
                }
            };
        }
    }

    @Singleton
    @SuppressWarnings("unchecked")
    public static class BooleanProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType, Type genericType, Annotation[] annotations) {
            if (boolean.class.isAssignableFrom(rawType) || Boolean.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new ParamConverter<Boolean>() {
                    @Override
                    public Boolean fromString(String value) {
                        return "".equals(value)
                                || "true".equalsIgnoreCase(value)
                                || "1".equals(value);
                    }

                    @Override
                    public String toString(Boolean value) {
                        if (value == null) {
                            throw new IllegalArgumentException(
                                    LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                            );
                        }
                        return value.toString();
                    }
                };
            }
            return null;
        }
    }

    /**
     * Provider of {@link ParamConverter param converter} that convert the supplied string into a Java
     * {@link Date} instance using conversion method from the
     * {@link HttpDateFormat http date formatter} utility class.
     */
    @Singleton
    @SuppressWarnings("unchecked")
    public static class DateProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (LocalDateTime.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new LocalDateTimeParamConverter();
            } else if (Duration.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new DurationParamConverter();
            } else if (Instant.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new InstantParamConverter();
            } else if (LocalDate.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new LocalDateParamConverter();
            } else if (LocalTime.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new LocalTimeParamConverter();
            } else if (Period.class.isAssignableFrom(rawType)) {
                return (ParamConverter<T>) new PeriodParamConverter();
            } else if (Date.class.isAssignableFrom(rawType)) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(String value) {
                        if (value == null) {
                            throw new IllegalArgumentException(
                                    LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                            );
                        }
                        return rawType.cast(parseDate(value));
                    }

                    @Override
                    public String toString(final T value) throws IllegalArgumentException {
                        if (value == null) {
                            throw new IllegalArgumentException(
                                    LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                            );
                        }
                        return value.toString();
                    }
                };
            }
            return null;
        }
    }

    public static class DurationParamConverter implements ParamConverter<Duration> {
        @Override
        public Duration fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return Duration.parse(value);
        }

        @Override
        public String toString(Duration value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return value.toString();
        }
    }

    public static class PeriodParamConverter implements ParamConverter<Period> {
        @Override
        public Period fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return Period.parse(value);
        }

        @Override
        public String toString(Period value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return value.toString();
        }
    }

    public static class InstantParamConverter implements ParamConverter<Instant> {
        @Override
        public Instant fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return parseInstant(value);
        }

        @Override
        public String toString(Instant value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return value.toString();
        }
    }

    public static class LocalDateParamConverter implements ParamConverter<LocalDate> {

        @Override
        public LocalDate fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }

            return LocalDate.from(parseInstant(value));
        }

        @Override
        public String toString(LocalDate value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return value.toString();
        }
    }

    public static class LocalDateTimeParamConverter implements ParamConverter<LocalDateTime> {

        @Override
        public LocalDateTime fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return LocalDateTime.from(parseInstant(value));
        }

        @Override
        public String toString(LocalDateTime value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return value.toString();
        }
    }

    public static class LocalTimeParamConverter implements ParamConverter<LocalTime> {

        @Override
        public LocalTime fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return LocalTime.from(parseInstant(value));
        }

        @Override
        public String toString(LocalTime value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value")
                );
            }
            return value.toString();
        }
    }
}
