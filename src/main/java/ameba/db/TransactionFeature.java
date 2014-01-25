package ameba.db;

import ameba.feature.exception.ThrowableExceptionMapper;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author: ICode
 * @since: 13-8-17 下午6:17
 */
public class TransactionFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        ThrowableExceptionMapper.init(context);
        return true;
    }
}
