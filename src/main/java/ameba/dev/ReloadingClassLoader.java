package ameba.dev;

import ameba.Application;
import ameba.exceptions.UnexpectedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

/**
 * @author icode
 */
public class ReloadingClassLoader extends ClassLoader {

    /**
     * 该保护域适用于所有已加载的类
     */
    public ProtectionDomain protectionDomain;
    public File packageRoot;

    public ReloadingClassLoader(Application app) {
        this(ReloadingClassLoader.class.getClassLoader(), app);
    }

    public ReloadingClassLoader(ClassLoader parent, Application app) {
        super(parent);
        packageRoot = app.getPackageRoot();
        try {
            CodeSource codeSource = new CodeSource(new URL("file:" + app.getSourceRoot().getAbsolutePath()), (Certificate[]) null);
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            protectionDomain = new ProtectionDomain(codeSource, permissions);
        } catch (MalformedURLException e) {
            throw new UnexpectedException("", e);
        }
    }

    public synchronized Class<?> defineClass(String name, byte[] code) {
        if (name == null) {
            throw new IllegalArgumentException("");
        }
        return defineClass(name, code, 0, code.length);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
/*
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // First check if it's an application Class
        Class<?> applicationClass = loadApplicationClass(name);
        if (applicationClass != null) {
            if (resolve) {
                resolveClass(applicationClass);
            }
            return applicationClass;
        }
*/

        // Delegate to the classic classloader
        return super.loadClass(name, resolve);
    }

    /**
     * 查找类的字节码
     */
    protected byte[] getClassDefinition(String name) {
        name = name.replace(".", "/") + ".class";
        InputStream is = getResourceAsStream(name);
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new UnexpectedException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new UnexpectedException(e);
            }
        }
    }


}