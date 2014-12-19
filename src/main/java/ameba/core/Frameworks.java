package ameba.core;

import ameba.mvc.ErrorPageGenerator;
import com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author icode
 */
public class Frameworks {
    private MessageBodyWriter<Viewable> viewableMessageBodyWriter;
    private ErrorPageGenerator errorPageGenerator;

    private Frameworks() {
    }

    public static ErrorPageGenerator getErrorPageGenerator(ServiceLocator serviceLocator) {
        final Set<ExceptionMapper> exceptionMappers = Sets.newLinkedHashSet();
        exceptionMappers.addAll(Providers.getCustomProviders(serviceLocator, ExceptionMapper.class));
        exceptionMappers.addAll(Providers.getProviders(serviceLocator, ExceptionMapper.class));
        for (ExceptionMapper t : exceptionMappers) {
            if (t instanceof ErrorPageGenerator) {
                return (ErrorPageGenerator) t;
            }
        }
        return null;
    }

    public static MessageBodyWriter<Viewable> getViewableMessageBodyWriter(MessageBodyWorkers workers) {
        return workers.getMessageBodyWriter(Viewable.class, Viewable.class,
                new Annotation[]{}, null);
    }
}
