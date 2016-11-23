package ameba.mvc.template.internal;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.mvc.spi.ViewableContext;

import javax.inject.Singleton;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.WriterInterceptor;

/**
 * Provides MVC functionality.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 * @version $Id: $Id
 */
public class MvcBinder extends AbstractBinder {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(TemplateMethodInterceptor.class).to(WriterInterceptor.class).in(Singleton.class);
        //noinspection unchecked
        bind(ViewableMessageBodyWriter.class).to(MessageBodyWriter.class).in(Singleton.class);

        bind(TemplateModelProcessor.class).to(ModelProcessor.class).in(Singleton.class);
        bindAsContract(ResolvingViewableContext.class).in(Singleton.class);
        bind(ResolvingViewableContext.class).to(ViewableContext.class).in(Singleton.class).ranked(1);
    }
}
