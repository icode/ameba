package ameba.db.ebean.filter.ast;

import ameba.ast.spi.Expression;

/**
 * @author icode
 */
public class QueryExpression extends Expression {
    private String column;
    private String op;

    public QueryExpression(String fragment, int offset) {
        super(offset);
        int opIndex = fragment.lastIndexOf(".");
        if (opIndex == -1) {
            this.op = fragment;
        } else {
            this.column = fragment.substring(0, opIndex);
            this.op = fragment.substring(opIndex + 1);
        }
    }

    @Override
    public String toString() {
        return getOffset() + ": [" + column + "][" + op + "]";
    }
}
