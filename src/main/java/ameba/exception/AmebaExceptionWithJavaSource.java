package ameba.exception;

import java.io.File;
import java.util.List;

/**
 * @author icode
 */
public abstract class AmebaExceptionWithJavaSource extends AmebaException implements SourceAttachment {

    protected Integer line;
    protected File[] sourceFile;
    protected List<String> source;
    protected Integer lineIndex;

    protected AmebaExceptionWithJavaSource(String message) {
        super(message);
    }

    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line) {
        super(message, cause);
        this.line = line;
    }

    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line, File[] sourceFile, List<String> source, Integer lineIndex) {
        super(message, cause);
        this.line = line;
        this.sourceFile = sourceFile;
        this.source = source;
        this.lineIndex = lineIndex;
    }

    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line, File sourceFile, List<String> source, Integer lineIndex) {
        this(message, cause, line, new File[]{sourceFile}, source, lineIndex);
    }

    @Override
    public File getSourceFile() {
        return sourceFile[0];
    }

    @Override
    public File[] getSourceFiles() {
        return sourceFile;
    }

    @Override
    public List<String> getSource() {
        return source;
    }

    @Override
    public Integer getLineNumber() {
        return line;
    }

    @Override
    public Integer getLineIndex() {
        return lineIndex;
    }
}
