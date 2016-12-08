package ameba.exception;

import java.net.URL;
import java.util.List;

/**
 * <p>Abstract AmebaExceptionWithJavaSource class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public abstract class AmebaExceptionWithJavaSource extends AmebaException implements SourceAttachment {

    protected Integer line;
    protected URL sourceUrl;
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
     * @param message   a {@link java.lang.String} object.
     * @param cause     a {@link java.lang.Throwable} object.
     * @param line      a {@link java.lang.Integer} object.
     * @param sourceUrl url
     * @param source    a {@link java.util.List} object.
     * @param lineIndex a {@link java.lang.Integer} object.
     */
    @SuppressWarnings("JavaDoc")
    public AmebaExceptionWithJavaSource(String message, Throwable cause, Integer line, Integer lineIndex,
                                        URL sourceUrl, List<String> source) {
        super(message, cause);
        this.line = line;
        this.sourceUrl = sourceUrl;
        this.source = source;
        this.lineIndex = lineIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getSourceUrl() {
        return sourceUrl;
    }

    /** {@inheritDoc} */
    @Override
    public Integer getLineNumber() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public Integer getLineIndex() {
        return lineIndex;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSource() {
        return source;
    }
}
