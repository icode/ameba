package ameba.message;

import ameba.util.MimeType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * @author icode
 */
public class Download implements Serializable {
    private Object entity;
    private boolean attachment;
    private String fileName;
    private MediaType mediaType;

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

    public static class Builder {

        private Object entity;
        private boolean attachment = true;
        private String fileName;
        private MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;

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
            this.entity = entity;
            if (fileName == null) {
                fileName = entity.getName();
            }
            return this;
        }

        public Builder entity(Path entity) {
            this.entity = entity;
            if (fileName == null) {
                fileName = entity.getFileName().toString();
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
            return entity;
        }
    }
}
