package ameba.db.ebean.filter.ast;

import ameba.ast.spi.Expression;

/**
 * @author icode
 */
public class ValueExpression extends Expression {
    private String value;

    public ValueExpression(String fragment, int offset) {
        super(offset);
        value = fragment;
    }

    @Override
    public String toString() {
        return getOffset() + ": [" + value + "]";
    }
}
