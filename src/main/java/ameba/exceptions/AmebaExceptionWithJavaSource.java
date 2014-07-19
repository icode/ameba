package ameba.exceptions;

import java.util.List;

/**
 * @author icode
 */
public abstract class AmebaExceptionWithJavaSource extends AmebaException implements SourceAttachment {

    Integer line;

    protected AmebaExceptionWithJavaSource() {
    }

    protected AmebaExceptionWithJavaSource(String message) {
        super(message);
    }

    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line) {
        super(message, cause);
        this.line = line;
    }

    @Override
    public String getSourceFile() {
        return null;
    }

    @Override
    public List<String> getSource() {
        return null;
    }

    @Override
    public Integer getLineNumber() {
        return null;
    }
}
