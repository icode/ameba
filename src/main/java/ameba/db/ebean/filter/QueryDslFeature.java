package ameba.db.ebean.filter;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class QueryDslFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        Configuration cfg = context.getConfiguration();
        if (!cfg.isRegistered(CommonExprTransformer.class))
            context.register(CommonExprTransformer.class);
        if (!cfg.isRegistered(CommonExprArgTransformer.class))
            context.register(CommonExprArgTransformer.class);
        if (!cfg.isRegistered(QuerySyntaxExceptionMapper.class))
            context.register(QuerySyntaxExceptionMapper.class);
        return true;
    }
}
