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
 * <p>Abstract ClassInfo class.</p>
 *
 * @author icode
 *
 */
public abstract class ClassInfo {
    private static final Logger logger = LoggerFactory.getLogger(ClassInfo.class);

    private CtClass ctClass;
    private String fileName;
    private Object[] annotations;

    /**
     * <p>Constructor for ClassInfo.</p>
     *
     * @param fileName a {@link java.lang.String} object.
     */
    public ClassInfo(String fileName) {
        this.fileName = fileName;
    }

    /**
     * <p>Getter for the field <code>fileName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * <p>Getter for the field <code>ctClass</code>.</p>
     *
     * @return a {@link javassist.CtClass} object.
     */
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

    /**
     * <p>getClassName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return getCtClass().getName();
    }

    /**
     * <p>Getter for the field <code>annotations</code>.</p>
     *
     * @return an array of {@link java.lang.Object} objects.
     */
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

    /**
     * <p>containsAnnotations.</p>
     *
     * @param annotationClass a {@link java.lang.Class} object.
     * @return a boolean.
     */
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

    /**
     * <p>accpet.</p>
     *
     * @param acceptable a {@link ameba.scanner.Acceptable} object.
     * @return a boolean.
     */
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

    /**
     * <p>isPublic.</p>
     *
     * @return a boolean.
     */
    public boolean isPublic() {
        return javassist.Modifier.isPublic(getCtClass().getModifiers());
    }

    /**
     * <p>toClass.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class toClass() {
        return getClassForName(getCtClass().getName());
    }

    /**
     * <p>getClassForName.</p>
     *
     * @param className a {@link java.lang.String} object.
     * @return a {@link java.lang.Class} object.
     */
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

    /**
     * <p>startsWithPackage.</p>
     *
     * @param pkgs a {@link java.lang.String} object.
     * @return a boolean.
     */
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

    /**
     * <p>getFileStream.</p>
     *
     * @return a {@link java.io.InputStream} object.
     */
    public abstract InputStream getFileStream();

    /**
     * <p>closeFileStream.</p>
     */
    public abstract void closeFileStream();
}
