package ameba.message;

import ameba.util.MimeType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>Download class.</p>
 *
 * @author icode
 *
 */
public class Download implements Serializable {
    private Object entity;
    private String etag;
    private boolean attachment;
    private String fileName;
    private MediaType mediaType;
    private Long lastModified;

    /**
     * <p>Constructor for Download.</p>
     */
    protected Download() {
    }

    /**
     * <p>file.</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a {@link ameba.message.Download.Builder} object.
     */
    public static Builder file(File file) {
        return new Builder().entity(file);
    }

    /**
     * <p>path.</p>
     *
     * @param path a {@link java.nio.file.Path} object.
     * @return a {@link ameba.message.Download.Builder} object.
     */
    public static Builder path(Path path) {
        return new Builder().entity(path);
    }

    /**
     * <p>bytes.</p>
     *
     * @param bytes an array of byte.
     * @return a {@link ameba.message.Download.Builder} object.
     */
    public static Builder bytes(byte[] bytes) {
        return new Builder().entity(bytes);
    }

    /**
     * <p>inputStream.</p>
     *
     * @param inputStream a {@link java.io.InputStream} object.
     * @return a {@link ameba.message.Download.Builder} object.
     */
    public static Builder inputStream(InputStream inputStream) {
        return new Builder().entity(inputStream);
    }

    /**
     * <p>reader.</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @return a {@link ameba.message.Download.Builder} object.
     */
    public static Builder reader(Reader reader) {
        return new Builder().entity(reader);
    }

    /**
     * <p>streamingOutput.</p>
     *
     * @param streamingOutput a {@link javax.ws.rs.core.StreamingOutput} object.
     * @return a {@link ameba.message.Download.Builder} object.
     */
    public static Builder streamingOutput(StreamingOutput streamingOutput) {
        return new Builder().entity(streamingOutput);
    }

    /**
     * <p>Getter for the field <code>entity</code>.</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * <p>isAttachment.</p>
     *
     * @return a boolean.
     */
    public boolean isAttachment() {
        return attachment;
    }

    /**
     * <p>Getter for the field <code>fileName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * <p>Getter for the field <code>mediaType</code>.</p>
     *
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * <p>Getter for the field <code>etag</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * <p>Getter for the field <code>lastModified</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
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
