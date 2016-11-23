package ameba.event;

import akka.actor.*;
import scala.Option;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * <p>Abstract AsyncListener class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public abstract class AsyncListener<E extends Event> implements Listener<E> {

    UntypedActor actor;

    /**
     * <p>actor.</p>
     *
     * @return a {@link akka.actor.ActorRef} object.
     * @since 0.1.6e
     */
    public ActorRef actor() {
        return actor.self();
    }

    /**
     * <p>aroundPreStart.</p>
     */
    public void aroundPreStart() {
        actor.aroundPreStart();
    }

    /**
     * <p>getSender.</p>
     *
     * @return a {@link akka.actor.ActorRef} object.
     */
    public ActorRef getSender() {
        return actor.getSender();
    }

    /**
     * <p>aroundPostRestart.</p>
     *
     * @param reason a {@link java.lang.Throwable} object.
     */
    public void aroundPostRestart(Throwable reason) {
        actor.aroundPostRestart(reason);
    }

    /**
     * <p>Getter for the field <code>actor</code>.</p>
     *
     * @return a {@link akka.actor.ActorRef} object.
     * @since 0.1.6e
     */
    public ActorRef getActor() {
        return actor.getSelf();
    }

    /**
     * <p>supervisorStrategy.</p>
     *
     * @return a {@link akka.actor.SupervisorStrategy} object.
     */
    public SupervisorStrategy supervisorStrategy() {
        return actor.supervisorStrategy();
    }

    /**
     * <p>getContext.</p>
     *
     * @return a {@link akka.actor.UntypedActorContext} object.
     */
    public UntypedActorContext getContext() {
        return actor.getContext();
    }

    /**
     * <p>postRestart.</p>
     *
     * @param reason a {@link java.lang.Throwable} object.
     * @throws java.lang.Exception if any.
     */
    public void postRestart(Throwable reason) throws Exception {
        actor.postRestart(reason);
    }

    /**
     * <p>preStart.</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void preStart() throws Exception {
        actor.preStart();
    }

    /**
     * <p>sender.</p>
     *
     * @return a {@link akka.actor.ActorRef} object.
     */
    public ActorRef sender() {
        return actor.sender();
    }

    /**
     * <p>onReceive.</p>
     *
     * @param message a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    public void onReceive(Object message) throws Exception {
        actor.onReceive(message);
    }

    /**
     * <p>aroundReceive.</p>
     *
     * @param receive a {@link scala.PartialFunction} object.
     * @param msg     a {@link java.lang.Object} object.
     */
    public void aroundReceive(PartialFunction<Object, BoxedUnit> receive, Object msg) {
        actor.aroundReceive(receive, msg);
    }

    /**
     * <p>context.</p>
     *
     * @return a {@link akka.actor.ActorContext} object.
     */
    public ActorContext context() {
        return actor.context();
    }

    /**
     * <p>unhandled.</p>
     *
     * @param message a {@link java.lang.Object} object.
     */
    public void unhandled(Object message) {
        actor.unhandled(message);
    }

    /**
     * <p>preRestart.</p>
     *
     * @param reason  a {@link java.lang.Throwable} object.
     * @param message a {@link scala.Option} object.
     * @throws java.lang.Exception if any.
     */
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        actor.preRestart(reason, message);
    }

    /**
     * <p>receive.</p>
     *
     * @return a {@link scala.PartialFunction} object.
     */
    public PartialFunction<Object, BoxedUnit> receive() {
        return actor.receive();
    }

    /**
     * <p>aroundPostStop.</p>
     */
    public void aroundPostStop() {
        actor.aroundPostStop();
    }

    /**
     * <p>postStop.</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void postStop() throws Exception {
        actor.postStop();
    }

    /**
     * <p>aroundPreRestart.</p>
     *
     * @param reason  a {@link java.lang.Throwable} object.
     * @param message a {@link scala.Option} object.
     */
    public void aroundPreRestart(Throwable reason, Option<Object> message) {
        actor.aroundPreRestart(reason, message);
    }

    /**
     * <p>onReceive.</p>
     *
     * @param event a E object.
     */
    public abstract void onReceive(E event);
}
