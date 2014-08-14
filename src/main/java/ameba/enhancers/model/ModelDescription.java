package ameba.enhancers.model;

/**
 * @author icode
 */
public class ModelDescription {
    String className;
    String classFile;
    String classSimpleName;
    byte[] classBytecode;
    Class clazz;

    public byte[] getClassByteCode() {
        return classBytecode;
    }

    public String getClassName() {
        return className;
    }

    public String getClassFile() {
        return classFile;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

}
