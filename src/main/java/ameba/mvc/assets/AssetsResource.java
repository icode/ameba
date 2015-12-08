package ameba.mvc.assets;

import ameba.container.server.Request;
import ameba.core.Application;
import ameba.message.internal.MediaType;
import ameba.util.MimeType;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.spi.ExceptionMappers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>AssetsResource class.</p>
 *
 * @author ICode
 * @since 13-8-17 下午2:55
 */
@Path("assets")
@Singleton
public class AssetsResource {

    @Inject
    private Application application;

    @Inject
    private Provider<ExceptionMappers> mappers;

    private static void closeJarFileIfNeeded(final JarURLConnection jarConnection,
                                             final JarFile jarFile) throws IOException {
        if (!jarConnection.getUseCaches()) {
            jarFile.close();
        }
    }

    private static EntityTag computeEntityTag(final File file) {

        final StringBuilder sb = new StringBuilder();
        final long fileLength = file.length();
        final long lastModified = file.lastModified();
        if ((fileLength >= 0) || (lastModified >= 0)) {
            sb.append(fileLength).append('-').
                    append(lastModified);
            return new EntityTag(sb.toString());
        }
        return null;
    }

    /**
     * <p>getResource.</p>
     *
     * @param fileName a {@link java.lang.String} object.
     * @param request  request
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws URISyntaxException uri error
     * @throws IOException        io error
     */
    @GET
    @Path("{file:.*}")
    public Response getResource(@PathParam("file") String fileName,
                                @Context ContainerRequest request,
                                @Context ExtendedUriInfo uriInfo) throws URISyntaxException, IOException {

        if (!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }
        URI rawUri = ((Request) request).getRawReqeustUri();
        String reqPath = rawUri.getPath();
        int pathFileIndex = reqPath.lastIndexOf("/");
        String reqFileName = reqPath;
        if (pathFileIndex != -1) {
            reqFileName = reqPath.substring(pathFileIndex);
        }
        if (!fileName.endsWith(reqFileName)) {
            if (pathFileIndex != -1) {
                fileName = fileName.substring(0, fileName.lastIndexOf("/")) + reqFileName;
            } else {
                fileName = reqFileName;
            }
        }

        List<String> uris = uriInfo.getMatchedURIs(true);
        String mapName = uris.get(uris.size() - 1);

        URL url = AssetsFeature.lookupAsset(mapName, fileName);

        URLConnection urlConnection = null;
        File fileResource = null;
        String filePath = null;
        boolean found = false;
        InputStream urlInputStream = null;

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
                    urlConnection = url.openConnection();
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
                        urlInputStream = new JarURLInputStream(jarUrlConnection,
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
                builder.entity(fileResource);
            } else {
                builder.entity(
                        urlInputStream != null ?
                                urlInputStream :
                                urlConnection.getInputStream());
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

    private Response notFound() {
        Throwable e = new NotFoundException();
        return Response.fromResponse(mappers.get().findMapping(e).toResponse(e))
                .type(MediaType.TEXT_HTML_TYPE).build();
    }

    private boolean isFileCacheEnabled() {
        return application.getMode().isProd();
    }

    private File getJarFile(final String path) throws MalformedURLException, FileNotFoundException {
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

    static class JarURLInputStream extends java.io.FilterInputStream {

        private final JarURLConnection jarConnection;
        private final JarFile jarFile;

        JarURLInputStream(final JarURLConnection jarConnection,
                          final JarFile jarFile,
                          final InputStream src) {
            super(src);
            this.jarConnection = jarConnection;
            this.jarFile = jarFile;
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
