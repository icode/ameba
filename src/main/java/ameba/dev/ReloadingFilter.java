package ameba.dev;

import ameba.Ameba;
import ameba.Application;
import ameba.compiler.Config;
import ameba.compiler.JavaCompiler;
import ameba.compiler.JavaSource;
import ameba.util.IOUtils;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.File;
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

//    @Context
//    private ExtendedResourceContext resourceContext;

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
                    compiler.compile(javaFiles);
                    for (JavaSource source : javaFiles) {
                        classes.add(new ClassDefinition(classLoader.loadClass(source.getClassName()), source.getBytecode()));
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                boolean forceDefine = false;

                try {
                    classLoader.detectChanges(classes);
                } catch (UnsupportedOperationException e) {
                    forceDefine = true;
                } catch (ClassNotFoundException e) {
                    logger.warn("在重新加载时未找到类", e);
                } catch (UnmodifiableClassException e) {
                    logger.warn("在重新加载时失败", e);
                }

                boolean result = reload(classes, classLoader, cl, forceDefine);
                if (result)
                    requestContext.abortWith(Response.temporaryRedirect(requestContext.getUriInfo().getRequestUri()).build());
            }

        } else {
            logger.warn("未找到包根目录，无法识别更改！请设置JVM参数，添加 -Dapp.source.root=${yourAppRootDir}");
        }

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * 重新加载容器
     * 1.当出现一个没有的class，新编译的
     * 2.强制加载，当类/方法签名改变时
     */
    boolean reload(List<ClassDefinition> reloadClasses, ReloadingClassLoader oClassLoader, ReloadingClassLoader nClassLoader, boolean force) {
        Application app = Ameba.getApp();

        boolean needReload = force;

        if (!needReload) {
            //检查是否有新的class
            for (ClassDefinition cf : reloadClasses) {
                Class cl = cf.getDefinitionClass();
                if (!oClassLoader.hasClass(cl.getName())) {
                    needReload = true;
                    break;
                }
            }
        }

        if (needReload) {
            //实例化一个没有被锁住的并且从原有app获得全部属性
            ResourceConfig resourceConfig = new ResourceConfig(app);
            resourceConfig.setClassLoader(nClassLoader);
            Thread.currentThread().setContextClassLoader(nClassLoader);

            for (ClassDefinition cf : reloadClasses) {
                try {
                    resourceConfig.register(nClassLoader.loadClass(cf.getDefinitionClass().getName()));
                } catch (ClassNotFoundException e) {
                    logger.error("重新获取class失败", e);
                }
            }
            //切换已有class，更换class loader
            for (Class clazz : app.getClasses()) {
                try {
                    clazz = nClassLoader.loadClass(clazz.getName());
                    if (!resourceConfig.isRegistered(clazz))
                        resourceConfig.register(clazz);
                } catch (ClassNotFoundException e) {
                    logger.error("重新获取class失败", e);
                }
            }

            app.reload(resourceConfig);
            return true;
        }
        return false;
    }
}
