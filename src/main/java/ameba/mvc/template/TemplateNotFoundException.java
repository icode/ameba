package ameba.mvc.template;

import java.io.File;
import java.util.List;

/**
 * <p>TemplateNotFoundException class.</p>
 *
 * @author icode
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
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     * @param line a {@link java.lang.Integer} object.
     * @param sourceFile a {@link java.io.File} object.
     * @param source a {@link java.util.List} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     */
    public TemplateNotFoundException(String message, Throwable cause, Integer line, File sourceFile, List<String> source, Integer lineIndex) {
        super(message, cause, line, sourceFile, source, lineIndex);
    }
}
