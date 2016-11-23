package ameba.db.dsl;

/**
 * <p>ExprApplier interface.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public interface ExprApplier<T> {
    /**
     * <p>apply.</p>
     *
     * @param expr a T object.
     */
    void apply(T expr);
}
