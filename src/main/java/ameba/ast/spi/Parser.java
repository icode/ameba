package ameba.ast.spi;

import java.text.ParseException;

/**
 * @author icode
 */
public interface Parser {
    /**
     * Parse the expression.
     *
     * @param source - expression source
     * @param offset - expression offset
     * @return expression root node
     * @throws ParseException - If the source cannot be parsed
     */
    Node parse(String source, int offset) throws ParseException;
}
