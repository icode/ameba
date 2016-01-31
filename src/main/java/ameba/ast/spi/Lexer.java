package ameba.ast.spi;

/**
 * @author icode
 */
public interface Lexer {
    Node parse(String fragment, int offset, Parser.State state);

    Parser.State nextState(Parser.State state, char ch);
}
