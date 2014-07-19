package ameba.mvc.template;

import ameba.exceptions.AmebaExceptionWithJavaSource;

/**
 * @author icode
 */
public class TemplateException extends AmebaExceptionWithJavaSource {
    public TemplateException() {
    }

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }
}
