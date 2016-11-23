package ameba.mvc.template.internal;

import org.glassfish.jersey.server.mvc.Viewable;

import java.util.List;

/**
 * {@link org.glassfish.jersey.server.mvc.Viewable} implementation representing return value of enhancing methods added to
 * {@link org.glassfish.jersey.server.model.Resource resources} annotated with {@link org.glassfish.jersey.server.mvc.Template}.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 * @see org.glassfish.jersey.server.mvc.Template
 * @version $Id: $Id
 */
public class ImplicitViewable extends Viewable {

    private final List<String> templateNames;

    private final Class<?> resolvingClass;

    /**
     * Create a {@code ImplicitViewable}.
     *
     * @param templateNames  allowed template names for which a {@link Viewable viewable} can be resolved.
     * @param model          the model, may be {@code null}.
     * @param resolvingClass the class to use to resolve the template name if the template is not absolute,
     *                       if {@code null} then the resolving class will be obtained from the last matching resource.
     * @throws IllegalArgumentException if the template name is {@code null}.
     */
    ImplicitViewable(final List<String> templateNames, final Object model, final Class<?> resolvingClass)
            throws IllegalArgumentException {
        super("", model);

        this.templateNames = templateNames;
        this.resolvingClass = resolvingClass;
    }

    /**
     * Get allowed template names for which a {@link Viewable viewable} can be resolved.
     *
     * @return allowed template names.
     */
    public List<String> getTemplateNames() {
        return templateNames;
    }

    /**
     * Get the resolving class.
     *
     * @return Resolving class.
     */
    public Class<?> getResolvingClass() {
        return resolvingClass;
    }
}
