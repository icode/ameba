package ameba.message.filtering;

import ameba.Ameba;
import ameba.core.Requests;
import org.glassfish.jersey.message.MessageUtils;
import org.glassfish.jersey.message.internal.HeaderUtils;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Universal logging filter.
 * <br>
 * Can be used on client or server side. Has the highest priority.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Martin Matula
 * @author icode
 */
@PreMatching
@Priority(Integer.MIN_VALUE)
public final class LoggingFilter implements ContainerRequestFilter, ClientRequestFilter, ContainerResponseFilter,
        ClientResponseFilter, WriterInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";
    private static final String ENTITY_LOGGER_PROPERTY = LoggingFilter.class.getName() + ".entityLogger";
    private static final String LOGGING_ID_PROPERTY = LoggingFilter.class.getName() + ".id";
    private static final String LOGGER_BUFFER_PROPERTY = LoggingFilter.class.getName() + ".loggerBuffer";

    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
            (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());

    private static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;

    //
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private final AtomicLong _id = new AtomicLong(0);
    private final boolean printEntity;
    private final int maxEntitySize;
    private Boolean printMoreType;

    /**
     * Create a logging filter logging the request and response to a default JDK
     * logger, named as the fully qualified class name of this class. Entity
     * logging is turned off by default.
     */
    public LoggingFilter() {
        this(LOGGER, false);
    }

    /**
     * Create a logging filter with custom logger and custom settings of entity
     * logging.
     *
     * @param logger      the logger to log requests and responses.
     * @param printEntity if true, entity will be logged as well up to the default maxEntitySize, which is 8KB
     */
    @SuppressWarnings("BooleanParameter")
    public LoggingFilter(final Logger logger, final boolean printEntity) {
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
     *                      and print "...more..." string at the end. Negative values are interpreted as zero.
     */
    public LoggingFilter(final Logger logger, final int maxEntitySize) {
        this.logger = logger;
        this.printEntity = true;
        this.maxEntitySize = Math.max(0, maxEntitySize);
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
            b.append("\n...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    private boolean getPrintMoreType() {
        if (printMoreType == null) {
            this.printMoreType = "true".equalsIgnoreCase((String) Ameba.getApp().getProperty("ameba.trace.enabled"));
        }
        return printMoreType;
    }

    private boolean isSupportPrintType(MediaType mediaType) {
        return mediaType != null && ((getPrintMoreType() && (mediaType.getType().equals("text")
                || mediaType.getSubtype().equals("javascript")))
                || mediaType.getSubtype().equals("json")
                || mediaType.getSubtype().equals("xml"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        final long id = _id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);

        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Sending client request", id, context.getMethod(), context.getUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getStringHeaders());

        if (printEntity && context.hasEntity() && isSupportPrintType(context.getMediaType())) {
            final OutputStream stream = new LoggingStream(b, context.getEntityStream());
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext)
            throws IOException {
        final Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        final long id = requestId != null ? (Long) requestId : _id.incrementAndGet();

        StringBuilder b = (StringBuilder) requestContext.getProperty(LOGGER_BUFFER_PROPERTY);
        if (b == null) {
            b = new StringBuilder();
            requestContext.setProperty(LOGGER_BUFFER_PROPERTY, b);
        }

        printResponseLine(b, "Client response received", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getHeaders());

        if (printEntity && responseContext.hasEntity() && isSupportPrintType(responseContext.getMediaType())) {
            responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream(),
                    MessageUtils.getCharset(responseContext.getMediaType())));
        }

        log(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        final long id = _id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);

        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Server has received a request", id, context.getMethod(), context.getUriInfo().getRequestUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getHeaders());

        if (printEntity && context.hasEntity() && isSupportPrintType(context.getMediaType())) {
            context.setEntityStream(
                    logInboundEntity(b, context.getEntityStream(), MessageUtils.getCharset(context.getMediaType())));
        }

        log(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {

        final Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        final long id = requestId != null ? (Long) requestId : _id.incrementAndGet();

        final StringBuilder b = new StringBuilder();
        requestContext.setProperty(LOGGER_BUFFER_PROPERTY, b);

        printResponseLine(b, "Server responded with a response", id, responseContext.getStatus());
        if (printEntity && isSupportPrintType(responseContext.getMediaType()) && responseContext.hasEntity()) {
            final OutputStream stream = new LoggingStream(b, responseContext.getEntityStream());
            responseContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();

        final Object requestId = Requests.getProperty(LOGGING_ID_PROPERTY);
        final long id = requestId != null ? (Long) requestId : _id.incrementAndGet();
        StringBuilder b = (StringBuilder) writerInterceptorContext.getProperty(LOGGER_BUFFER_PROPERTY);
        if (b == null) {
            b = new StringBuilder();
            writerInterceptorContext.setProperty(LOGGER_BUFFER_PROPERTY, b);
        }
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, HeaderUtils.asStringHeaders(writerInterceptorContext.getHeaders()));

        if (stream != null) {
            log(stream.getStringBuilder(MessageUtils.getCharset(writerInterceptorContext.getMediaType())));
        } else {
            log(b);
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
