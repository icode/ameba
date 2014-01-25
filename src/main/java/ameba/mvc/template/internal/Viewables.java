package ameba.mvc.template.internal;

import org.glassfish.jersey.server.mvc.Viewable;

import java.util.UUID;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-20
 */
public class Viewables {

    private Viewables() {
    }

    public static Viewable newViewable() {
        return new Viewable("/");
    }

    public static Viewable newViewable(Object model) {
        return new Viewable("/", model);
    }

    public static Viewable newViewable(Object model, Class<?> resolvingClass) {
        return new Viewable("/", model, resolvingClass);
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

    public static Viewable newDefaultViewable(String templateName, Object model, Class<?> resolvingClass) {
        return new Viewable(templateName, model, resolvingClass);
    }

}
