package ameba.mvc.template;

import ameba.exception.AmebaExceptionWithJavaSource;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * <p>TemplateException class.</p>
 *
 * @author icode
 */
public class TemplateException extends AmebaExceptionWithJavaSource {

    /**
     * <p>Constructor for TemplateException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public TemplateException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for TemplateException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     * @param line    a {@link java.lang.Integer} object.
     */
    public TemplateException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    /**
     * <p>Constructor for TemplateException.</p>
     *
     * @param message   a {@link java.lang.String} object.
     * @param cause     a {@link java.lang.Throwable} object.
     * @param line      a {@link java.lang.Integer} object.
     * @param sourceUrl a {@link URL} object.
     * @param source    a {@link java.util.List} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     */
    public TemplateException(String message, Throwable cause, Integer line, Integer lineIndex,
                             URL sourceUrl, List<String> source) {
        super(message, cause, line, lineIndex, sourceUrl, source);
    }

    @Override
    public List<String> getSource() {
        InputStream in = null;
        try {
            if (sourceUrl != null) {
                in = sourceUrl.openStream();
                return IOUtils.readLines(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
        return Lists.newArrayList();
    }
}
