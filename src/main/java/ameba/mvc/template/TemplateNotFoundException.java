package ameba.mvc.template;

import java.net.URL;
import java.util.List;

/**
 * <p>TemplateNotFoundException class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class TemplateNotFoundException extends TemplateException {
    /**
     * <p>Constructor for TemplateNotFoundException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public TemplateNotFoundException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for TemplateNotFoundException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     * @param line    a {@link java.lang.Integer} object.
     */
    public TemplateNotFoundException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    /**
     * <p>Constructor for TemplateNotFoundException.</p>
     *
     * @param message   a {@link java.lang.String} object.
     * @param cause     a {@link java.lang.Throwable} object.
     * @param line      a {@link java.lang.Integer} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     * @param sourceUrl a {@link java.net.URL} object.
     * @param sourceUrl a {@link java.net.URL} object.
     * @param sourceUrl a {@link java.net.URL} object.
     * @param sourceUrl a {@link java.net.URL} object.
     * @param sourceUrl a {@link java.net.URL} object.
     * @param sourceUrl a {@link java.net.URL} object.
     * @param source    a {@link java.util.List} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     */
    public TemplateNotFoundException(String message, Throwable cause, Integer line, Integer lineIndex, URL sourceUrl, List<String> source) {
        super(message, cause, line, lineIndex, sourceUrl, source);
    }
}
