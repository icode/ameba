package ameba.db.ebean.support;

import io.ebeaninternal.api.SpiEbeanServer;

/**
 * <p>Abstract AbstractModelResource class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public abstract class AbstractModelResource<URI_ID, MODEL>
        extends ModelResource<URI_ID, URI_ID, MODEL> {
    /**
     * <p>Constructor for AbstractModelResource.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     */
    public AbstractModelResource(Class<MODEL> modelType) {
        super(modelType);
    }

    /**
     * <p>Constructor for AbstractModelResource.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     * @param server    a {@link io.ebeaninternal.api.SpiEbeanServer} object.
     */
    public AbstractModelResource(Class<MODEL> modelType, SpiEbeanServer server) {
        super(modelType, server);
    }
}
