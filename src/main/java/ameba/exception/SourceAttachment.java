package ameba.exception;

import java.io.File;
import java.util.List;

/**
 * <p>SourceAttachment interface.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public interface SourceAttachment {

    /**
     * <p>getSourceFile.</p>
     *
     * @return a {@link java.io.File} object.
     */
    File getSourceFile();

    /**
     * <p>getSourceFiles.</p>
     *
     * @return an array of {@link java.io.File} objects.
     */
    File[] getSourceFiles();

    /**
     * <p>getSource.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getSource();

    /**
     * <p>getLineNumber.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getLineNumber();

    /**
     * <p>getLineIndex.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getLineIndex();
}
