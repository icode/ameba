package ameba.db.ebean.support;

import com.avaje.ebeaninternal.api.SpiEbeanServer;

/**
 * <p>Abstract AbstractModelResource class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class AbstractModelResource<URI_ID, MODEL>
        extends ModelResource<URI_ID, URI_ID, MODEL> {
    public AbstractModelResource(Class<MODEL> modelType) {
        super(modelType);
    }

    public AbstractModelResource(Class<MODEL> modelType, SpiEbeanServer server) {
        super(modelType, server);
    }
}
