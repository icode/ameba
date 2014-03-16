package ameba.mvc.template.internal;

import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

/**
 * 404 跳转到模板
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-27
 */
@Provider
public class NotFoundForward implements ExceptionMapper<NotFoundException> {

    @Inject
    private UriInfo uriInfo;

    private AbstractTemplateProcessor<Boolean> templateProcessor;

    @Inject
    public NotFoundForward(final Configuration config, @Optional final ServletContext servletContext) {
        this.templateProcessor = new AbstractTemplateProcessor<Boolean>(config, servletContext, HttlViewProcessor.CONFIG_SUFFIX, HttlViewProcessor.getExtends(config)) {
            @Override
            protected Boolean resolve(String templatePath, Reader reader) throws Exception {
                return true;
            }

            @Override
            public void writeTo(Boolean aBoolean, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException {

            }
        };
        ;
    }

    @Override
    public Response toResponse(NotFoundException exception) {
        String path = uriInfo.getPath();
        path = path.equals("/") ? "/index" : path;
        try {
            if (templateProcessor.resolve(path, (MediaType) null)) {
                return Response.ok(Viewables.newDefaultViewable(path)).build();
            }
        } catch (Exception e) {
            //noop
        }
        return exception.getResponse();
    }

}
