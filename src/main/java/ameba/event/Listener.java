package ameba.event;

import akka.actor.*;
import scala.Option;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * @author icode
 */
public abstract class Listener<E extends Event> {

    UntypedActor actor;

    public ActorRef self() {
        return actor.self();
    }

    public void aroundPreStart() {
        actor.aroundPreStart();
    }

    public ActorRef getSender() {
        return actor.getSender();
    }

    public void aroundPostRestart(Throwable reason) {
        actor.aroundPostRestart(reason);
    }

    public ActorRef getSelf() {
        return actor.getSelf();
    }

    public SupervisorStrategy supervisorStrategy() {
        return actor.supervisorStrategy();
    }

    public UntypedActorContext getContext() {
        return actor.getContext();
    }

    public void postRestart(Throwable reason) throws Exception {
        actor.postRestart(reason);
    }

    public void preStart() throws Exception {
        actor.preStart();
    }

    public ActorRef sender() {
        return actor.sender();
    }

    public void onReceive(Object message) throws Exception {
        actor.onReceive(message);
    }

    public void aroundReceive(PartialFunction<Object, BoxedUnit> receive, Object msg) {
        actor.aroundReceive(receive, msg);
    }

    public ActorContext context() {
        return actor.context();
    }

    public void unhandled(Object message) {
        actor.unhandled(message);
    }

    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        actor.preRestart(reason, message);
    }

    public PartialFunction<Object, BoxedUnit> receive() {
        return actor.receive();
    }

    public void aroundPostStop() {
        actor.aroundPostStop();
    }

    public void postStop() throws Exception {
        actor.postStop();
    }

    public void aroundPreRestart(Throwable reason, Option<Object> message) {
        actor.aroundPreRestart(reason, message);
    }

    public abstract void onReceive(E event);
}
