package ameba.websocket.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

/**
 * @author icode
 */
public class JacksonEncoder implements Encoder {
    @Inject
    ObjectMapper mapper;

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

    public static class Text<T> extends JacksonEncoder implements Encoder.Text<T> {
        @Override
        public String encode(T object) throws EncodeException {
            try {
                return mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new EncodeException(object, "encode json error", e);
            }
        }
    }

    public static class TextStream<T> extends JacksonEncoder implements Encoder.TextStream<T> {
        @Override
        public void encode(T object, Writer writer) throws EncodeException, IOException {
            mapper.writeValue(writer, object);
        }
    }

    public static class Binary<T> extends JacksonEncoder implements Encoder.Binary<T> {
        @Override
        public ByteBuffer encode(T object) throws EncodeException {
            try {
                return ByteBuffer.wrap(mapper.writeValueAsBytes(object));
            } catch (JsonProcessingException e) {
                throw new EncodeException(object, "encode json error", e);
            }
        }
    }

    public static class BinaryStream<T> extends JacksonEncoder implements Encoder.BinaryStream<T> {
        @Override
        public void encode(T object, OutputStream os) throws EncodeException, IOException {
            mapper.writeValue(os, object);
        }
    }
}
