package ameba.message.internal;

import ameba.core.Requests;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.message.internal.HeaderUtils;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * @author icode
 */
public class MessageHelper {

    public static final String STREAMING_RANGE_ENABLED = "streaming.range.enabled";

    private MessageHelper() {
    }

    public static void disableCurrentStreamingRange() {
        Requests.setProperty(STREAMING_RANGE_ENABLED, false);
    }

    public static void enableCurrentStreamingRange() {
        Requests.setProperty(STREAMING_RANGE_ENABLED, true);
    }

    public static Iterable<StreamingProcess> getStreamingProcesses(ServiceLocator locator) {
        return Providers.getAllProviders(locator, StreamingProcess.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> StreamingProcess<T> getStreamingProcess(T entity, ServiceLocator locator) {
        for (StreamingProcess process : getStreamingProcesses(locator)) {
            if (process.isSupported(entity)) {
                return process;
            }
        }
        return null;
    }


    public static String getHeaderString(MultivaluedMap<String, Object> headers, String name) {
        return HeaderUtils.asHeaderString(headers.get(name), RuntimeDelegate.getInstance());
    }
}
