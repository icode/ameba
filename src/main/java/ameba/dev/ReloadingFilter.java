package ameba.dev;

import ameba.Ameba;
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
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ReloadingClassLoader classLoader = (ReloadingClassLoader) Ameba.getApp().getClassLoader();

        File pkgRoot = Ameba.getApp().getPackageRoot();

        if (pkgRoot != null) {
            FluentIterable<File> iterable = Files.fileTreeTraverser()
                    .breadthFirstTraversal(pkgRoot);

            File classesRoot = new File(IOUtils.getResource("").getFile());

            List<File> javaFiles = Lists.newArrayList();

            for (File f : iterable) {
                if (f.isFile() && f.getName().endsWith(".java")) {
                    String path = pkgRoot.toPath().relativize(f.toPath()).toString();
                    File clazz = new File(classesRoot, path.substring(0, path.length() - 5) + ".class");
                    if (!clazz.exists() || f.lastModified() > clazz.lastModified()) {
                        javaFiles.add(f);
                    }
                }
            }

            if (javaFiles.size() > 0) {
                final List<ClassDefinition> classes = Lists.newArrayList();

                /*CompilationResult result = JAVAC.compile(javaFiles.toArray(new String[javaFiles.size()]),
                        new FileResourceReader(pkgRoot),
                        new FileResourceStore() {
                            @Override
                            public void write(String pResourceName, byte[] pData) {
                                super.write(pResourceName, pData);
                                try {
                                    Class cl = finalClassLoader.loadClass(pResourceName.substring(0, pResourceName.lastIndexOf(".")).replace(File.separator, "."));
                                    classes.add(new ClassDefinition(cl, pData));
                                } catch (ClassNotFoundException e) {
                                    logger.error("class not found", e);
                                }
                            }
                        },
                        classLoader,
                        JA_C_S);

                if (result.getErrors().length == 0) {
                    try {
                        classLoader.detectChanges(classes);
                    } catch (UnsupportedOperationException e) {

                    } catch (ClassNotFoundException e) {
                        logger.warn("在重新加载时未找到类", e);
                    } catch (UnmodifiableClassException e) {
                        logger.warn("在重新加载时失败", e);
                    }
                } else {
                    for (CompilationProblem p : result.getErrors()) {
                        logger.error(p.toString());
                    }
                }*/
                classLoader = new ReloadingClassLoader(classLoader.getParent(), Ameba.getApp());
                //Ameba.getApp().forApplication(Ameba.getApp());
                Ameba.getApp().reload();

            }

        } else {
            logger.warn("未找到包根目录，无法识别更改！请设置JVM参数，添加 -Dapp.source.root=${yourAppRootDir}");
        }

        Thread.currentThread().setContextClassLoader(classLoader);
    }
}
