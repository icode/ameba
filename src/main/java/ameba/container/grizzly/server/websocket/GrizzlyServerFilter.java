package ameba.container.grizzly.server.websocket;

import org.glassfish.grizzly.*;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeHolder;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.memory.ByteBufferArray;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyWriter;
import org.glassfish.tyrus.container.grizzly.client.TaskProcessor;
import org.glassfish.tyrus.core.CloseReasons;
import org.glassfish.tyrus.core.RequestContext;
import org.glassfish.tyrus.core.TyrusUpgradeResponse;
import org.glassfish.tyrus.core.Utils;
import org.glassfish.tyrus.spi.Connection;
import org.glassfish.tyrus.spi.ReadHandler;
import org.glassfish.tyrus.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * @author icode
 */
public class GrizzlyServerFilter extends BaseFilter {

    private static final Logger logger = LoggerFactory.getLogger(GrizzlyServerFilter.class);

    private static final Attribute<Connection> TYRUS_CONNECTION = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
            .createAttribute(GrizzlyServerFilter.class.getName() + ".Connection");

    private static final Attribute<TaskProcessor> TASK_PROCESSOR = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
            .createAttribute(TaskProcessor.class.getName() + ".TaskProcessor");

    private final ServerContainer serverContainer;


    // ------------------------------------------------------------ Constructors

    /**
     * Constructs a new {@link GrizzlyServerFilter}.
     *
     * @param serverContainer
     */
    public GrizzlyServerFilter(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
    }

    // ----------------------------------------------------- Methods from Filter

    private static UpgradeRequest createWebSocketRequest(final HttpContent requestContent) {

        final HttpRequestPacket requestPacket = (HttpRequestPacket) requestContent.getHttpHeader();

        final RequestContext requestContext = RequestContext.Builder.create()
                .requestURI(URI.create(requestPacket.getRequestURI()))
                .queryString(requestPacket.getQueryString())
                .secure(requestPacket.isSecure())
                .remoteAddr(requestPacket.getRemoteAddress())
                .build();

        for (String name : requestPacket.getHeaders().names()) {
            final List<String> values = requestContext.getHeaders().get(name);
            if (values == null) {
                requestContext.getHeaders().put(name, Utils.parseHeaderValue(requestPacket.getHeader(name).trim()));
            } else {
                values.addAll(Utils.parseHeaderValue(requestPacket.getHeader(name).trim()));
            }
        }

        return requestContext;
    }

    /**
     * Method handles Grizzly {@link org.glassfish.grizzly.Connection} close phase. Check if the {@link org.glassfish.grizzly.Connection} is a {@link org.glassfish.tyrus.core.TyrusWebSocket}, if
     * yes - tries to close the websocket gracefully (sending close frame) and calls {@link
     * org.glassfish.tyrus.core.TyrusWebSocket#onClose(org.glassfish.tyrus.core.frame.CloseFrame)}. If the Grizzly {@link org.glassfish.grizzly.Connection} is not websocket - passes processing to the next
     * filter in the chain.
     *
     * @param ctx {@link org.glassfish.grizzly.filterchain.FilterChainContext}
     * @return {@link org.glassfish.grizzly.filterchain.NextAction} instruction for {@link org.glassfish.grizzly.filterchain.FilterChain}, how it should continue the execution
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {

        final org.glassfish.tyrus.spi.Connection connection = getConnection(ctx);
        if (connection != null) {
            TaskProcessor taskProcessor = getTaskProcessor(ctx);
            taskProcessor.processTask(new CloseTask(connection, CloseReasons.CLOSED_ABNORMALLY.getCloseReason(), ctx.getConnection()));
        }
        return ctx.getStopAction();
    }

    /**
     * Handle Grizzly {@link org.glassfish.grizzly.Connection} read phase. If the {@link org.glassfish.grizzly.Connection} has associated {@link org.glassfish.tyrus.core.TyrusWebSocket} object
     * (websocket connection), we check if websocket handshake has been completed for this connection, if not -
     * initiate/validate handshake. If handshake has been completed - parse websocket {@link org.glassfish.tyrus.core.frame.Frame}s one by one and
     * pass processing to appropriate {@link org.glassfish.tyrus.core.TyrusWebSocket}: {@link org.glassfish.tyrus.core.TyrusEndpointWrapper} for server- and client- side
     * connections.
     *
     * @param ctx {@link FilterChainContext}
     * @return {@link NextAction} instruction for {@link org.glassfish.grizzly.filterchain.FilterChain}, how it should continue the execution
     * @throws IOException
     */
    @Override
    @SuppressWarnings("unchecked")
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        // Get the parsed HttpContent (we assume prev. filter was HTTP)
        final HttpContent message = ctx.getMessage();

        final org.glassfish.tyrus.spi.Connection tyrusConnection = getConnection(ctx);

        logger.debug("handleRead websocket: {0} content-size={1} headers=\n{2}",
                tyrusConnection, message.getContent().remaining(), message.getHttpHeader());

        if (tyrusConnection == null) {
            // Get the HTTP header
            final HttpHeader header = message.getHttpHeader();

            // If websocket is null - it means either non-websocket Connection
            if (!UpgradeRequest.WEBSOCKET.equalsIgnoreCase(header.getUpgrade()) && message.getHttpHeader().isRequest()) {
                // if it's not a websocket connection - pass the processing to the next filter
                return ctx.getInvokeAction();
            }

            final String ATTR_NAME = "org.glassfish.tyrus.container.grizzly.WebSocketFilter.HANDSHAKE_PROCESSED";

            final AttributeHolder attributeHolder = ctx.getAttributes();
            if (attributeHolder != null) {
                final Object attribute = attributeHolder.getAttribute(ATTR_NAME);
                if (attribute != null) {
                    // handshake was already performed on this context.
                    return ctx.getInvokeAction();
                } else {
                    attributeHolder.setAttribute(ATTR_NAME, true);
                }
            }
            // Handle handshake
            return handleHandshake(ctx, message);
        }

        // tyrusConnection is not null
        // this is websocket with the completed handshake
        if (message.getContent().hasRemaining()) {
            // get the frame(s) content

            Buffer buffer = message.getContent();
            message.recycle();
            final ReadHandler readHandler = tyrusConnection.getReadHandler();
            TaskProcessor taskProcessor = getTaskProcessor(ctx);
            if (!buffer.isComposite()) {
                taskProcessor.processTask(new ProcessTask(buffer.toByteBuffer(), readHandler));
            } else {
                final ByteBufferArray byteBufferArray = buffer.toByteBufferArray();
                final ByteBuffer[] array = byteBufferArray.getArray();

                for (int i = 0; i < byteBufferArray.size(); i++) {
                    taskProcessor.processTask(new ProcessTask(array[i], readHandler));
                }

                byteBufferArray.recycle();
            }
        }
        return ctx.getStopAction();
    }

    private org.glassfish.tyrus.spi.Connection getConnection(FilterChainContext ctx) {
        return TYRUS_CONNECTION.get(ctx.getConnection());
    }

    private TaskProcessor getTaskProcessor(FilterChainContext ctx) {
        return TASK_PROCESSOR.get(ctx.getConnection());
    }

    /**
     * Handle websocket handshake
     *
     * @param ctx     {@link FilterChainContext}
     * @param content HTTP message
     * @return {@link NextAction} instruction for {@link org.glassfish.grizzly.filterchain.FilterChain}, how it should continue the execution
     */
    private NextAction handleHandshake(final FilterChainContext ctx, HttpContent content) {
        final UpgradeRequest upgradeRequest = createWebSocketRequest(content);
        final UpgradeResponse upgradeResponse = new TyrusUpgradeResponse();
        final WebSocketEngine.UpgradeInfo upgradeInfo = serverContainer.getWebSocketEngine().upgrade(upgradeRequest, upgradeResponse);

        switch (upgradeInfo.getStatus()) {
            case SUCCESS:
                final org.glassfish.grizzly.Connection grizzlyConnection = ctx.getConnection();
                write(ctx, upgradeRequest, upgradeResponse);

                final org.glassfish.tyrus.spi.Connection connection = upgradeInfo.createConnection(new GrizzlyWriter(ctx.getConnection()), new org.glassfish.tyrus.spi.Connection.CloseListener() {
                    @Override
                    public void close(javax.websocket.CloseReason reason) {
                        grizzlyConnection.close();
                    }
                });

                TYRUS_CONNECTION.set(grizzlyConnection, connection);
                TASK_PROCESSOR.set(grizzlyConnection, new TaskProcessor());

                grizzlyConnection.addCloseListener(new CloseListener() {
                    @Override
                    public void onClosed(Closeable closeable, ICloseType type) throws IOException {
                        // close detected on connection
                        connection.close(CloseReasons.GOING_AWAY.getCloseReason());
                        // might not be necessary, connection is going to be recycled/freed anyway
                        TYRUS_CONNECTION.remove(grizzlyConnection);
                        TASK_PROCESSOR.remove(grizzlyConnection);
                    }
                });

                return ctx.getStopAction();

            case HANDSHAKE_FAILED:
                write(ctx, upgradeRequest, upgradeResponse);
                content.recycle();
                return ctx.getStopAction();

            case NOT_APPLICABLE:
                return ctx.getInvokeAction();
        }

        return ctx.getStopAction();
    }

    private void write(FilterChainContext ctx, UpgradeRequest request, UpgradeResponse response) {
        final HttpResponsePacket responsePacket = ((HttpRequestPacket) ((HttpContent) ctx.getMessage()).getHttpHeader()).getResponse();
        responsePacket.setProtocol(Protocol.HTTP_1_1);
        responsePacket.setStatus(response.getStatus());

        for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            responsePacket.setHeader(entry.getKey(), Utils.getHeaderFromList(entry.getValue()));
        }

        ctx.write(HttpContent.builder(responsePacket).build());
    }

    private class ProcessTask extends TaskProcessor.Task {
        private final ByteBuffer buffer;
        private final ReadHandler readHandler;

        private ProcessTask(ByteBuffer buffer, ReadHandler readHandler) {
            this.buffer = buffer;
            this.readHandler = readHandler;
        }

        @Override
        public void execute() {
            readHandler.handle(buffer);
        }
    }

    private class CloseTask extends TaskProcessor.Task {
        private final org.glassfish.tyrus.spi.Connection connection;
        private final javax.websocket.CloseReason closeReason;
        private final org.glassfish.grizzly.Connection grizzlyConnection;

        private CloseTask(org.glassfish.tyrus.spi.Connection connection, javax.websocket.CloseReason closeReason, org.glassfish.grizzly.Connection grizzlyConnection) {
            this.connection = connection;
            this.closeReason = closeReason;
            this.grizzlyConnection = grizzlyConnection;
        }

        @Override
        public void execute() {
            connection.close(closeReason);
            TYRUS_CONNECTION.remove(grizzlyConnection);
            TASK_PROCESSOR.remove(grizzlyConnection);
        }
    }
}