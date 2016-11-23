package ameba.mvc.template.internal;

import ameba.core.Requests;
import org.glassfish.jersey.server.mvc.Viewable;

/**
 * <p>Viewables class.</p>
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-20
 * @version $Id: $Id
 */
public class Viewables {

    /**
     * Constant <code>PROTECTED_DIR="_protected"</code>
     */
    public static final String PROTECTED_DIR = "_protected";
    /**
     * Constant <code>PROTECTED_DIR_PATH="/ + PROTECTED_DIR"</code>
     */
    public static final String PROTECTED_DIR_PATH = "/" + PROTECTED_DIR;

    private Viewables() {
    }

    /**
     * <p>newViewable.</p>
     *
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newViewable() {
        return new Viewable(getPath());
    }

    /**
     * <p>newViewable.</p>
     *
     * @param model a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newViewable(Object model) {
        return new Viewable(getPath(), model);
    }

    private static String getPath() {
        return "/" + Requests.getUriInfo().getPath();
    }

    private static String getPath(String name) {
        return name.startsWith("/") ? name : "/" + name;
    }

    /**
     * <p>newProtected.</p>
     *
     * @param model a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newProtected(Object model) {
        return new Viewable(PROTECTED_DIR_PATH + getPath(), model);
    }

    /**
     * <p>newProtected.</p>
     *
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newProtected() {
        return new Viewable(PROTECTED_DIR_PATH + getPath());
    }

    /**
     * <p>newProtected.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newProtected(String name) {
        return new Viewable(PROTECTED_DIR_PATH + getPath(name));
    }

    /**
     * <p>newProtected.</p>
     *
     * @param name  a {@link java.lang.String} object.
     * @param model a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newProtected(String name, Object model) {
        return new Viewable(PROTECTED_DIR_PATH + getPath(name), model);
    }

    /**
     * <p>newDefaultViewable.</p>
     *
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newDefaultViewable() {
        return new Viewable("");
    }

    /**
     * <p>newDefaultViewable.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newDefaultViewable(String name) {
        return new Viewable(name);
    }

    /**
     * <p>newDefaultViewable.</p>
     *
     * @param model a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newDefaultViewable(Object model) {
        return new Viewable("", model);
    }

    /**
     * <p>newDefaultViewable.</p>
     *
     * @param templateName a {@link java.lang.String} object.
     * @param model        a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     */
    public static Viewable newDefaultViewable(String templateName, Object model) {
        return new Viewable(templateName, model);
    }

}
