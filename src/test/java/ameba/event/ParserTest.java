package ameba.event;

import ameba.ast.SyntaxParser;
import ameba.ast.spi.Lexer;
import ameba.ast.spi.Node;
import ameba.ast.spi.Parser;
import ameba.db.ebean.filter.ast.ArgumentsExpression;
import ameba.db.ebean.filter.ast.QueryExpression;
import org.junit.Test;

import java.text.ParseException;

/**
 * @author icode
 */
public class ParserTest {
    @Test
    public void testParser() throws ParseException {
        SyntaxParser parser = new SyntaxParser(new QueryLexer());
        Node node = parser.parse("a.b.eq(abc)or(a.eq(1)b.or(b.eq(0)b.eq(1)))");
    }

    private class QueryLexer implements Lexer {

        private Node currentNode;

        @Override
        public Node parse(String fragment, int offset, Parser.State state) {
            if (state.equals(Parser.State.PROCESS)) {
                switch (state.getAction()) {
                    case 0:
                    case 1:
                        return (currentNode = new QueryExpression(fragment, offset));
                    case 3:
                        currentNode.addChild(new ArgumentsExpression(fragment, offset));
                        return null;
                }
            }
            return new QueryExpression(fragment, offset);
        }

        // 0 处理普通代码
        // 1 开始处理参数
        // 2 处理参数中
        // 3 结束处理参数
        @Override
        public Parser.State nextState(Parser.State state, char ch) {
            if (state.equals(Parser.State.PROCESS)) {
                switch (state.getAction()) {
                    case 0:
                        return Parser.State._process(1, 1);
                    case 1:
                        return Parser.State._process(2, 1);
                }
            }
            switch (ch) {
                case '(':
                    if (state.equals(Parser.State.PROCESS)) {
                        state.setProgress(state.getProgress() + 1);
                        return state;
                    } else {
                        return Parser.State._process(0, 1);
                    }
                case ')':
                    if (state.equals(Parser.State.PROCESS)) {
                        state.setProgress(state.getProgress() - 1);
                        return state;
                    } else {
                        return Parser.State._process(3, 1);
                    }
            }
            return Parser.State._continue();
        }
    }
}
