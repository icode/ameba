package ameba.db.dsl;

/**
 * @author icode
 */
public interface ExprApplier<T> {
    void apply(T expr);
}
