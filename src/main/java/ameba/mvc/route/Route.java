package ameba.mvc.route;

import javax.persistence.MappedSuperclass;

/**
 * 路由器实体
 *
 * @author ICode
 * @since 13-8-9 下午7:45
 */
@MappedSuperclass
public class Route {
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
