package ameba.exception;

import java.io.File;
import java.util.List;

/**
 * @author icode
 */
public interface SourceAttachment {

    File getSourceFile();

    File[] getSourceFiles();

    List<String> getSource();

    Integer getLineNumber();

    Integer getLineIndex();
}
