package ameba.event;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.google.common.eventbus.Subscribe;

/**
 * <p>Listener interface.</p>
 *
 * @author icode
 */
@JsonIgnoreType
public interface Listener<E extends Event> {
    /**
     * <p>onReceive.</p>
     *
     * @param event a E object.
     */
    @Subscribe
    public void onReceive(E event);
}
