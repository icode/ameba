package ameba.message;

import ameba.util.MimeType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author icode
 */
public class Download implements Serializable {
    private Object entity;
    private String etag;
    private boolean attachment;
    private String fileName;
    private MediaType mediaType;
    private Long lastModified;

    protected Download() {
    }

    public static Builder file(File file) {
        return new Builder().entity(file);
    }

    public static Builder path(Path path) {
        return new Builder().entity(path);
    }

    public static Builder bytes(byte[] bytes) {
        return new Builder().entity(bytes);
    }

    public static Builder inputStream(InputStream inputStream) {
        return new Builder().entity(inputStream);
    }

    public static Builder reader(Reader reader) {
        return new Builder().entity(reader);
    }

    public static Builder streamingOutput(StreamingOutput streamingOutput) {
        return new Builder().entity(streamingOutput);
    }

    public Object getEntity() {
        return entity;
    }

    public boolean isAttachment() {
        return attachment;
    }

    public String getFileName() {
        return fileName;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getEtag() {
        return etag;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public static class Builder {

        private Object entity;
        private String etag;
        private Long lastModified;
        private boolean attachment = true;
        private String fileName;
        private MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;

        private static String computeEntityTag(final Path path) {
            final StringBuilder sb = new StringBuilder();
            long fileLength;
            long lastModified;
            try {
                fileLength = Files.size(path);
                lastModified = Files.getLastModifiedTime(path).toMillis();
            } catch (Exception e) {
                return null;
            }
            if ((fileLength >= 0) || (lastModified >= 0)) {
                sb.append(fileLength).append('-').
                        append(lastModified);
                return sb.toString();
            }
            return null;
        }

        public Object entity() {
            return entity;
        }

        public boolean attachment() {
            return attachment;
        }

        public String fileName() {
            return fileName;
        }

        public MediaType mediaType() {
            return mediaType;
        }

        public Builder entity(File entity) {
            return entity(entity.toPath());
        }

        public Builder entity(Path entity) {
            this.entity = entity;
            if (fileName == null) {
                fileName = entity.getFileName().toString();
            }
            if (etag == null) {
                etag = computeEntityTag(entity);
            }
            if (lastModified == null) {
                try {
                    lastModified = Files.getLastModifiedTime(entity).toMillis();
                } catch (IOException e) {
                    //
                }
            }
            return this;
        }

        public Builder entity(InputStream entity) {
            this.entity = entity;
            return this;
        }

        public Builder entity(Reader entity) {
            this.entity = entity;
            return this;
        }

        public Builder entity(StreamingOutput entity) {
            this.entity = entity;
            return this;
        }

        public Builder entity(byte[] entity) {
            this.entity = entity;
            return this;
        }

        public Builder attachment(boolean attachment) {
            this.attachment = attachment;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder mediaType(MediaType type) {
            this.mediaType = type;
            return this;
        }

        public Builder etag(String etag) {
            this.etag = etag;
            return this;
        }

        public String etag() {
            return etag;
        }

        public Builder lastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Long lastModified() {
            return lastModified;
        }

        public Builder disableCache() {
            etag = null;
            lastModified = null;
            return this;
        }

        public Builder detectMediaType() {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            if (fileName != null) {
                String type = MimeType.getByFilename(fileName);
                if (type != null) {
                    mediaType = MediaType.valueOf(type);
                }
            }
            return this;
        }

        public Download build() {
            Download entity = new Download();
            entity.entity = this.entity;
            entity.attachment = attachment;
            entity.fileName = fileName;
            entity.mediaType = mediaType;
            entity.etag = etag;
            entity.lastModified = lastModified;
            return entity;
        }
    }
}
