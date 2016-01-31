package ameba.ast;

import ameba.ast.spi.*;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @author icode
 */
public class SyntaxParser implements Parser {

    private Lexer lexer;

    public SyntaxParser(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public Node parse(String source) throws ParseException {
        List<Node> children = Lists.newArrayList();
        int sourceLength = source.length();
        int i = 0;
        State state = State.CONTINUE;
        char c;
        final StringBuilder buffer = new StringBuilder();
        while (sourceLength > i) {
            c = source.charAt(i);
            buffer.append(c);

            state = lexer.nextState(state, c);

            i++;

            if (state.equals(State.CONTINUE)) {
                // CONTINUE
            } else if (state.equals(State.BREAK)) {
                break;
            } else if (state.equals(State.PROCESS) && (state.getAction() == 0 || state.getAction() == 3)) {
                int length = buffer.length();
                int offset = i - length - state.getBackspace() + 1;
                Node node = lexer.parse(buffer.substring(0, length - state.getBackspace()), offset, state);
                if (node != null)
                    children.add(node);
                buffer.setLength(0);
            }
        }

        return new RootNode(children);
    }

    private static class RootNode extends AbstractNode {

        public RootNode(List<Node> children) {
            super(0);
            this.children = children;
        }

        @Override
        public void accept(Visitor visitor) throws IOException, ParseException {
            visit(this, visitor);
        }

        public void visit(Node node, Visitor visitor) throws IOException, ParseException {
            for (Node n : node.getChildren()) {
                if (visitor.visit(n)) {
                    visit(n, visitor);
                }
            }
        }
    }
}
