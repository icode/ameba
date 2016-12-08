package ameba.exception;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * <p>ConfigErrorException class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public class ConfigErrorException extends AmebaExceptionWithJavaSource {
    private String key;

    /**
     * <p>Constructor for ConfigErrorException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ConfigErrorException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for ConfigErrorException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param key     a {@link java.lang.String} object.
     */
    public ConfigErrorException(String message, String key) {
        super(message);
        this.key = key;
    }

    /**
     * <p>Constructor for ConfigErrorException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     * @param line    a {@link java.lang.Integer} object.
     */
    public ConfigErrorException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    /**
     * <p>Constructor for ConfigErrorException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param key     a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public ConfigErrorException(String message, String key, Throwable cause) {
        super(message, cause, -1);
        this.key = key;
    }

    /**
     * <p>Getter for the field <code>key</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSource() {
        return Lists.newArrayList();
    }
}
