package ameba.filter;

import ameba.core.Application;
import com.google.common.base.Charsets;
import org.glassfish.jersey.message.MessageUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author icode
 */
public class BaseLoggingFilter {

    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";
    private static final String REQ_ATTR_ID = BaseLoggingFilter.class.getName() + "__req_id";
    private static final String ENTITY_LOGGER_PROPERTY = BaseLoggingFilter.class.getName() + ".entityLogger";

    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
            new Comparator<Map.Entry<String, List<String>>>() {

                @Override
                public int compare(final Map.Entry<String, List<String>> o1, final Map.Entry<String, List<String>> o2) {
                    return o1.getKey().compareToIgnoreCase(o2.getKey());
                }
            };

    private static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;

    private final Logger logger;
    private final AtomicLong _id = new AtomicLong(0);
    private final boolean printEntity;
    private final int maxEntitySize;
    private Boolean printMoreType;

    @Inject
    private Application application;


    /**
     * Create a logging filter with custom logger and custom settings of entity
     * logging.
     *
     * @param logger      the logger to log requests and responses.
     * @param printEntity if true, entity will be logged as well up to the default maxEntitySize, which is 8KB
     */
    @SuppressWarnings("BooleanParameter")
    public BaseLoggingFilter(final Logger logger, final boolean printEntity) {
        this.logger = logger;
        this.printEntity = printEntity;
        this.maxEntitySize = DEFAULT_MAX_ENTITY_SIZE;
    }

    /**
     * Creates a logging filter with custom logger and entity logging turned on, but potentially limiting the size
     * of entity to be buffered and logged.
     *
     * @param logger        the logger to log requests and responses.
     * @param maxEntitySize maximum number of entity bytes to be logged (and buffered) - if the entity is larger,
     *                      logging filter will print (and buffer in memory) only the specified number of bytes
     *                      and print "...more..." string at the end.
     */
    public BaseLoggingFilter(final Logger logger, final int maxEntitySize) {
        this.logger = logger;
        this.printEntity = true;
        this.maxEntitySize = maxEntitySize;
    }

    private boolean getPrintMoreType() {
        if (printMoreType == null) {
            this.printMoreType = "true".equalsIgnoreCase((String) application.getProperty("ameba.trace.enabled"));
        }
        return printMoreType;
    }

    private void log(final StringBuilder b) {
        if (logger != null) {
            logger.info(b.toString());
        }
    }

    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append(Long.toString(id)).append(" ");
        return b;
    }

    private void printRequestLine(final StringBuilder b, final String note, final long id, final String method, final URI uri) {
        prefixId(b, id).append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName())
                .append("\n");
        prefixId(b, id).append(REQUEST_PREFIX).append(method).append(" ")
                .append(uri.toASCIIString()).append("\n");
    }

    private void printResponseLine(final StringBuilder b, final String note, final long id, final int status) {
        prefixId(b, id).append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName()).append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX)
                .append(Integer.toString(status))
                .append("\n");
    }

    private void printPrefixedHeaders(final StringBuilder b,
                                      final long id,
                                      final String prefix,
                                      final MultivaluedMap<String, String> headers) {
        for (final Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
            final List<?> val = headerEntry.getValue();
            final String header = headerEntry.getKey();

            if (val.size() == 1) {
                prefixId(b, id).append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
            } else {
                final StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (final Object s : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(s);
                }
                prefixId(b, id).append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }

    private Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {
        final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<>(COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    private InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = stream.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize), charset));
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    public void filter(final ContainerRequestContext context) throws IOException {
        final long id = this._id.incrementAndGet();
        context.setProperty(REQ_ATTR_ID, id);
        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Server has received a request", id, context.getMethod(), context.getUriInfo().getRequestUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getHeaders());

        if (printEntity && context.hasEntity()
                && isSupportPrintType(context.getMediaType())) {
            context.setEntityStream(
                    logInboundEntity(b, context.getEntityStream(), getCharset(context.getMediaType())));
        } else if (printEntity && context.hasEntity()) {
            b.append('\n').append("-- stream request entity --").append('\n');
        }

        log(b);
    }

    private boolean isSupportPrintType(MediaType mediaType) {
        return mediaType != null && ((getPrintMoreType() && (mediaType.getType().equals("text")
                || mediaType.getSubtype().equals("javascript")))
                || mediaType.getSubtype().equals("json")
                || mediaType.getSubtype().equals("xml"));
    }

    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        Long id = (Long) requestContext.getProperty(REQ_ATTR_ID);
        final StringBuilder b = new StringBuilder();

        printResponseLine(b, "Server responded with a response", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getStringHeaders());

        if (printEntity && responseContext.hasEntity()
                && isSupportPrintType(responseContext.getMediaType())) {
            final OutputStream stream = new LoggingStream(b, responseContext.getEntityStream());
            responseContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            if (printEntity && responseContext.hasEntity()) {
                b.append('\n').append("-- stream response entity --").append('\n');
            }
            log(b);
        }
    }

    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            log(stream.getStringBuilder(getCharset(writerInterceptorContext.getMediaType())));
        }
    }

    private Charset getCharset(final MediaType media) {
        try {
            return MessageUtils.getCharset(media);
        } catch (Exception e) {
            return Charsets.UTF_8;
        }
    }

    private class LoggingStream extends FilterOutputStream {

        private final StringBuilder b;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LoggingStream(final StringBuilder b, final OutputStream inner) {
            super(inner);

            this.b = b;
        }

        StringBuilder getStringBuilder(final Charset charset) {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, maxEntitySize), charset));
            if (entity.length > maxEntitySize) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= maxEntitySize) {
                baos.write(i);
            }
            out.write(i);
        }
    }
}