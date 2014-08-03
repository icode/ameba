package ameba.mvc.template.internal;

import groovy.lang.Singleton;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.*;
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
@Singleton
public class NotFoundForward implements ExtendedExceptionMapper<NotFoundException> {

    @Inject
    private javax.inject.Provider<UriInfo> uriInfo;

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
    }

    @Override
    public Response toResponse(NotFoundException exception) {
        return Response.ok(Viewables.newDefaultViewable("/" + getCurrentPath())).build();
    }

    private String getCurrentPath() {
        String path = uriInfo.get().getPath();
        return path.equals("/") || path.equals("") ? "/index" : path;
    }

    @Override
    public boolean isMappable(NotFoundException exception) {
        String path = getCurrentPath();
        //受保护目录,不允许直接访问
        if (path.startsWith(AmebaTemplateProcessor.PROTECTED_DIR)) return false;
        try {
            return templateProcessor.resolve("/" + path, (MediaType) null);
        } catch (Exception e) {
            return false;
        }
    }
}