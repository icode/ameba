package ameba.mvc.assets;

import ameba.core.Application;
import ameba.message.internal.MediaType;
import ameba.util.MimeType;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.spi.ExceptionMappers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Singleton
public abstract class AbstractAssetsResource {

    @Inject
    private Application.Mode mode;

    @Inject
    private Provider<ExceptionMappers> mappers;

    protected static void closeJarFileIfNeeded(final JarURLConnection jarConnection,
                                               final JarFile jarFile) throws IOException {
        if (!jarConnection.getUseCaches()) {
            jarFile.close();
        }
    }

    protected static EntityTag computeEntityTag(final File file) {

        final StringBuilder sb = new StringBuilder();
        final long fileLength = file.length();
        final long lastModified = file.lastModified();
        if ((fileLength >= 0) || (lastModified >= 0)) {
            sb.append(fileLength).append('-').
                    append(lastModified);
            return new EntityTag(sb.toString());
        }
        return new EntityTag(RandomStringUtils.random(10));
    }

    protected static File getJarFile(final String path) throws MalformedURLException, FileNotFoundException {
        final int jarDelimIdx = path.indexOf("!/");
        if (jarDelimIdx == -1) {
            return null;
        }

        final File file = new File(path.substring(0, jarDelimIdx));

        if (!file.exists() || !file.isFile()) {
            return null;
        }

        return file;
    }

    protected Response assets(URL url, ContainerRequest request) throws URISyntaxException, IOException {
        File fileResource = null;
        String filePath = null;
        boolean found = false;
        JarURLInputStream jarURLInputStream = null;

        if (url != null) {
            // url may point to a folder or a file
            if ("file".equals(url.getProtocol())) {
                final File file = new File(url.toURI());

                if (file.exists()) {
                    if (file.isDirectory()) {
                        final File welcomeFile = new File(file, "/index.html");
                        if (welcomeFile.exists() && welcomeFile.isFile()) {
                            fileResource = welcomeFile;
                            filePath = welcomeFile.getPath();
                            found = true;
                        }
                    } else {
                        fileResource = file;
                        filePath = file.getPath();
                        found = true;
                    }
                }
            } else {
                if ("jar".equals(url.getProtocol())) {
                    URLConnection urlConnection = url.openConnection();
                    final JarURLConnection jarUrlConnection = (JarURLConnection) urlConnection;
                    JarEntry jarEntry = jarUrlConnection.getJarEntry();
                    final JarFile jarFile = jarUrlConnection.getJarFile();
                    // check if this is not a folder
                    // we can't rely on jarEntry.isDirectory() because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6233323
                    InputStream is = null;

                    if (jarEntry.isDirectory() ||
                            (is = jarFile.getInputStream(jarEntry)) == null) { // it's probably a folder
                        final String welcomeResource =
                                jarEntry.getName().endsWith("/") ?
                                        jarEntry.getName() + "index.html" :
                                        jarEntry.getName() + "/index.html";

                        jarEntry = jarFile.getJarEntry(welcomeResource);
                        if (jarEntry != null) {
                            is = jarFile.getInputStream(jarEntry);
                        }
                    }

                    if (is != null) {
                        jarURLInputStream = new JarURLInputStream(
                                jarUrlConnection,
                                jarEntry,
                                jarFile, is);

                        filePath = jarEntry.getName();
                        found = true;
                    } else {
                        closeJarFileIfNeeded(jarUrlConnection, jarFile);
                    }
                }
            }
        }

        if (!found) {
            return notFound();
        }

        File file = fileResource;

        Response.ResponseBuilder builder = null;

        if (file == null) {
            // if it's not a jar file - we don't know what to do with that
            // so not adding it to the file cache
            if ("jar".equals(url.getProtocol())) {
                file = getJarFile(
                        // we need that because url.getPath() may have url encoded symbols,
                        // which are getting decoded when calling uri.getPath()
                        new URI(url.getPath()).getPath()
                );
            }
        }

        if (file == null) {
            return notFound();
        }

        EntityTag eTag = null;
        Date lastModified = null;
        if (isFileCacheEnabled()) {

            eTag = computeEntityTag(file);
            lastModified = new Date(file.lastModified());

            builder = request.evaluatePreconditions(
                    lastModified, eTag);
        }

        // the resoruce's information was modified, return it
        if (builder == null) {
            builder = Response.ok();

            if (fileResource != null) {
                builder.entity(fileResource.toPath());
            } else {
                builder.entity(jarURLInputStream)
                        .header(HttpHeaders.CONTENT_LENGTH, jarURLInputStream.jarEntry.getSize());
            }

            if (isFileCacheEnabled()) {
                builder.tag(eTag).lastModified(lastModified);
            }

            int dot = filePath.lastIndexOf('.');

            if (dot > 0) {
                String ext = filePath.substring(dot + 1);
                String ct = MimeType.get(ext, MediaType.APPLICATION_OCTET_STREAM);
                if (ct != null) {
                    builder.header(HttpHeaders.CONTENT_TYPE, ct);
                }
            } else {
                builder.type(MimeType.get("html"));
            }
        }

        return builder.build();
    }

    protected Response notFound() {
        Throwable e = new NotFoundException();
        return Response.fromResponse(mappers.get().findMapping(e).toResponse(e))
                .type(MediaType.TEXT_HTML_TYPE).build();
    }

    protected boolean isFileCacheEnabled() {
        return mode.isProd();
    }

    protected static class JarURLInputStream extends FilterInputStream {

        private final JarURLConnection jarConnection;
        private final JarFile jarFile;
        private final JarEntry jarEntry;

        JarURLInputStream(final JarURLConnection jarConnection,
                          final JarEntry jarEntry,
                          final JarFile jarFile,
                          final InputStream src) {
            super(src);
            this.jarConnection = jarConnection;
            this.jarFile = jarFile;
            this.jarEntry = jarEntry;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                closeJarFileIfNeeded(jarConnection, jarFile);
            }
        }
    }
}
