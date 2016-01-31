package ameba.db.ebean.filter.ast;

import ameba.ast.spi.Expression;
import org.apache.commons.lang3.StringUtils;

/**
 * @author icode
 */
public class ArgumentsExpression extends Expression {

    public ArgumentsExpression(String fragment, int offset) {
        super(offset);
    }

    @Override
    public String toString() {
        return getOffset() + ": (" + StringUtils.join(getChildren(), ",") + ")";
    }
}
