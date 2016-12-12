package ameba.db.ebean.filter;

import ameba.core.ServiceLocators;
import ameba.db.dsl.*;
import ameba.db.dsl.QueryExprMeta.Val;
import ameba.i18n.Messages;
import io.ebean.Expression;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;

import java.util.Arrays;

/**
 * <p>EbeanExprInvoker class.</p>
 *
 * @author icode
 *
 */
public class EbeanExprInvoker extends QueryExprInvoker<Expression> {

    private SpiEbeanServer server;
    private ServiceLocator locator;
    private SpiQuery<?> query;

    /**
     * <p>Constructor for EbeanExprInvoker.</p>
     *
     * @param query   a {@link io.ebeaninternal.api.SpiQuery} object.
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    public EbeanExprInvoker(SpiQuery<?> query, ServiceLocator locator) {
        this.locator = locator;
        this.query = query;
        this.server = query.getBeanDescriptor().getEbeanServer();
    }

    /**
     * <p>Getter for the field <code>server</code>.</p>
     *
     * @return a {@link io.ebeaninternal.api.SpiEbeanServer} object.
     */
    public SpiEbeanServer getServer() {
        return server;
    }

    /**
     * <p>Getter for the field <code>query</code>.</p>
     *
     * @return a {@link io.ebeaninternal.api.SpiQuery} object.
     */
    public SpiQuery<?> getQuery() {
        return query;
    }

    /**
     * <p>Getter for the field <code>locator</code>.</p>
     *
     * @return a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    public ServiceLocator getLocator() {
        return locator;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * <p>getTransformer.</p>
     *
     * @param transformerClass a {@link java.lang.Class} object.
     * @param <R>              Result.
     * @param <T>              Transformer.
     * @return a {@link java.lang.Iterable} object.
     */
    protected <R, T extends Transformer<Transformed<R>>> Iterable<T> getTransformer(Class<T> transformerClass) {
        Iterable<RankedProvider<T>> rankedProviders =
                ServiceLocators.getRankedProviders(locator, transformerClass);
        return ServiceLocators
                .sortRankedProviders(new RankedComparator<>(), rankedProviders);
    }

    /**
     * {@inheritDoc}
     */
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
