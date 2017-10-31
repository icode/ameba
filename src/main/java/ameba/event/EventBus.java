package ameba.event;

import ameba.exception.AmebaException;

import java.lang.reflect.Method;

/**
 * <p>Abstract EventBus class.</p>
 *
 * @author icode
 */
public interface EventBus<Event extends ameba.event.Event> {


    /**
     * <p>createMix.</p>
     *
     * @param <Event> event
     * @return a {@link ameba.event.EventBus} object.
     */
    static <Event extends ameba.event.Event> EventBus<Event> createMix() {
        return new Mixed<>();
    }

    /**
     * <p>create.</p>
     *
     * @return a {@link ameba.event.EventBus} object.
     */
    static EventBus create() {
        return new BasicEventBus();
    }

    /**
     * <p>subscribe.</p>
     *
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     */
    <E extends Event> void subscribe(Class<E> event, Listener<E> listener);

    /**
     * subscribe event by {@link ameba.event.Subscribe} annotation
     * <pre>
     * class SubEevent {
     *     public SubEevent(){
     *         EventBus.subscribe(this);
     *     }
     *     {@literal @Subscribe({ Container.ReloadEvent.class })}
     *     private void doSome(MyEvent e){
     *         ....
     *     }
     * }
     * class SubEevent2 {
     *     {@literal @Subscribe({ Container.ReloadEvent.class })}
     *     private void doSome(){
     *         ....
     *     }
     *     {@literal @Subscribe({ Container.ReloadEvent.class })}
     *     private void doSome(ReloadEvent e){
     *         ....
     *     }
     * }
     * class SubEevent2 {
     *     {@literal @Subscribe({ Container.ReloadEvent.class })}
     *     private void doSome(){
     *         ....
     *     }
     * }
     * </pre>
     *
     * @param obj class or instance
     */
    @SuppressWarnings("unchecked")
    default void subscribe(Object obj) {
        if (obj == null) {
            return;
        }
        Class objClass;
        if (obj instanceof Class) {
            objClass = (Class) obj;
            try {
                obj = objClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AmebaException("subscribe event error, "
                        + objClass.getName() + " must be have a public void arguments constructor", e);
            }
        } else {
            objClass = obj.getClass();
        }
        final Object finalObj = obj;
        for (Method method : objClass.getDeclaredMethods()) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null && !method.isBridge()) {
                Class[] argsClass = method.getParameterTypes();
                final Class<Event>[] needEvent = new Class[argsClass.length];
                Class<Event>[] events = (Class<Event>[]) subscribe.value();

                for (int i = 0; i < argsClass.length; i++) {
                    if (ameba.event.Event.class.isAssignableFrom(argsClass[i])) {
                        needEvent[i] = argsClass[i];
                    }
                }

                if (subscribe.value().length == 0) {
                    events = needEvent;
                }

                method.setAccessible(true);
                for (final Class<Event> event : events) {
                    if (event == null) continue;
                    Listener<Event> listener = ev -> {
                        Object[] args = new Object[needEvent.length];
                        try {
                            for (int i = 0; i < needEvent.length; i++) {
                                if (needEvent[i] != null && needEvent[i].isAssignableFrom(event)) {
                                    args[i] = ev;
                                }
                            }
                            method.invoke(finalObj, args);
                        } catch (IllegalAccessException e) {
                            throw new AmebaException("handle event error, " + method.getName()
                                    + " method must be not have arguments or extends from Event argument", e);
                        } catch (Exception e) {
                            throw new AmebaException("handle " + method.getName() + " event error. ", e);
                        }
                    };
                    subscribe(event, subscribe.async() ? (AsyncListener<Event>) listener::onReceive : listener);
                }
            }
        }
    }

    /**
     * <p>unsubscribe.</p>
     *
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     */
    <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener);

    /**
     * <p>unsubscribe.</p>
     *
     * @param event a {@link java.lang.Class} object.
     */
    <E extends Event> void unsubscribe(Class<E> event);

    /**
     * <p>publish.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    <E extends Event> void publish(E event);

    class Mixed<Event extends ameba.event.Event> extends BasicEventBus<Event> {

        private final AsyncEventBus<Event> asyncEventBus;

        Mixed() {
            asyncEventBus = AsyncEventBus.create();
        }

        @Override
        public <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.subscribe(event, (AsyncListener<E>) listener);
            } else {
                super.subscribe(event, listener);
            }
        }

        @Override
        public <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.unsubscribe(event, listener);
            } else {
                super.unsubscribe(event, listener);
            }
        }

        @Override
        public <E extends Event> void unsubscribe(Class<E> event) {
            asyncEventBus.unsubscribe(event);
            super.unsubscribe(event);
        }

        @Override
        public <E extends Event> void publish(E event) {
            if (event == null) return;
            asyncEventBus.publish(event);
            super.publish(event);
        }
    }
}
