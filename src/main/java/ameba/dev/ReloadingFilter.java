package ameba.dev;

import ameba.Ameba;
import ameba.compiler.Config;
import ameba.compiler.JavaCompiler;
import ameba.compiler.JavaSource;
import ameba.util.IOUtils;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.util.List;

/**
 * @author icode
 */
@Provider
@PreMatching
@Priority(0)
public class ReloadingFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ReloadingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ReloadingClassLoader classLoader = (ReloadingClassLoader) Ameba.getApp().getClassLoader();

        File pkgRoot = Ameba.getApp().getPackageRoot();

        if (pkgRoot != null) {
            FluentIterable<File> iterable = Files.fileTreeTraverser()
                    .breadthFirstTraversal(pkgRoot);

            File classesRoot = new File(IOUtils.getResource("").getFile());

            List<JavaSource> javaFiles = Lists.newArrayList();

            for (File f : iterable) {
                if (f.isFile() && f.getName().endsWith(".java")) {
                    String path = pkgRoot.toPath().relativize(f.toPath()).toString();
                    String className = path.substring(0, path.length() - 5);
                    File clazz = new File(classesRoot, className + ".class");
                    if (!clazz.exists() || f.lastModified() > clazz.lastModified()) {

                        javaFiles.add(new JavaSource(className.replaceAll(File.separator, "."),
                                pkgRoot, classesRoot));
                    }
                }
            }


            if (javaFiles.size() > 0) {
                final List<ClassDefinition> classes = Lists.newArrayList();

                ReloadingClassLoader cl = new ReloadingClassLoader(classLoader.getParent(), Ameba.getApp());
                JavaCompiler compiler = JavaCompiler.create(cl, new Config());
                try {
                    compiler.generateJavaClass(javaFiles);
                    for (JavaSource source : javaFiles) {
                        source.saveClassFile();
                        classes.add(new ClassDefinition(classLoader.loadClass(source.getClassName()), source.getBytecode()));
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }

                try {
                    classLoader.detectChanges(classes);
                } catch (UnsupportedOperationException e) {
                    classLoader = new ReloadingClassLoader(classLoader.getParent(), Ameba.getApp());
                } catch (ClassNotFoundException e) {
                    logger.warn("在重新加载时未找到类", e);
                } catch (UnmodifiableClassException e) {
                    logger.warn("在重新加载时失败", e);
                }
                //Ameba.getApp().forApplication(Ameba.getApp());
                //Ameba.getApp().reload();
            }

        } else {
            logger.warn("未找到包根目录，无法识别更改！请设置JVM参数，添加 -Dapp.source.root=${yourAppRootDir}");
        }

        Thread.currentThread().setContextClassLoader(classLoader);
    }
}
