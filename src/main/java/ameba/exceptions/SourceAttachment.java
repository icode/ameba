package ameba.exceptions;

import java.io.File;
import java.util.List;

/**
 * @author icode
 */
public interface SourceAttachment {

    File getSourceFile();
    List<String> getSource();
    Integer getLineNumber();
    Integer getLineIndex();
}
