package ameba.exception;

import java.io.File;
import java.util.List;

/**
 * <p>Abstract AmebaExceptionWithJavaSource class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class AmebaExceptionWithJavaSource extends AmebaException implements SourceAttachment {

    protected Integer line;
    protected File[] sourceFile;
    protected List<String> source;
    protected Integer lineIndex;

    /**
     * <p>Constructor for AmebaExceptionWithJavaSource.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    protected AmebaExceptionWithJavaSource(String message) {
        super(message);
    }

    /**
     * <p>Constructor for AmebaExceptionWithJavaSource.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     * @param line    a {@link java.lang.Integer} object.
     */
    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line) {
        super(message, cause);
        this.line = line;
    }

    /**
     * <p>Constructor for AmebaExceptionWithJavaSource.</p>
     *
     * @param message    a {@link java.lang.String} object.
     * @param cause      a {@link java.lang.Throwable} object.
     * @param line       a {@link java.lang.Integer} object.
     * @param sourceFile an array of {@link java.io.File} objects.
     * @param source     a {@link java.util.List} object.
     * @param lineIndex  a {@link java.lang.Integer} object.
     */
    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line, File[] sourceFile, List<String> source, Integer lineIndex) {
        super(message, cause);
        this.line = line;
        this.sourceFile = sourceFile;
        this.source = source;
        this.lineIndex = lineIndex;
    }

    /**
     * <p>Constructor for AmebaExceptionWithJavaSource.</p>
     *
     * @param message    a {@link java.lang.String} object.
     * @param cause      a {@link java.lang.Throwable} object.
     * @param line       a {@link java.lang.Integer} object.
     * @param sourceFile a {@link java.io.File} object.
     * @param source     a {@link java.util.List} object.
     * @param lineIndex  a {@link java.lang.Integer} object.
     */
    protected AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line, File sourceFile, List<String> source, Integer lineIndex) {
        this(message, cause, line, new File[]{sourceFile}, source, lineIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSourceFile() {
        return sourceFile[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File[] getSourceFiles() {
        return sourceFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getLineNumber() {
        return line;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getLineIndex() {
        return lineIndex;
    }
}
