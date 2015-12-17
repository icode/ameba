package ameba.filter;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author icode
 */
public class RangedOutputStream extends OutputStream {

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();
    private static final String BOUNDARY_LINE_FORMAT = "--%s";
    private static final String CONTENT_TYPE_LINE_FORMAT = "Content-Type: %s";
    private static final String CONTENT_RANGE_FORMAT = "%s %d-%d/%d";
    private static final String CONTENT_RANGE_LINE_FORMAT = "Content-Range: " + CONTENT_RANGE_FORMAT;
    private static final String EMPTY_LINE = "\r\n";
    List<Range> ranges;
    MultivaluedMap<String, Object> headers;
    private OutputStream outputStream;
    private String boundary;
    private String accept;
    private String contentType;
    private boolean multipart;
    private boolean flushed = false;
    private int pos = 0;

    public RangedOutputStream(OutputStream outputStream, String ranges, String contentType, MultivaluedMap<String, Object> headers) {
        this.outputStream = outputStream;
        this.ranges = new ArrayList<>();
        String[] acceptRanges = ranges.split("=");
        this.accept = acceptRanges[0];
        for (String range : acceptRanges[1].split(",")) {
            String[] bounds = range.split("-");
            this.ranges.add(new Range(Integer.valueOf(bounds[0]), bounds.length == 2 ? Integer.valueOf(bounds[1]) : null));
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
        return String.format(CONTENT_RANGE_LINE_FORMAT, this.accept, range.getFrom(), range.getTo(this.pos), this.pos);
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
        if (this.multipart) {
            this.headers.putSingle(HttpHeaders.CONTENT_TYPE, String.format("multipart/byteranges; boundary=%s", this.boundary));
            for (Range range : this.ranges) {
                this.outputStream.write(String.format(BOUNDARY_LINE_FORMAT + EMPTY_LINE, this.boundary).getBytes());
                this.outputStream.write(String.format(CONTENT_TYPE_LINE_FORMAT + EMPTY_LINE, this.contentType).getBytes());
                this.outputStream.write(
                        String.format(CONTENT_RANGE_LINE_FORMAT + EMPTY_LINE, this.accept, range.getFrom(), range.getTo(this.pos), this.pos)
                                .getBytes());
                this.outputStream.write(EMPTY_LINE.getBytes());
                this.outputStream.write(range.getBytes());
                this.outputStream.write(EMPTY_LINE.getBytes());
            }
            this.outputStream.write(String.format(BOUNDARY_LINE_FORMAT, this.boundary + "--").getBytes());
        } else {
            Range range = this.ranges.get(0);
            this.headers.putSingle("Content-Range", String.format(CONTENT_RANGE_FORMAT, this.accept, range.getFrom(), range.getTo(this.pos), this.pos));
            this.outputStream.write(range.getBytes());
        }
        this.flushed = true;
    }

    public class Range extends OutputStream {

        private ByteArrayOutputStream outputStream;

        private Integer from;

        private Integer to;

        public Range(Integer from, Integer to) {
            this.outputStream = new ByteArrayOutputStream();
            this.from = from;
            this.to = to;
        }

        public boolean contains(Integer i) {
            if (this.to == null) {
                return (this.from <= i);
            }
            return (this.from <= i && i <= this.to);
        }

        public byte[] getBytes() {
            return this.outputStream.toByteArray();
        }

        public Integer getFrom() {
            return this.from;
        }

        public Integer getTo(Integer ifNull) {
            return this.to == null ? ifNull : this.to;
        }

        @Override
        public void write(int b) throws IOException {
            this.outputStream.write(b);
        }

    }

}
