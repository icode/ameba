package ameba.scanner;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.glassfish.jersey.internal.OsgiRegistry;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * @author icode
 */
public abstract class ClassInfo {
    private static final Logger logger = LoggerFactory.getLogger(ClassInfo.class);

    private CtClass ctClass;
    private String fileName;
    private Object[] annotations;

    public ClassInfo(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public CtClass getCtClass() {
        if (ctClass == null && fileName.endsWith(".class")) {
            try {
                ctClass = ClassPool.getDefault().makeClass(getFileStream());
            } catch (IOException e) {
                logger.error("make class error", e);
            }
        }
        return ctClass;
    }

    public String getClassName() {
        return getCtClass().getName();
    }

    public Object[] getAnnotations() {
        if (annotations == null) {
            try {
                annotations = getCtClass().getAvailableAnnotations();
            } catch (Exception | Error e) {
                return new Object[0];
            }
        }
        return annotations;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final boolean containsAnnotations(Class<? extends Annotation>... annotationClass) {
        if (ArrayUtils.isEmpty(annotationClass)) {
            return false;
        }

        for (Object anno : getAnnotations()) {
            for (Class cls : annotationClass) {
                if (((Annotation) anno).annotationType().equals(cls)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean accpet(Acceptable<CtClass> acceptable) {
        boolean accept = checkSuperClass(getCtClass(), acceptable);
        if (!accept)
            accept = checkInterface(getCtClass(), acceptable);
        return accept;
    }

    private boolean checkSuperClass(CtClass superClass, Acceptable<CtClass> accept) {
        while (superClass != null && !superClass.getName().equals(Object.class.getName())) {
            if (accept.accept(superClass) || checkInterface(superClass, accept)) {
                return true;
            }
            try {
                superClass = superClass.getSuperclass();
            } catch (NotFoundException e) {
                return false;
            }
        }
        return false;
    }

    private boolean checkInterface(CtClass interfaceClass, Acceptable<CtClass> accept) {
        try {
            for (CtClass ctClass : interfaceClass.getInterfaces()) {
                if (accept.accept(ctClass)
                        || checkInterface(ctClass, accept)) {
                    return true;
                }
            }
        } catch (NotFoundException e) {
            return false;
        }
        return false;
    }

    public boolean isPublic() {
        return javassist.Modifier.isPublic(getCtClass().getModifiers());
    }

    public Class toClass() {
        return getClassForName(getCtClass().getName());
    }

    public Class getClassForName(final String className) {
        try {
            final OsgiRegistry osgiRegistry = ReflectionHelper.getOsgiRegistryInstance();

            if (osgiRegistry != null) {
                return osgiRegistry.classForNameWithException(className);
            } else {
                return AccessController.doPrivileged(ReflectionHelper.classForNameWithExceptionPEA(className));
            }
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(LocalizationMessages.ERROR_SCANNING_CLASS_NOT_FOUND(className), ex);
        } catch (final PrivilegedActionException pae) {
            final Throwable cause = pae.getCause();
            if (cause instanceof ClassNotFoundException) {
                throw new RuntimeException(LocalizationMessages.ERROR_SCANNING_CLASS_NOT_FOUND(className), cause);
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    public boolean startsWithPackage(String... pkgs) {
        for (String st : pkgs) {
            if (!st.endsWith(".")) st += ".";
            String className = getClassName();
            if (className.startsWith(st)) {
                return true;
            }
        }
        return false;
    }

    public abstract InputStream getFileStream();

    public abstract void closeFileStream();
}