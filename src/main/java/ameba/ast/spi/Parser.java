package ameba.ast.spi;

import java.text.ParseException;

/**
 * @author icode
 */
public interface Parser {
    /**
     * Parse the expression.
     *
     * @param source - expression source
     * @return expression root node
     * @throws ParseException - If the source cannot be parsed
     */
    Node parse(String source) throws ParseException;

    class State {
        public static final State ERROR = newState(0);
        public static final State BREAK = newState(-1);
        public static final State CONTINUE = newState(1);
        public static final State PROCESS = newState(2);
        private int backspace;
        private int type;
        private int action;
        private int progress = 0;

        protected State(int type, int action) {
            this.type = type;
            this.backspace = 0;
            this.action = action;
        }

        protected State(int type, int action, int backspace) {
            this.type = type;
            this.action = action;
            this.backspace = backspace;
        }

        static State newState(int type, int action, int backspace) {
            return new State(type, action, backspace);
        }

        static State newState(int type) {
            return newState(type, -1, 0);
        }

        static State newState(int type, int action) {
            return newState(type, action, 0);
        }

        public static State _error() {
            return ERROR;
        }

        public static State _error(int action) {
            return newState(ERROR.type, action);
        }

        public static State _error(int action, int backspace) {
            return newState(ERROR.type, action, backspace);
        }

        public static State _break() {
            return BREAK;
        }

        public static State _break(int action) {
            return newState(BREAK.type, action);
        }

        public static State _break(int action, int backspace) {
            return newState(BREAK.type, action, backspace);
        }

        public static State _continue() {
            return CONTINUE;
        }

        public static State _continue(int action) {
            return newState(CONTINUE.type, action);
        }

        public static State _continue(int action, int backspace) {
            return newState(CONTINUE.type, action, backspace);
        }

        public static State _process() {
            return PROCESS;
        }

        public static State _process(int action) {
            return newState(PROCESS.type, action);
        }

        public static State _process(int action, int backspace) {
            return newState(PROCESS.type, action, backspace);
        }

        public int getBackspace() {
            return backspace;
        }

        public int getType() {
            return type;
        }

        public int getAction() {
            return action;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            return type == state.type;
        }

        @Override
        public int hashCode() {
            return type;
        }
    }
}
