package ameba.exception;

import ameba.Ameba;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>ConfigErrorException class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class ConfigErrorException extends AmebaExceptionWithJavaSource {
    private String config;
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
     * {@inheritDoc}
     */
    @Override
    public File getSourceFile() {
        return new File(Ameba.getApp().getConfigFiles()[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File[] getSourceFiles() {

        List<File> files = Lists.newArrayListWithExpectedSize(Ameba.getApp().getConfigFiles().length);

        for (String conf : Ameba.getApp().getConfigFiles()) {
            files.add(new File(conf));
        }

        return files.toArray(new File[files.size()]);
    }

    String getConfig() {
        if (config == null) {
            try {
                config = IOUtils.readFromResource(getSourceFile().getPath());
            } catch (IOException e) {
                config = null;
            }
        }
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSource() {
        return getConfig() == null ? null : Lists.newArrayList(config.split("\\s"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getLineNumber() {
        if (line == null || line == -1) {
            int i = 0;
            List<String> lines = getSource();
            if (lines == null) return null;
            for (String line : lines) {
                i++;
                if (key.equals(line.split("=")[0])) {
                    return i;
                }
            }
        }
        return super.getLineNumber();
    }
}
