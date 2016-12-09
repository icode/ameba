package ameba.event;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

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
    void onReceive(E event);
}