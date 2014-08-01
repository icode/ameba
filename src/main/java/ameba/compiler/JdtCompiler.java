package ameba.compiler;

import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import com.google.common.collect.Maps;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class JdtCompiler extends JavaCompiler {
    @Override
    public void generateJavaClass(JavaSource... source) {
        generateJavaClass(Arrays.asList(source));
    }

    @Override
    public void generateJavaClass(List<JavaSource> sources) {
        if (sources == null || sources.size() == 0) throw new IllegalArgumentException("java source list is blank");
        IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
        CompilerOptions options = getCompilerOptions();
        CompilerRequestor requestor = new CompilerRequestor(sources);
        IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());

        ICompilationUnit[] compilationUnits = new ICompilationUnit[sources.size()];
        for (JavaSource source : sources)
            compilationUnits[compilationUnits.length] = new CompilationUnit(source);

        Compiler compiler = new Compiler(new NameEnvironment(sources), policy, options, requestor, problemFactory);

        compiler.compile(compilationUnits);

        if (requestor.hasErrors()) {
            throw new CompileErrorException("编译出错!");
        }
    }

    private CompilerOptions getCompilerOptions() {
        Map<String, String> settings = new HashMap<String, String>();
        settings.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
        settings.put(CompilerOptions.OPTION_Encoding, JavaSource.JAVA_FILE_ENCODING);
        settings.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
        settings.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
        settings.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);

        CompilerOptions options = new CompilerOptions(settings);
        options.parseLiteralExpressionsAsConstants = true;
        return options;
    }

    static class CompilationUnit implements ICompilationUnit {
        final JavaSource source;

        public CompilationUnit(JavaSource source) {
            this.source = source;
        }

        @Override
        public char[] getFileName() {
            return source.getJavaFile().getAbsolutePath().toCharArray();
        }

        @Override
        public char[] getContents() {
            return source.getSourceCode().toCharArray();
        }

        @Override
        public char[] getMainTypeName() {
            String qualifiedClassName = source.getClassName();
            int dot = qualifiedClassName.lastIndexOf('.');
            if (dot > 0) {
                return qualifiedClassName.substring(dot + 1).toCharArray();
            }
            return qualifiedClassName.toCharArray();
        }

        @Override
        public char[][] getPackageName() {
            StringTokenizer tokenizer = new StringTokenizer(source.getClassName(), ".");
            char[][] result = new char[tokenizer.countTokens() - 1][];
            for (int i = 0; i < result.length; i++) {
                String tok = tokenizer.nextToken();
                result[i] = tok.toCharArray();
            }
            return result;
        }

        @Override
        public boolean ignoreOptionalProblems() {
            return false;
        }
    }

    static class NameEnvironment implements INameEnvironment {
        static final Logger log = LoggerFactory.getLogger(NameEnvironment.class);
        final Map<String, Boolean> cache = new HashMap<String, Boolean>();
        final ClassLoader classLoader;
        final Map<String, JavaSource> map;

        public NameEnvironment(List<JavaSource> sources) {
            map = Maps.newHashMap();
            for (JavaSource source : sources) {
                map.put(source.getClassName(), source);
            }
            this.classLoader = ClassUtils.getContextClassLoader();
        }

        @Override
        public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < compoundTypeName.length; i++) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append(compoundTypeName[i]);
            }
            return findType(sb.toString());
        }

        @Override
        public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
            StringBuilder sb = new StringBuilder();
            for (char[] aPackageName : packageName) {
                sb.append(aPackageName).append('.');
            }
            sb.append(typeName);
            return findType(sb.toString());
        }

        private NameEnvironmentAnswer findType(String className) {
            if (map.containsKey(className)) {
                return new NameEnvironmentAnswer(new CompilationUnit(map.get(className)), null);
            }

            InputStream is = null;
            try {
                String resourceName = className.replace('.', '/') + JavaSource.CLASS_EXTENSION;
                is = classLoader.getResourceAsStream(resourceName);
                if (is != null) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    char[] fileName = resourceName.toCharArray();
                    ClassFileReader classFileReader = new ClassFileReader(bytes, fileName, true);
                    return new NameEnvironmentAnswer(classFileReader, null);
                }
            } catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
                log.error("Compilation error", e);
            } finally {
                IOUtils.closeQuietly(is);
            }
            return null;
        }

        @Override
        public boolean isPackage(char[][] parentPackageName, char[] packageName) {
            StringBuilder sb = new StringBuilder();
            if (parentPackageName != null) {
                for (char[] p : parentPackageName) {
                    sb.append(p).append('.');
                }
            }
            sb.append(packageName);
            String name = sb.toString();

            Boolean found = cache.get(name);
            if (found != null) {
                return found;
            }

            boolean isPackage = !isJavaClass(name);
            cache.put(name, isPackage);
            return isPackage;
        }

        private boolean isJavaClass(String name) {
            if (map.containsKey(name)) {
                return true;
            }

            String resourceName = name.replace('.', '/') + JavaSource.CLASS_EXTENSION;
            URL url = classLoader.getResource(resourceName);
            return url != null;
        }

        @Override
        public void cleanup() {
        }
    }

    static class CompilerRequestor implements ICompilerRequestor {
        IProblem[] errors;
        List<JavaSource> sources;
        Map<String, JavaSource> map;

        CompilerRequestor(List<JavaSource> sources) {
            this.sources = sources;
            map = Maps.newHashMap();

            for (JavaSource js : sources) {
                map.put(js.getClassName(), js);
            }
        }

        @Override
        public void acceptResult(CompilationResult result) {
            if (result.hasErrors()) {
                errors = result.getErrors();
            } else {
                ClassFile[] classFiles = result.getClassFiles();
                for (ClassFile classFile : classFiles) {
                    char[][] compoundName =
                            classFile.getCompoundName();
                    String className = "";
                    String sep = "";
                    for (char[] aCompoundName : compoundName) {
                        className += sep;
                        className += new String(aCompoundName);
                        sep = ".";
                    }
                    byte[] bytes = classFile.getBytes();
                    map.get(className).setBytecode(bytes);
                }
            }
        }

        public boolean hasErrors() {
            return errors != null;
        }

        public IProblem[] getErrors() {
            return errors;
        }

    }

}
