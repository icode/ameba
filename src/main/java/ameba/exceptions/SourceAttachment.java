package ameba.exceptions;

import java.util.List;

/**
 * @author icode
 */
public interface SourceAttachment {

    String getSourceFile();

    List<String> getSource();

    Integer getLineNumber();
}
