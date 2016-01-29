package ameba.ast.spi;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author icode
 */
public interface Visitor {
    /**
     * Visit a node.
     *
     * @param node - visited node
     * @return true - Need visit the children nodes.
     * @throws IOException    - If an I/O error occurs
     * @throws ParseException - If the expression cannot be parsed on runtime
     */
    boolean visit(Node node) throws IOException, ParseException;
}
