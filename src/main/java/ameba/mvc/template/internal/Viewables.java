package ameba.mvc.template.internal;

import ameba.core.Requests;
import org.glassfish.jersey.server.mvc.Viewable;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-20
 */
public class Viewables {

    public static final String PROTECTED_DIR = "_protected";
    public static final String PROTECTED_DIR_PATH = "/" + PROTECTED_DIR;

    private Viewables() {
    }

    public static Viewable newViewable() {
        return new Viewable(getPath());
    }

    public static Viewable newViewable(Object model) {
        return new Viewable(getPath(), model);
    }

    private static String getPath() {
        return "/" + Requests.getUriInfo().getPath();
    }

    private static String getPath(String name) {
        return name.startsWith("/") ? name : "/" + name;
    }

    public static Viewable newProtected(Object model) {
        return new Viewable(PROTECTED_DIR_PATH + getPath(), model);
    }

    public static Viewable newProtected() {
        return new Viewable(PROTECTED_DIR_PATH + getPath());
    }

    public static Viewable newProtected(String name) {
        return new Viewable(PROTECTED_DIR_PATH + getPath(name));
    }

    public static Viewable newProtected(String name, Object model) {
        return new Viewable(PROTECTED_DIR_PATH + getPath(name), model);
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
