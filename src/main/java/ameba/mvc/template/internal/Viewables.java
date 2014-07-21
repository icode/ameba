package ameba.mvc.template.internal;

import ameba.mvc.route.RouteHelper;
import org.glassfish.jersey.server.mvc.Viewable;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-20
 */
public class Viewables {

    public static final String PROTECTED_DIR = "_protected";
    public static final String PROTECTED_DIR_PATH = "/" + PROTECTED_DIR + "/";

    private Viewables() {
    }

    public static Viewable newViewable() {
        return new Viewable("/" + RouteHelper.getCurrentRequestContext().getUriInfo().getPath());
    }

    public static Viewable newViewable(Object model) {
        return new Viewable("/" + RouteHelper.getCurrentRequestContext().getUriInfo().getPath(), model);
    }

    public static Viewable newProtected(Object model) {
        return new Viewable(PROTECTED_DIR_PATH + RouteHelper.getCurrentRequestContext().getUriInfo().getPath(), model);
    }

    public static Viewable newProtected() {
        return new Viewable(PROTECTED_DIR_PATH + RouteHelper.getCurrentRequestContext().getUriInfo().getPath());
    }

    public static Viewable newProtected(String name) {
        return new Viewable(PROTECTED_DIR_PATH + name);
    }

    public static Viewable newProtected(String name, Object model) {
        return new Viewable(PROTECTED_DIR_PATH + name, model);
    }

    public static Viewable newDefaultViewable() {
        return new Viewable("");
    }

    public static Viewable newDefaultViewable(String name) {
        return new Viewable(name);
    }

    public static Viewable newDefaultViewable(Object model) {
        return new Viewable("", model);
    }

    public static Viewable newDefaultViewable(String templateName, Object model) {
        return new Viewable(templateName, model);
    }

}
