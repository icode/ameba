package ameba.db.dsl;

/**
 * @author icode
 */
public interface ApplyExpr<T> {
    void apply(T expr);
}
