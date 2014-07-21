package ameba.exceptions;

import ameba.Ameba;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author icode
 */
public class ConfigErrorException extends AmebaExceptionWithJavaSource {
    private String config;
    private String key;

    public ConfigErrorException(String message, String key) {
        super(message);
        this.key = key;
    }

    public ConfigErrorException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    public ConfigErrorException(String message, String key, Throwable cause) {
        super(message, cause, -1);
        this.key = key;
    }

    @Override
    public File getSourceFile() {
        return new File(Ameba.getApp().getConfigFile());
    }

    String getConfig() {
        if (config == null) {
            try {
                config = IOUtils.readFromResource(getSourceFile().getPath());
            } catch (IOException e) {
                return "";
            }
        }
        return config;
    }

    @Override
    public List<String> getSource() {
        return Lists.newArrayList(config.split("\\s"));
    }

    @Override
    public Integer getLineNumber() {
        if (line == null || line == -1) {
            int i = 0;
            for (String line : getSource()) {
                i++;
                if (key.equals(line.split("=")[0])) {
                    return i;
                }
            }
        }
        return super.getLineNumber();
    }
}
