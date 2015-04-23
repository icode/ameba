package ameba.event;

import akka.actor.ActorRef;
import ameba.exception.AmebaException;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>Abstract EventBus class.</p>
 *
 * @author icode
 */
public abstract class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final SetMultimap<Class<?>, Listener> listeners = LinkedHashMultimap.create();
    private final ReadWriteLock subscribersByTypeLock = new ReentrantReadWriteLock();

    private EventBus() {
    }

    /**
     * <p>createMix.</p>
     *
     * @return a {@link ameba.event.EventBus} object.
     */
    public static EventBus createMix() {
        return new Mixed();
    }

    /**
     * <p>create.</p>
     *
     * @return a {@link ameba.event.EventBus} object.
     */
    public static EventBus create() {
        return new EventBus() {
        };
    }

    /**
     * <p>subscribe.</p>
     *
     * @param event    a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E>      a E object.
     */
    public <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
        subscribersByTypeLock.writeLock().lock();
        try {
            listeners.put(event, listener);
        } finally {
            subscribersByTypeLock.writeLock().unlock();
        }
    }

    private List<Method> getAnnotatedMethods(Class<?> clazz) {
//        Set<? extends Class<?>> supers = TypeToken.of(clazz).getTypes().rawTypes();
        List<Method> identifiers = Lists.newArrayList();
//        for (Class<?> superClazz : supers) {
        for (Method superClazzMethod : clazz.getDeclaredMethods()) {
            if (superClazzMethod.isAnnotationPresent(Subscribe.class)
                    && !superClazzMethod.isBridge()) {
                identifiers.add(superClazzMethod);
            }
        }
//        }
        return identifiers;
    }

    /**
     * subscribe event by {@link ameba.event.Subscribe} annotation
     * <p/>
     * <pre>{@code
     * <p/>
     * class SubEevent {
     * <p/>
     *     public SubEevent(){
     *         EventBus.subscribe(this);
     *     }
     * <p/>
     *
     *     @@Subscribe({ Container.ReloadEvent.class })
     *     private void doSome(MyEvent e){
     *         ....
     *     }
     * }
     * <p/>
     * class SubEevent2 {
     * <p/>
     *     @@Subscribe({ Container.ReloadEvent.class })
     *     private void doSome(){
     *         ....
     *     }
     * <p/>
     *     @@Subscribe({ Container.ReloadEvent.class })
     *     private void doSome(MyEvent e){
     *         ....
     *     }
     * }
     * <p/>
     * class SubEevent2 {
     * <p/>
     *     @@Subscribe({ Container.ReloadEvent.class })
     *     private void doSome(){
     *         ....
     *     }
     * <p/>
     * @param obj class or instance
     * @since 0.1.6e
     */
    @SuppressWarnings("unchecked")
    public void subscribe(Object obj) {
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
        List<Method> methods = getAnnotatedMethods(objClass);
        for (final Method method : methods) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                Class[] argsClass = method.getParameterTypes();
                final Class[] needEvent = new Class[argsClass.length];
                Class<? extends Event>[] events = subscribe.value();

                for (int i = 0; i < argsClass.length; i++) {
                    if (Event.class.isAssignableFrom(argsClass[i])) {
                        needEvent[i] = argsClass[i];
                    }
                }

                if (subscribe.value().length == 0) {
                    events = needEvent;
                }

                method.setAccessible(true);
                for (final Class<? extends Event> event : events) {
                    if (event == null) continue;
                    Listener listener = new Listener() {
                        @Override
                        public void onReceive(Event ev) {
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
                        }
                    };
                    subscribe(event, listener, subscribe);
                }
            }
        }
    }

    /**
     * <p>subscribe.</p>
     *
     * @param event a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param subscribe a {@link ameba.event.Subscribe} object.
     * @param <E> a E object.
     * @since 0.1.6e
     */
    protected <E extends Event> void subscribe(Class<E> event, final Listener<E> listener, Subscribe subscribe) {
        subscribe(event, listener);
    }

    /**
     * <p>unsubscribe.</p>
     *
     * @param event a {@link java.lang.Class} object.
     * @param listener a {@link ameba.event.Listener} object.
     * @param <E> a E object.
     */
    public <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
        subscribersByTypeLock.writeLock().lock();
        try {
            listeners.remove(event, listener);
        } finally {
            subscribersByTypeLock.writeLock().unlock();
        }
    }

    /**
     * <p>unsubscribe.</p>
     *
     * @param event a {@link java.lang.Class} object.
     * @param <E> a E object.
     * @since 0.1.6e
     */
    public <E extends Event> void unsubscribe(Class<E> event) {
        subscribersByTypeLock.writeLock().lock();
        try {
            listeners.removeAll(event);
        } finally {
            subscribersByTypeLock.writeLock().unlock();
        }
    }

    /**
     * <p>publish.</p>
     *
     * @param event a {@link ameba.event.Event} object.
     */
    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        Set<Listener> listenerSet = listeners.get(event.getClass());
        for (Listener listener : listenerSet) {
            try {
                listener.onReceive(event);
            } catch (Exception e) {
                logger.error(event.getClass().getName() + " event handler has a error", e);
            }
        }
    }

    public static class Mixed extends EventBus {

        private final AsyncEventBus<Event, ActorRef> asyncEventBus;

        Mixed() {
            asyncEventBus = AsyncEventBus.create();
        }

        public <E extends Event> void subscribe(Class<E> event, final Listener<E> listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.subscribe(event, (AsyncListener) listener);
            } else {
                super.subscribe(event, listener);
            }
        }

        @SuppressWarnings("unchecked")
        protected <E extends Event> void subscribe(Class<E> event, final Listener<E> listener, Subscribe subscribe) {
            if (subscribe.async()) {
                asyncEventBus.subscribe(event, new AsyncListener<E>() {
                    @Override
                    public void onReceive(E event) {
                        listener.onReceive(event);
                    }
                });
            } else {
                super.subscribe(event, listener);
            }
        }

        public <E extends Event> void unsubscribe(Class<E> event, final Listener<E> listener) {
            if (listener instanceof AsyncListener) {
                asyncEventBus.unsubscribe(event, (AsyncListener) listener);
            } else {
                super.unsubscribe(event, listener);
            }
        }

        @Override
        public <E extends Event> void unsubscribe(Class<E> event) {
            super.unsubscribe(event);
            asyncEventBus.unsubscribe(event);
        }

        public void publish(Event event) {
            if (event == null) return;
            asyncEventBus.publish(event);
            super.publish(event);
        }
    }
}
