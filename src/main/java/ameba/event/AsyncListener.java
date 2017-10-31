package ameba.event;

import co.paralleluniverse.fibers.Suspendable;

/**
 * @author icode
 */
public interface AsyncListener<E extends Event> extends Listener<E> {
    @Suspendable
    void onReceive(E event);
}
