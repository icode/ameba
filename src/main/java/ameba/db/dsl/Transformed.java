package ameba.db.dsl;

/**
 * <p>Abstract Transformed class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public abstract class Transformed<R> {
    /**
     * <p>fail.</p>
     *
     * @param transformer a {@link ameba.db.dsl.Transformer} object.
     * @param <T>         a T object.
     * @return a {@link ameba.db.dsl.Transformed} object.
     */
    public static <T> Transformed<T> fail(Transformer transformer) {
        return new Basic<>(transformer, null, false);
    }

    /**
     * <p>fail.</p>
     *
     * @param <T> a T object.
     * @return a {@link ameba.db.dsl.Transformed} object.
     */
    public static <T> Transformed<T> fail() {
        return fail(null);
    }

    /**
     * <p>succ.</p>
     *
     * @param transformer a {@link ameba.db.dsl.Transformer} object.
     * @param result a T object.
     * @param <T> a T object.
     * @return a {@link ameba.db.dsl.Transformed} object.
     */
    public static <T> Transformed<T> succ(Transformer transformer, T result) {
        return new Basic<>(transformer, result, true);
    }

    /**
     * <p>result.</p>
     *
     * @return a R object.
     */
    public abstract R result();

    /**
     * <p>success.</p>
     *
     * @return a boolean.
     */
    public abstract boolean success();

    /**
     * <p>transformer.</p>
     *
     * @return a {@link ameba.db.dsl.Transformer} object.
     */
    public abstract Transformer transformer();

    public static class Basic<T> extends Transformed<T> {

        private Transformer transformer;
        private T result;
        private boolean success;

        public Basic(Transformer transformer, T result, boolean success) {
            this.transformer = transformer;
            this.result = result;
            this.success = success;
        }

        @Override
        public T result() {
            return result;
        }

        @Override
        public boolean success() {
            return success;
        }

        @Override
        public Transformer transformer() {
            return transformer;
        }
    }
}
