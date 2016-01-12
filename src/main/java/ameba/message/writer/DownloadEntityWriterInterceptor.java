package ameba.message.writer;

import ameba.core.Requests;
import ameba.message.DownloadEntity;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author icode
 */
public class DownloadEntityWriterInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();
        if (entity instanceof DownloadEntity) {
            DownloadEntity downloadEntity = (DownloadEntity) entity;
            entity = downloadEntity.getEntity();
            if (downloadEntity.isDownload()) {

                String download = "attachment;";
                String fileName = downloadEntity.getFileName();

                if (StringUtils.isBlank(fileName) && entity instanceof File) {
                    fileName = ((File) entity).getName();
                }
                if (StringUtils.isNotBlank(fileName)) {
                    String userAgent = Requests.getHeaders().getFirst(HttpHeaders.USER_AGENT);
                    fileName = URLEncoder.encode(fileName, Charsets.UTF_8.name());
                    if (StringUtils.isBlank(userAgent) || userAgent.contains("MSIE") || userAgent.contains("Android"))
                        fileName = "filename=" + fileName;
                    else
                        fileName = "filename*=UTF-8''" + fileName;
                    download += fileName;
                }

                context.getHeaders().putSingle(HttpHeaders.CONTENT_DISPOSITION, download);
            }

            if (downloadEntity.getMediaType() != null) {
                MediaType mediaType = downloadEntity.getMediaType();
                context.setMediaType(mediaType);
                context.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, mediaType);
            }
            context.setEntity(entity);
            if (entity != null) {
                context.setType(entity.getClass());
            } else {
                context.setType(byte[].class);
            }
        }
        context.proceed();
    }
}
