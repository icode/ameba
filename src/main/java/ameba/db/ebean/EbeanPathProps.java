package ameba.db.ebean;

import ameba.message.internal.BeanPathProperties;
import com.avaje.ebean.FetchPath;
import com.avaje.ebean.Query;

import java.util.Collection;
import java.util.Set;

/**
 * @author icode
 */
public class EbeanPathProps implements FetchPath {
    private BeanPathProperties pathProperties;

    public EbeanPathProps(BeanPathProperties pathProperties) {
        this.pathProperties = pathProperties;
    }

    public static EbeanPathProps of(BeanPathProperties pathProperties) {
        return new EbeanPathProps(pathProperties);
    }

    @Override
    public boolean hasPath(String path) {
        return pathProperties.hasPath(path);
    }

    @Override
    public Set<String> getProperties(String path) {
        return pathProperties.getProperties(path);
    }

    public Collection<BeanPathProperties.Props> getPathProps() {
        return pathProperties.getPathProps();
    }

    @Override
    public <T> void apply(final Query<T> query) {
        pathProperties.each(new BeanPathProperties.Each<BeanPathProperties.Props>() {
            @Override
            public void execute(BeanPathProperties.Props props) {
                String path = props.getPath();
                String propsStr = props.getPropertiesAsString();

                if (path == null || path.isEmpty()) {
                    query.select(propsStr);
                } else {
                    query.fetch(path, propsStr);
                }
            }
        });
    }
}