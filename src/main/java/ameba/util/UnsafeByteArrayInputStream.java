package ameba.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author icode
 */
public class UnsafeByteArrayInputStream extends InputStream {

    protected byte data[];

    protected int  position, limit, mark = 0;

    public UnsafeByteArrayInputStream(byte buf[]){
        this(buf, 0, buf.length);
    }

    public UnsafeByteArrayInputStream(byte buf[], int offset){
        this(buf, offset, buf.length - offset);
    }

    public UnsafeByteArrayInputStream(byte buf[], int offset, int length){
        data = buf;
        position = mark = offset;
        limit = Math.min(offset + length, buf.length);
    }

    public int read() {
        return (position < limit) ? (data[position++] & 0xff) : -1;
    }

    public int read(byte b[], int off, int len) {
        if (b == null) throw new NullPointerException("read byte[] == null");
        if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
        if (position >= limit) return -1;
        if (position + len > limit) len = limit - position;
        if (len <= 0) return 0;
        System.arraycopy(data, position, b, off, len);
        position += len;
        return len;
    }

    public long skip(long len) {
        if (position + len > limit) len = limit - position;
        if (len <= 0) return 0;
        position += len;
        return len;
    }

    public int available() {
        return limit - position;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) {
        mark = position;
    }

    public void reset() {
        position = mark;
    }

    public void close() throws IOException {
    }

    public int position() {
        return position;
    }

    public void position(int newPosition) {
        position = newPosition;
    }

    public int size() {
        return data == null ? 0 : data.length;
    }
}