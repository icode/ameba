package ameba.websocket;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>WebSocket class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebSocket {

    /**
     * websocket path
     *
     * @return path
     */
    String path();

    /**
     * Enable SockJS support
     *
     * @return SockJS enabled
     */
    boolean withSockJS() default false;

    /**
     * The ordered array of web socket protocols this endpoint supports.
     * For example, {"superchat", "chat"}.
     *
     * @return the subprotocols.
     */
    String[] subprotocols() default {};

    /**
     * The ordered array of decoder classes this endpoint will use. For example,
     * if the developer has provided a MysteryObject decoder, this endpoint will be able to
     * receive MysteryObjects as web socket messages. The websocket runtime will use the first
     * decoder in the list able to decode a message, ignoring the remaining decoders.
     *
     * @return the decoders.
     */
    Class<? extends Decoder>[] decoders() default {};

    /**
     * The ordered array of encoder classes this endpoint will use. For example,
     * if the developer has provided a MysteryObject encoder, this class will be able to
     * send web socket messages in the form of MysteryObjects. The websocket runtime will use the first
     * encoder in the list able to encode a message, ignoring the remaining encoders.
     *
     * @return the encoders.
     */
    Class<? extends Encoder>[] encoders() default {};


    /**
     * The optional custom configurator class that the developer would like to use
     * to further configure new instances of this endpoint. If no configurator
     * class is provided, the implementation uses its own.  The implementation
     * creates a new instance of the configurator per logical endpoint.
     *
     * @return the custom configuration class, or ServerEndpointConfig.Configurator.class
     * if none was set in the annotation.
     */
    Class<? extends ServerEndpointConfig.Configurator> configurator() default ServerEndpointConfig.Configurator.class;

    /**
     * <p>extensions.</p>
     *
     * @return an array of {@link java.lang.Class} objects.
     */
    Class<? extends Extension>[] extensions() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface On {
        /**
         * 映射的action/event名称，如果为空则同方法名
         *
         * @return mapping name
         */
        String value() default "";

        Class<? extends Encoder>[] encoders() default {};

        Class<? extends Decoder>[] decoders() default {};
    }
}