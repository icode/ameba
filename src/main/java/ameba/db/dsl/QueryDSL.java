package ameba.db.dsl;

import ameba.i18n.Messages;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.List;

/**
 * <p>QueryDSL class.</p>
 *
 * @author icode
 */
public class QueryDSL {

    private static final DiagnosticErrorListener ERROR_LISTENER = new DiagnosticErrorListener();

    private QueryDSL() {
    }

    /**
     * <p>invoke.</p>
     *
     * @param expression  a {@link java.lang.String} object.
     * @param invoker     a {@link ameba.db.dsl.QueryExprInvoker} object.
     * @param exprApplier a {@link ameba.db.dsl.ExprApplier} object.
     * @param <T>         a T object.
     */
    public static <T> void invoke(String expression,
                                  QueryExprInvoker<T> invoker,
                                  ExprApplier<T> exprApplier) {
        QueryExprInvoker.invoke(parse(expression), invoker, exprApplier);
    }

    /**
     * <p>parse.</p>
     *
     * @param expression a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<QueryExprMeta> parse(String expression) {
        QueryParser parser = parser(tokens(expression));
        try {
            return parse(parser);
        } catch (ParseCancellationException | RecognitionException e) {
            RecognitionException err;
            if (e instanceof ParseCancellationException) {
                err = (RecognitionException) e.getCause();
            } else {
                err = (RecognitionException) e;
            }
            throw new QuerySyntaxException(
                    Messages.get("dsl.parse.err", err.getOffendingToken().getCharPositionInLine()),
                    e
            );
        }
    }

    /**
     * <p>parse.</p>
     *
     * @param parser a {@link ameba.db.dsl.QueryParser} object.
     * @return a {@link java.util.List} object.
     */
    public static List<QueryExprMeta> parse(QueryParser parser) {
        QueryExprListener listener = listener(parser);
        parser.query();
        return listener.getQueryExprMetaList();
    }

    /**
     * <p>tokens.</p>
     *
     * @param expression a {@link java.lang.String} object.
     * @return a {@link org.antlr.v4.runtime.CommonTokenStream} object.
     */
    public static CommonTokenStream tokens(String expression) {
        return tokens(lexer(expression));
    }

    /**
     * <p>tokens.</p>
     *
     * @param lexer a {@link ameba.db.dsl.QueryLexer} object.
     * @return a {@link org.antlr.v4.runtime.CommonTokenStream} object.
     */
    public static CommonTokenStream tokens(QueryLexer lexer) {
        return new CommonTokenStream(lexer);
    }

    /**
     * <p>lexer.</p>
     *
     * @param expression a {@link java.lang.String} object.
     * @return a {@link ameba.db.dsl.QueryLexer} object.
     */
    public static QueryLexer lexer(String expression) {
        QueryLexer lexer = new QueryLexer(input(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERROR_LISTENER);
        return lexer;
    }

    /**
     * <p>input.</p>
     *
     * @param expression a {@link java.lang.String} object.
     * @return a {@link org.antlr.v4.runtime.CharStream} object.
     */
    public static CharStream input(String expression) {
        return CharStreams.fromString(expression == null ? "" : expression, "db_query");
    }

    /**
     * <p>parser.</p>
     *
     * @param tokens a {@link org.antlr.v4.runtime.CommonTokenStream} object.
     * @return a {@link ameba.db.dsl.QueryParser} object.
     */
    public static QueryParser parser(CommonTokenStream tokens) {
        QueryParser parser = new QueryParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    /**
     * <p>listener.</p>
     *
     * @return a {@link ameba.db.dsl.QueryExprListener} object.
     */
    public static QueryExprListener listener() {
        return new QueryExprListener();
    }

    /**
     * <p>listener.</p>
     *
     * @param parser a {@link ameba.db.dsl.QueryParser} object.
     * @return a {@link ameba.db.dsl.QueryExprListener} object.
     */
    public static QueryExprListener listener(QueryParser parser) {
        QueryExprListener listener = listener();
        parser.addParseListener(listener);
        return listener;
    }
}
