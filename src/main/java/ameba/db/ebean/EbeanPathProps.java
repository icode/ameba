package ameba.db.ebean;

import ameba.message.internal.BeanPathProperties;
import io.ebean.FetchPath;
import io.ebean.Query;

import java.util.Collection;
import java.util.Set;

/**
 * <p>EbeanPathProps class.</p>
 *
 * @author icode
 *
 */
public class EbeanPathProps implements FetchPath {
    private BeanPathProperties pathProperties;

    /**
     * <p>Constructor for EbeanPathProps.</p>
     *
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     */
    public EbeanPathProps(BeanPathProperties pathProperties) {
        this.pathProperties = pathProperties;
    }

    /**
     * <p>of.</p>
     *
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     * @return a {@link ameba.db.ebean.EbeanPathProps} object.
     */
    public static EbeanPathProps of(BeanPathProperties pathProperties) {
        return new EbeanPathProps(pathProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPath(String path) {
        return pathProperties.hasPath(path);
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getProperties(String path) {
        return pathProperties.getProperties(path);
    }

    /**
     * <p>getPathProps.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<BeanPathProperties.Props> getPathProps() {
        return pathProperties.getPathProps();
    }

    /** {@inheritDoc} */
    @Override
    public <T> void apply(final Query<T> query) {
        pathProperties.each(props -> {
            String path = props.getPath();
            String propsStr = props.getPropertiesAsString();

            if (path == null || path.isEmpty()) {
                query.select(propsStr);
            } else {
                query.fetch(path, propsStr);
            }
        });
    }
}
