package ameba.mvc.template;

import java.io.File;
import java.util.List;

/**
 * @author icode
 */
public class TemplateNotFoundException extends TemplateException {
    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    public TemplateNotFoundException(String message, Throwable cause, Integer line, File sourceFile, List<String> source, Integer lineIndex) {
        super(message, cause, line, sourceFile, source, lineIndex);
    }
}
