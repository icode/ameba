package ameba.mvc.template;

import ameba.exception.AmebaExceptionWithJavaSource;

import java.io.File;
import java.util.List;

/**
 * @author icode
 */
public class TemplateException extends AmebaExceptionWithJavaSource {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause, Integer line) {
        super(message, cause, line);
    }

    public TemplateException(String message, Throwable cause, Integer line, File sourceFile, List<String> source, Integer lineIndex) {
        super(message, cause, line, sourceFile, source, lineIndex);
    }
}
