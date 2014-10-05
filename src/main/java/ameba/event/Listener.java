package ameba.event;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.google.common.eventbus.Subscribe;

/**
 * @author icode
 */
@JsonIgnoreType
public interface Listener<E extends Event> {
    @Subscribe
    public void onReceive(E event);
}
