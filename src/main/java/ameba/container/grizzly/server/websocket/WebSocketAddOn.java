package ameba.container.grizzly.server.websocket;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.tyrus.spi.ServerContainer;

/**
 * @author icode
 */
public class WebSocketAddOn implements AddOn {

    private final ServerContainer serverContainer;

    public WebSocketAddOn(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
    }

    @Override
    public void setup(NetworkListener networkListener, FilterChainBuilder builder) {
        // Get the index of HttpServerFilter in the HttpServer filter chain
        final int httpServerFilterIdx = builder.indexOfType(HttpServerFilter.class);

        if (httpServerFilterIdx >= 0) {
            // Insert the WebSocketFilter right before HttpServerFilter
            builder.add(httpServerFilterIdx, new GrizzlyServerFilter(serverContainer));
        }
    }
}