package ameba.db.ebean.filter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class QueryDslFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(CommonExprTransformer.class);
        context.register(CommonExprArgTransformer.class);
        return true;
    }
}
