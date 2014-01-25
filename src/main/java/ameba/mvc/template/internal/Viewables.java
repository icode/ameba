package ameba.mvc.template.internal;

import org.glassfish.jersey.server.mvc.Viewable;

import java.util.UUID;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-20
 */
public class Viewables {

    static final String USE_REQUEST_PATH_VIEWABLE_KEY_WORD = "/::USE::REQUEST::PATH::" + UUID.randomUUID().toString().toUpperCase() + "::/";

    private Viewables() {
    }

    public static Viewable newViewable() {
        return new Viewable(USE_REQUEST_PATH_VIEWABLE_KEY_WORD);
    }

    public static Viewable newViewable(Object model) {
        return new Viewable(USE_REQUEST_PATH_VIEWABLE_KEY_WORD, model);
    }

    public static Viewable newViewable(Object model, Class<?> resolvingClass) {
        return new Viewable(USE_REQUEST_PATH_VIEWABLE_KEY_WORD, model, resolvingClass);
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
