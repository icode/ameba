package ameba.db.ebean.filter;

import ameba.core.ServiceLocators;
import ameba.db.dsl.*;
import ameba.db.dsl.QueryExprMeta.Val;
import ameba.i18n.Messages;
import com.avaje.ebean.Expression;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;

import java.util.Arrays;

/**
 * @author icode
 */
public class EbeanExprInvoker extends QueryExprInvoker<Expression> {

    private SpiEbeanServer server;
    private ServiceLocator locator;
    private SpiQuery<?> query;

    public EbeanExprInvoker(SpiQuery<?> query, ServiceLocator locator) {
        this.locator = locator;
        this.query = query;
        this.server = query.getBeanDescriptor().getEbeanServer();
    }

    public SpiEbeanServer getServer() {
        return server;
    }

    public SpiQuery<?> getQuery() {
        return query;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Val<Expression> arg(String field, String op, Val<Expression> arg,
                                  int index, int count, QueryExprMeta parent) {
        Iterable<ExprArgTransformer> transformers = getTransformer(ExprArgTransformer.class);
        for (ExprArgTransformer transformer : transformers) {
            Transformed<Val<Expression>> transformed = transformer.transform(
                    field, op, arg, index, count, this, parent
            );
            if (transformed.success()) {
                return transformed.result();
            }
        }
        throw new QuerySyntaxException(Messages.get("dsl.transform.err", field, op, String.valueOf(arg)));
    }

    protected <R, T extends Transformer<Transformed<R>>> Iterable<T> getTransformer(Class<T> transformerClass) {
        Iterable<RankedProvider<T>> rankedProviders =
                ServiceLocators.getRankedProviders(locator, transformerClass);
        return ServiceLocators
                .sortRankedProviders(new RankedComparator<T>(), rankedProviders);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Val<Expression> expr(String field, String op, Val<Expression>[] args, QueryExprMeta parent) {
        Iterable<ExprTransformer> transformers = getTransformer(ExprTransformer.class);
        for (ExprTransformer<Expression, EbeanExprInvoker> transformer : transformers) {
            Transformed<Val<Expression>> transformed = transformer.transform(field, op, args, this, parent);
            if (transformed.success()) {
                return transformed.result();
            }
        }
        throw new QuerySyntaxException(Messages.get("dsl.transform.err", field, op, Arrays.toString(args)));
    }
}
