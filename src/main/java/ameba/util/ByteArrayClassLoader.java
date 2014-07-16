package ameba.util;

/**
 * @author icode
 */
public class ByteArrayClassLoader extends ClassLoader {

    public ByteArrayClassLoader() {
        super(ByteArrayClassLoader.class.getClassLoader());
    }

    public ByteArrayClassLoader(ClassLoader parent) {
        super(parent);
    }

    public synchronized Class<?> defineClass(String name, byte[] code) {
        if (name == null) {
            throw new IllegalArgumentException("");
        }
        return defineClass(name, code, 0, code.length);
    }

}