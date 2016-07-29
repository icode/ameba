package ameba.db.ebean;

import ameba.message.internal.PathProperties;
import com.avaje.ebean.FetchPath;
import com.avaje.ebean.Query;

import java.util.Set;

/**
 * @author icode
 */
public class EbeanPathProps implements FetchPath {
    private PathProperties pathProperties;

    public EbeanPathProps(PathProperties pathProperties) {
        this.pathProperties = pathProperties;
    }

    public static EbeanPathProps of(PathProperties pathProperties) {
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

    @Override
    public <T> void apply(final Query<T> query) {
        pathProperties.each(new PathProperties.Each<PathProperties.Props>() {
            @Override
            public void execute(PathProperties.Props props) {
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