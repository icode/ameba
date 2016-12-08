package ameba.db.dsl;

/**
 * <p>ExprApplier interface.</p>
 *
 * @author icode
 *
 */
public interface ExprApplier<T> {
    /**
     * <p>apply.</p>
     *
     * @param expr a T object.
     */
    void apply(T expr);
}
