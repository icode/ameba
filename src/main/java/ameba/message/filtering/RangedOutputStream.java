package ameba.message.filtering;

import com.google.common.collect.Lists;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

/**
 * @author icode
 */
public class RangedOutputStream extends OutputStream {

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();
    private static final String BOUNDARY_LINE_FORMAT = "--%s";
    private static final String CONTENT_TYPE_LINE_FORMAT = HttpHeaders.CONTENT_TYPE + ": %s";
    private static final String CONTENT_RANGE_FORMAT = "%s %d-%d/%d";
    private static final String CONTENT_RANGE = "Content-Range";
    private static final String CONTENT_RANGE_LINE_FORMAT = CONTENT_RANGE + ": " + CONTENT_RANGE_FORMAT;
    private static final String EMPTY_LINE = "\r\n";
    private List<Range> ranges;
    private MultivaluedMap<String, Object> headers;
    private OutputStream outputStream;
    private String boundary;
    private String accept;
    private String contentType;
    private boolean multipart;
    private boolean flushed = false;
    private long pos = 0;
    private long contentLength = -1;

    public RangedOutputStream(OutputStream outputStream, String ranges, String contentType, MultivaluedMap<String, Object> headers) {
        this.outputStream = outputStream;
        this.ranges = Lists.newArrayList();
        String[] acceptRanges = ranges.split("=");
        this.accept = acceptRanges[0];
        for (String range : acceptRanges[1].split(",")) {
            String[] bounds = range.split("-");
            this.ranges.add(new Range(Long.valueOf(bounds[0]), bounds.length == 2 ? Long.valueOf(bounds[1]) : null));
        }
        this.headers = headers;
        this.contentType = contentType;
        this.multipart = this.ranges.size() > 1;
        this.boundary = this.generateBoundary();
    }

    private String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30;
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    public boolean isMultipart() {
        return this.multipart;
    }

    public String getBoundary() {
        return this.boundary;
    }

    public String getAcceptRanges() {
        return this.accept;
    }

    public String getContentRange(int index) {
        Range range = this.ranges.get(index);
        return String.format(CONTENT_RANGE_LINE_FORMAT, this.accept, range.getFrom(), range.getTo(this.pos - 1), this.pos);
    }

    @Override
    public void write(int b) throws IOException {
        for (Range range : this.ranges) {
            if (range.contains(this.pos)) {
                range.write(b);
            }
        }
        this.pos++;
    }

    @Override
    public void flush() throws IOException {
        if (this.flushed) {
            return;
        }

        Object len = this.headers.getFirst(HttpHeaders.CONTENT_LENGTH);
        contentLength = this.pos;
        if (len != null) {
            if (len instanceof String) {
                contentLength = Long.parseLong((String) len);
            } else if (len instanceof Long) {
                contentLength = (Long) len;
            } else {
                contentLength = Long.parseLong(String.valueOf(len));
            }
        }
        if (this.multipart) {
            this.headers.putSingle(HttpHeaders.CONTENT_TYPE, String.format("multipart/byteranges; boundary=%s", this.boundary));
            for (Range range : this.ranges) {
                this.outputStream.write(String.format(BOUNDARY_LINE_FORMAT + EMPTY_LINE, this.boundary).getBytes());
                this.outputStream.write(String.format(CONTENT_TYPE_LINE_FORMAT + EMPTY_LINE, this.contentType).getBytes());
                this.outputStream.write(
                        String.format(CONTENT_RANGE_LINE_FORMAT + EMPTY_LINE, this.accept, range.getFrom(),
                                range.getTo(this.pos - 1), contentLength)
                                .getBytes());
                this.outputStream.write(EMPTY_LINE.getBytes());
                this.outputStream.write(range.getBytes());
                this.outputStream.write(EMPTY_LINE.getBytes());
            }
            this.outputStream.write(String.format(BOUNDARY_LINE_FORMAT, this.boundary + "--").getBytes());
        } else {
            Range range = this.ranges.get(0);
            long to = range.getTo(this.pos - 1);
            this.headers.putSingle(CONTENT_RANGE, String.format(CONTENT_RANGE_FORMAT, this.accept, range.getFrom(),
                    to, contentLength));
            this.headers.putSingle(HttpHeaders.CONTENT_LENGTH, to - range.getFrom() + 1);
            this.outputStream.write(range.getBytes());
        }
        this.flushed = true;
    }

    public class Range extends OutputStream {

        private ByteArrayOutputStream outputStream;

        private Long from;

        private Long to;

        public Range(Long from, Long to) {
            this.outputStream = new ByteArrayOutputStream();
            this.from = from;
            this.to = to;
        }

        public boolean contains(Long i) {
            if (this.to == null) {
                return (this.from <= i);
            }
            return (this.from <= i && i <= this.to);
        }

        public byte[] getBytes() {
            return this.outputStream.toByteArray();
        }

        public Long getFrom() {
            return this.from;
        }

        public Long getTo(Long ifNull) {
            return this.to == null ? ifNull : this.to;
        }

        @Override
        public void write(int b) throws IOException {
            this.outputStream.write(b);
        }

    }

}
