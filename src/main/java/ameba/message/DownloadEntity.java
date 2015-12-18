package ameba.message;

import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;

/**
 * @author icode
 */
public class DownloadEntity implements Serializable {
    private Object entity;
    private boolean download;
    private String fileName;

    protected DownloadEntity() {
    }

    public static Builder file(File file) {
        return new Builder().entity(file);
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

    public boolean isDownload() {
        return download;
    }

    public String getFileName() {
        return fileName;
    }

    public static class Builder {

        private Object entity;
        private boolean download = true;
        private String fileName;

        public Object entity() {
            return entity;
        }

        public boolean download() {
            return download;
        }

        public String fileName() {
            return fileName;
        }


        public Builder entity(File entity) {
            this.entity = entity;
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

        public Builder download(boolean download) {
            this.download = download;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }


        public DownloadEntity build() {
            DownloadEntity entity = new DownloadEntity();
            entity.entity = this.entity;
            entity.download = download;
            entity.fileName = fileName;
            return entity;
        }
    }
}
