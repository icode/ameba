package ameba.websocket.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;

import javax.inject.Inject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;

/**
 * @author icode
 */
public abstract class JacksonDecoder implements Decoder {
    @Inject
    ObjectMapper mapper;

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

    public abstract static class Text<T> extends JacksonDecoder implements Decoder.Text<T> {
        private Class<T> objectClass;

        protected Text(Class<T> objectClass) {
            this.objectClass = objectClass;
        }

        @Override
        public T decode(String s) throws DecodeException {
            try {
                return mapper.readValue(s, objectClass);
            } catch (IOException e) {
                throw new DecodeException(s, "decode json error", e);
            }
        }

        @Override
        public boolean willDecode(String s) {
            try {
                mapper.readTree(s);
            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }

    public abstract static class TextStream<T> extends JacksonDecoder implements Decoder.TextStream<T> {
        private Class<T> objectClass;

        protected TextStream(Class<T> objectClass) {
            this.objectClass = objectClass;
        }

        @Override
        public T decode(Reader reader) throws DecodeException, IOException {
            return mapper.readValue(reader, objectClass);
        }
    }

    public abstract static class Binary<T> extends JacksonDecoder implements Decoder.Binary<T> {
        private Class<T> objectClass;

        protected Binary(Class<T> objectClass) {
            this.objectClass = objectClass;
        }

        @Override
        public T decode(ByteBuffer bytes) throws DecodeException {
            try {
                return mapper.readValue(new ByteBufferBackedInputStream(bytes), objectClass);
            } catch (IOException e) {
                throw new DecodeException(bytes, "decode json error", e);
            }
        }

        @Override
        public boolean willDecode(ByteBuffer bytes) {
            try {
                mapper.readTree(new ByteBufferBackedInputStream(bytes));
            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }

    public abstract static class BinaryStream<T> extends JacksonDecoder implements Decoder.BinaryStream<T> {
        private Class<T> objectClass;

        protected BinaryStream(Class<T> objectClass) {
            this.objectClass = objectClass;
        }

        @Override
        public T decode(InputStream is) throws DecodeException, IOException {
            try (Reader reader = new InputStreamReader(is)) {
                return mapper.readValue(reader, objectClass);
            }
        }
    }
}
