package ameba.scanner;

/**
 * @author icode
 */
public interface Acceptable<A> {
    /**
     * 如果是需要的class则返回true
     *
     * @param a info
     * @return 是否需要该class
     */
    boolean accept(A a);
}
