package ameba.message.internal;

import ameba.captcha.Captcha;
import ameba.util.Images;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author icode
 */
@Singleton
public class CaptchaWriterInterceptor implements WriterInterceptor {
    private static final MediaType IMG_TYPE = MediaType.valueOf("image/png");

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();
        if (entity instanceof Images.Captcha || entity instanceof Captcha) {
            context.setMediaType(IMG_TYPE);
            context.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, IMG_TYPE);
            if (entity instanceof Captcha) {
                Captcha captcha = (Captcha) entity;
                context.setType(BufferedImage.class);
                context.setEntity(captcha.getImage());
            }
        }
        context.proceed();
    }
}
