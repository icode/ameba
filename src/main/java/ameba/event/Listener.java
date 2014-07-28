package ameba.event;

import com.google.common.eventbus.Subscribe;

/**
 * @author icode
 */
public interface Listener<E extends Event> {
    @Subscribe
    public void onReceive(E event);
}
