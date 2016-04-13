package ameba.message.filtering;

import ameba.core.Requests;
import ameba.message.Download;
import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author icode
 */
@Singleton
public class DownloadEntityFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object entity = responseContext.getEntity();
        if (entity instanceof Download) {
            Download downloadEntity = (Download) entity;
            entity = downloadEntity.getEntity();
            if (downloadEntity.isAttachment()) {

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

                responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_DISPOSITION, download);
            }

            if (downloadEntity.getMediaType() != null) {
                MediaType mediaType = downloadEntity.getMediaType();
                responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, mediaType);
            }
            responseContext.setEntity(entity);
        }
    }
}
