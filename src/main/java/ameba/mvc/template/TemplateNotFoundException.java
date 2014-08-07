package ameba.mvc.template;

import ameba.exceptions.AmebaException;

/**
 * @author icode
 */
public class TemplateNotFoundException extends AmebaException {
    public TemplateNotFoundException() {
    }

    public TemplateNotFoundException(Throwable cause) {
        super(cause);
    }

    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
