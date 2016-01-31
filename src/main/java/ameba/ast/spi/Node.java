package ameba.ast.spi;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author icode
 */
public interface Node {
    /**
     * Accept a visitor.
     *
     * @param visitor Visitor
     * @throws IOException    - If an I/O error occurs
     * @throws ParseException - If the expression cannot be parsed on runtime
     */
    void accept(Visitor visitor) throws IOException, ParseException;

    /**
     * Get the node offset.
     *
     * @return offset
     */
    int getOffset();

    /**
     * Get the parent node.
     *
     * @return parent
     */
    Node getParent();

    /**
     * Get the template children nodes.
     *
     * @return children nodes
     */
    List<Node> getChildren();

    /**
     * add the template children node.
     */
    void addChild(Node node);
}
