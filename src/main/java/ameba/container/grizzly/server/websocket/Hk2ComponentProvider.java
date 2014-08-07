package ameba.container.grizzly.server.websocket;

import ameba.Ameba;
import org.glassfish.tyrus.core.ComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

/**
 * @author icode
 */
public class Hk2ComponentProvider extends ComponentProvider {
    private static final Logger logger = LoggerFactory.getLogger(Hk2ComponentProvider.class);

    @Override
    public boolean isApplicable(Class<?> c) {
        Annotation[] annotations = c.getAnnotations();

        for (Annotation annotation : annotations) {
            String annotationClassName = annotation.annotationType().getCanonicalName();
            if (annotationClassName.equals("javax.ejb.Singleton") ||
                    annotationClassName.equals("javax.ejb.Stateful") ||
                    annotationClassName.equals("javax.ejb.Stateless")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <T> Object create(Class<T> c) {
        return Ameba.getServiceLocator().create(c);
    }

    @Override
    public boolean destroy(Object o) {
        try {
            Ameba.getServiceLocator().preDestroy(o);
            return true;
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            return false;
        }
    }
}
