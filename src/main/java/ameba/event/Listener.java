package ameba.event;

/**
 * @author icode
 */
public interface Listener<E extends Event> {
    public void onReceive(E event);
}
