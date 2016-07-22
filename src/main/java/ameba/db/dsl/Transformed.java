package ameba.db.dsl;

/**
 * @author icode
 */
public abstract class Transformed<R> {
    public static <T> Transformed<T> fail(Transformer transformer) {
        return new Basic<>(transformer, null, false);
    }

    public static <T> Transformed<T> fail() {
        return fail(null);
    }

    public static <T> Transformed<T> succ(Transformer transformer, T result) {
        return new Basic<>(transformer, result, true);
    }

    public abstract R result();

    public abstract boolean success();

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
