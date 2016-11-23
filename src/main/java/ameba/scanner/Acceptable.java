package ameba.scanner;

/**
 * <p>Acceptable interface.</p>
 *
 * @author icode
 * @version $Id: $Id
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
