package ameba.message.internal;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

/**
 * @author icode
 */
public class MediaStreaming implements StreamingOutput {

    public static final String RANGE = "Range";
    public static final String CONTENT_RANGE = "Content-Range";
    private static final String EMPTY_LINE = "\r\n";
    private static final String MULTIPART_BYTERANGES = "multipart/byteranges; boundary=%s";
    private static final String BOUNDARY_LINE_FORMAT = "--%s" + EMPTY_LINE;
    private static final String CONTENT_TYPE_LINE_FORMAT = HttpHeaders.CONTENT_TYPE + ": %s" + EMPTY_LINE;
    private static final String CONTENT_RANGE_FORMAT = "%s %d-%d/%d";
    private static final String CONTENT_RANGE_LINE_FORMAT = CONTENT_RANGE + ": " + CONTENT_RANGE_FORMAT + EMPTY_LINE;
    private static final Random RANDOM = new Random();

    private MultivaluedMap<String, Object> headers;
    private javax.ws.rs.core.MediaType contentType;
    private Object entity;
    private StreamingProcess<Object> streamingProcess;

    public MediaStreaming(Object entity,
                          StreamingProcess<Object> streamingProcess,
                          javax.ws.rs.core.MediaType contentType,
                          MultivaluedMap<String, Object> headers) {
        this.entity = entity;
        this.streamingProcess = streamingProcess;
        this.contentType = contentType;
        this.headers = headers;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        List<Range> ranges = Lists.newArrayList();
        String[] acceptRanges = MessageHelper.getHeaderString(headers, RANGE).split("=");
        String accept = acceptRanges[0];
        for (String range : acceptRanges[1].split(",")) {
            String[] bounds = range.split("-");
            ranges.add(new Range(Long.valueOf(bounds[0]), bounds.length == 2 ? Long.valueOf(bounds[1]) : null));
        }
        boolean multipart = ranges.size() > 1;
        Object len = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
        long contentLength;
        if (len != null) {
            if (len instanceof String) {
                contentLength = Long.parseLong((String) len);
            } else if (len instanceof Long) {
                contentLength = (Long) len;
            } else {
                contentLength = Long.parseLong(String.valueOf(len));
            }
        } else {
            contentLength = streamingProcess.length(entity);
        }

        if (multipart) {
            int count = RANDOM.nextInt(11) + 20;
            String boundary = RandomStringUtils.randomAlphanumeric(count);
            headers.putSingle(HttpHeaders.CONTENT_TYPE, String.format(MULTIPART_BYTERANGES, boundary));
            for (Range range : ranges) {
                output.write(String.format(BOUNDARY_LINE_FORMAT, boundary).getBytes());
                output.write(String.format(CONTENT_TYPE_LINE_FORMAT, contentType).getBytes());
                long to = range.getTo(contentLength - 1);
                output.write(
                        String.format(CONTENT_RANGE_LINE_FORMAT,
                                accept, range.getFrom(),
                                to,
                                contentLength
                        ).getBytes()
                );
                output.write(EMPTY_LINE.getBytes());
                long currentLength = to - range.getFrom() + 1;
                streamingProcess.write(entity, output, range.getFrom(), range.to == null ? null : currentLength);
                output.write(EMPTY_LINE.getBytes());
            }
            output.write(String.format(BOUNDARY_LINE_FORMAT, boundary + "--").getBytes());
        } else {
            Range range = ranges.get(0);
            long to = range.getTo(contentLength - 1);
            headers.putSingle(CONTENT_RANGE,
                    String.format(CONTENT_RANGE_FORMAT,
                            accept, range.getFrom(),
                            to, contentLength)
            );
            long currentLength = to - range.getFrom() + 1;
            headers.putSingle(HttpHeaders.CONTENT_LENGTH, currentLength);
            streamingProcess.write(entity, output, range.getFrom(), range.to == null ? null : currentLength);
        }
    }


    public class Range {
        private Long from;

        private Long to;

        public Range(Long from, Long to) {
            this.from = from;
            this.to = to;
        }

        public boolean contains(Long i) {
            if (this.to == null) {
                return (this.from <= i);
            }
            return (this.from <= i && i <= this.to);
        }

        public Long getFrom() {
            return this.from;
        }

        public Long getTo(Long ifNull) {
            return this.to == null ? ifNull : this.to;
        }

    }
}
