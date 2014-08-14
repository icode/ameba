package ameba.enhancers;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.MemberValue;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author icode
 */
public abstract class Enhancer {

    protected ClassPool classPool;

    public Enhancer() {
        this.classPool = newClassPool();
    }

    public static ClassPool newClassPool() {
        ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
        classPool.appendClassPath(new LoaderClassPath(Enhancer.class.getClassLoader()));
        return classPool;
    }

    protected boolean isFinal(CtField ctField) {
        return Modifier.isFinal(ctField.getModifiers());
    }

    /**
     * Test if a class has the provided annotation
     * @param ctClass the javassist class representation
     * @param annotation fully qualified name of the annotation class eg."javax.persistence.Entity"
     * @return true if class has the annotation
     * @throws java.lang.ClassNotFoundException
     */
    protected boolean hasAnnotation(CtClass ctClass, String annotation) throws ClassNotFoundException {
        for (Object object : ctClass.getAvailableAnnotations()) {
            Annotation ann = (Annotation) object;
            if (ann.annotationType().getName().equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if a field has the provided annotation
     * @param ctField the javassist field representation
     * @param annotation fully qualified name of the annotation class eg."javax.persistence.Entity"
     * @return true if field has the annotation
     * @throws java.lang.ClassNotFoundException
     */
    protected boolean hasAnnotation(CtField ctField, String annotation) throws ClassNotFoundException {
        for (Object object : ctField.getAvailableAnnotations()) {
            Annotation ann = (Annotation) object;
            if (ann.annotationType().getName().equals(annotation)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Test if a method has the provided annotation
     * @param ctMethod the javassist method representation
     * @param annotation fully qualified name of the annotation class eg."javax.persistence.Entity"
     * @return true if field has the annotation
     * @throws java.lang.ClassNotFoundException
     */
    protected boolean hasAnnotation(CtMethod ctMethod, String annotation) throws ClassNotFoundException {
        for (Object object : ctMethod.getAvailableAnnotations()) {
            Annotation ann = (Annotation) object;
            if (ann.annotationType().getName().equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new annotation to be dynamically inserted in the byte code.
     */
    protected static void createAnnotation(AnnotationsAttribute attribute, Class<? extends Annotation> annotationType, Map<String, MemberValue> members) {
        javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(annotationType.getName(), attribute.getConstPool());
        for (Map.Entry<String, MemberValue> member : members.entrySet()) {
            annotation.addMemberValue(member.getKey(), member.getValue());
        }
        attribute.addAnnotation(annotation);
    }

    /**
     * Create a new annotation to be dynamically inserted in the byte code.
     */
    protected static void createAnnotation(AnnotationsAttribute attribute, Class<? extends Annotation> annotationType) {
        createAnnotation(attribute, annotationType, new HashMap<String, MemberValue>());
    }

    /**
     * Retrieve all class annotations.
     */
    protected static AnnotationsAttribute getAnnotations(CtClass ctClass) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(ctClass.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
            ctClass.getClassFile().addAttribute(annotationsAttribute);
        }
        return annotationsAttribute;
    }

    /**
     * Retrieve all field annotations.
     */
    protected static AnnotationsAttribute getAnnotations(CtField ctField) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(ctField.getFieldInfo().getConstPool(), AnnotationsAttribute.visibleTag);
            ctField.getFieldInfo().addAttribute(annotationsAttribute);
        }
        return annotationsAttribute;
    }

    protected boolean isProperty(CtField ctField) {
        return !(ctField.getName().equals(ctField.getName().toUpperCase())
                || ctField.getName().substring(0, 1).equals(ctField.getName().substring(0, 1).toUpperCase()))
                && Modifier.isPublic(ctField.getModifiers())
                && !Modifier.isStatic(ctField.getModifiers()) // protected classes will be considered public by this call
                && Modifier.isPublic(ctField.getDeclaringClass().getModifiers());
    }

    /**
     * Retrieve all method annotations.
     */
    protected static AnnotationsAttribute getAnnotations(CtMethod ctMethod) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(ctMethod.getMethodInfo().getConstPool(), AnnotationsAttribute.visibleTag);
            ctMethod.getMethodInfo().addAttribute(annotationsAttribute);
        }
        return annotationsAttribute;
    }


    boolean isAnon(Class clazz) {
        return clazz.getName().contains("$anonfun$") || clazz.getName().contains("$anon$");
    }


}
