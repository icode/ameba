package ameba.compiler;

import ameba.Ameba;
import ameba.util.ClassUtils;
import ameba.util.UnsafeByteArrayInputStream;
import ameba.util.UnsafeByteArrayOutputStream;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

public class JdkCompiler extends JavaCompiler {
    final Logger logger = LoggerFactory.getLogger(JdkCompiler.class);
    private final boolean isJdk6;
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;
    private javax.tools.JavaCompiler jc;
    private JavaFileManagerImpl fileManager;
    private List<String> options; // 编译参数
    private ClassLoaderImpl _classLoader;

    public JdkCompiler() {
        String version = System.getProperty("java.version");
        if (version != null && version.contains("1.6.")) {
            this.isJdk6 = true;
        } else {
            this.isJdk6 = false;
        }
        diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
    }

    @Override
    protected void initialize() {
        jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            // JDT 支持 ServiceLoader 方式载入。
            ServiceLoader<javax.tools.JavaCompiler> serviceLoader = ServiceLoader.load(javax.tools.JavaCompiler.class);
            Iterator<javax.tools.JavaCompiler> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                jc = iterator.next();
            }
        }
        if (jc == null) {
            throw new IllegalStateException("Can't get system java compiler. Please add jdk tools.jar to your classpath.");
        }

        StandardJavaFileManager standardJavaFileManager = jc.getStandardFileManager(diagnosticCollector, null, null);
        options = Arrays.asList("-encoding", JavaSource.JAVA_FILE_ENCODING, "-g", "-nowarn", "-source", "1.6", "-target", "1.6");

        setDefaultClasspath(standardJavaFileManager);

        _classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoaderImpl>() {
            public ClassLoaderImpl run() {
                return new ClassLoaderImpl(classLoader.getParent());
            }
        });

        fileManager = new JavaFileManagerImpl(standardJavaFileManager, _classLoader);
    }

    private void setDefaultClasspath(StandardJavaFileManager fileManager) {
        ClassLoader contextClassLoader = ClassUtils.getContextClassLoader();
        Collection<URL> classpath = ClassUtils.getClasspathURLs(contextClassLoader);

        if (classpath.size() > 0) {
            try {
                Set<File> files = new LinkedHashSet<File>(classpath.size() + 16);
                for (URL url : classpath) {
                    File file = new File(url.getFile());
                    if (file.exists()) {
                        files.add(file);
                    }
                }
                Iterable<? extends File> list = fileManager.getLocation(StandardLocation.CLASS_PATH);
                for (File file : list) {
                    files.add(file);
                }
                fileManager.setLocation(StandardLocation.CLASS_PATH, files);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        if (Ameba.getApp().getMode().isDev()) {
            // 输出编译用的 classpath
            Iterable<? extends File> files = fileManager.getLocation(StandardLocation.CLASS_PATH);
            for (File file : files) {
                logger.info("Compilation classpath: " + file.getAbsolutePath());
            }
        }
    }

    @Override
    public void generateJavaClass(JavaSource... source) throws IOException {
        generateJavaClass(Arrays.asList(source));
    }

    @Override
    public void generateJavaClass(List<JavaSource> sources) {
        if (sources == null || sources.size() == 0) throw new IllegalArgumentException("java source list is blank");
        List<JavaFileObject> fileList = Lists.newArrayList();

        for (JavaSource js : sources) {
            String name = js.getClassName();
            int i = name.lastIndexOf('.');
            String packageName = i < 0 ? "" : name.substring(0, i);
            String className = i < 0 ? name : name.substring(i + 1);
            JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(js);
            fileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName,
                    className + ClassUtils.JAVA_EXTENSION, javaFileObject);
            fileList.add(javaFileObject);
        }
        // 编译代码
        CompilationTask task = jc.getTask(null, fileManager, diagnosticCollector, options,
                null, fileList);

        Boolean result;
        if (isJdk6) {
            // jdk6 的 compiler 是线程不安全的，需要手动同步
            synchronized (this) {
                result = task.call();
            }
        } else {
            // jdk7+ 的 compiler 是线程安全的
            result = task.call();
        }

        for (JavaSource source : sources)
            try {
                _classLoader.findClass(source.getClassName());
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }

        // 返回编译结果
        if ((result == null) || !result) {

            throw new CompileErrorException("Compilation failed. diagnostics: " + diagnosticCollector.getDiagnostics());
        }
    }

    private static final class JavaFileObjectImpl extends SimpleJavaFileObject {

        private final JavaSource source;
        private UnsafeByteArrayOutputStream bytecode;

        public JavaFileObjectImpl(final String baseName, final CharSequence source) {
            super(ClassUtils.toURI(baseName + ClassUtils.JAVA_EXTENSION), Kind.SOURCE);
            this.source = new JavaSource(baseName, source.toString());
        }

        public JavaFileObjectImpl(JavaSource source) {
            super(ClassUtils.toURI(source.getJavaFile().getPath()), Kind.SOURCE);
            this.source = source;
        }

        public JavaFileObjectImpl(JavaSource source, final String baseName, Kind k) {
            super(ClassUtils.toURI(baseName), k);
            this.source = source;
        }

        JavaFileObjectImpl(final String name, final Kind kind) {
            super(ClassUtils.toURI(name), kind);
            source = null;
        }

        public JavaFileObjectImpl(URI uri, Kind kind) {
            super(uri, kind);
            source = null;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws UnsupportedOperationException {
            if (source == null || source.getSourceCode() == null) {
                throw new UnsupportedOperationException("source == null");
            }
            return source.getSourceCode();
        }

        public JavaSource getJavaSource() {
            return source;
        }

        @Override
        public InputStream openInputStream() {
            return new UnsafeByteArrayInputStream(getByteCode());
        }

        @Override
        public OutputStream openOutputStream() {
            return bytecode = new UnsafeByteArrayOutputStream();
        }

        public byte[] getByteCode() {
            byte[] bytes = bytecode.toByteArray();
            if (source != null)
                source.setBytecode(bytes);
            return bytes;
        }
    }

    private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

        private final ClassLoaderImpl classLoader;

        private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();

        public JavaFileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            FileObject o = fileObjects.get(uri(location, packageName, relativeName));
            if (o != null)
                return o;
            return super.getFileForInput(location, packageName, relativeName);
        }

        public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObjectImpl file) {
            URI uri = uri(location, packageName, relativeName);
            fileObjects.put(uri, file);
        }

        private URI uri(Location location, String packageName, String relativeName) {
            return ClassUtils.toURI(location.getName() + '/' + packageName + '/' + relativeName);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, JavaFileObject.Kind kind, FileObject outputFile)
                throws IOException {
            int index = qualifiedName.lastIndexOf(".");
            String pkg = qualifiedName.substring(0, index);
            String name = qualifiedName.substring(index + 1);
            if (name.contains("$"))
                name = name.substring(0, name.indexOf("$"));
            URI uri = uri(StandardLocation.SOURCE_PATH, pkg, name + ".java");

            JavaFileObject file = new JavaFileObjectImpl(((JavaFileObjectImpl) fileObjects.get(uri)).getJavaSource(),
                    qualifiedName, kind);
            classLoader.add(qualifiedName, file);
            return file;
        }

        @Override
        public ClassLoader getClassLoader(JavaFileManager.Location location) {
            return classLoader;
        }

        @Override
        public String inferBinaryName(Location loc, JavaFileObject file) {
            if (file instanceof JavaFileObjectImpl)
                return file.getName();
            return super.inferBinaryName(loc, file);
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse)
                throws IOException {
            ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();
            if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                for (JavaFileObject file : fileObjects.values()) {
                    if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                        files.add(file);
                    }
                }
                files.addAll(classLoader.files());
            } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
                for (JavaFileObject file : fileObjects.values()) {
                    if (file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)) {
                        files.add(file);
                    }
                }
            }
            Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
            for (JavaFileObject file : result) {
                files.add(file);
            }
            return files;
        }
    }

    private final class ClassLoaderImpl extends ClassLoader {

        private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();

        ClassLoaderImpl(final ClassLoader parentClassLoader) {
            super(parentClassLoader);
        }

        Collection<JavaFileObject> files() {
            return Collections.unmodifiableCollection(classes.values());
        }

        @Override
        protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
            JavaFileObject file = classes.get(qualifiedClassName);
            if (file != null) {
                byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
                return defineClass(qualifiedClassName, bytes, 0, bytes.length);
            }

            try {
                return super.findClass(qualifiedClassName);
            } catch (ClassNotFoundException e) {

                throw e;
            }
        }

        void add(final String qualifiedClassName, final JavaFileObject file) {
            classes.put(qualifiedClassName, file);
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            if (name.endsWith(ClassUtils.CLASS_EXTENSION)) {
                String qualifiedClassName = name.substring(0, name.length() - ClassUtils.CLASS_EXTENSION.length()).replace('/', '.');
                JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
                if (file != null) {
                    return new UnsafeByteArrayInputStream(file.getByteCode());
                }
            }
            return super.getResourceAsStream(name);
        }
    }
}
