package ameba.message.writer;

import ameba.message.DownloadEntity;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLEncoder;

/**
 * @author icode
 */
@Produces({"application/octet-stream", "*/*"})
public class DownloadEntityMessageBodyWriter implements MessageBodyWriter<DownloadEntity> {

    @Inject
    private Provider<MessageBodyWorkers> workers;
    private MessageBodyWriter writer;

    public MessageBodyWriter getWriter(Class type, Annotation[] annotations, MediaType mediaType) {
        if (writer == null) {
            writer = workers.get().getMessageBodyWriter(type, null, annotations, mediaType);
        }
        if (writer == null)
            throw new MessageBodyProviderNotFoundException("MessageBodyWriter not found for download [entity=" + type + "]");
        return writer;
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DownloadEntity.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public long getSize(DownloadEntity downloadEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        Object entity = downloadEntity.getEntity();
        if (entity == null)
            return 0;
        return getWriter(entity.getClass(), annotations, mediaType)
                .getSize(downloadEntity.getEntity(), entity.getClass(), null, annotations, mediaType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeTo(DownloadEntity downloadEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        Object entity = downloadEntity.getEntity();
        Class entityClass = entity.getClass();
        if (downloadEntity.isDownload()) {

            String download = "attachment;";
            String fileName = downloadEntity.getFileName();
            if (StringUtils.isBlank(fileName) && entity instanceof File) {
                fileName = ((File) entity).getName();
            }
            if (StringUtils.isNotBlank(fileName)) {
                String userAgent = (String) httpHeaders.getFirst(HttpHeaders.USER_AGENT);
                if (userAgent != null) {
                    userAgent = userAgent.toLowerCase();
                } else {
                    userAgent = "";
                }
                fileName = URLEncoder.encode(fileName, Charsets.UTF_8.name());
                if (userAgent.contains("msie"))
                    fileName = "filename=" + fileName;
                else
                    fileName = "filename*=UTF-8''" + fileName;
                download += fileName;
            }

            httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, download);
        }

        if (downloadEntity.getMediaType() != null) {
            mediaType = downloadEntity.getMediaType();
            httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaType);
        }

        getWriter(entityClass, annotations, mediaType)
                .writeTo(entity, entityClass, null, annotations, mediaType, httpHeaders, entityStream);
    }

}
