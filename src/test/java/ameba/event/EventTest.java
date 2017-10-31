package ameba.event;

import ameba.lib.Fibers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author icode
 */
public class EventTest {

    private static final Logger logger = LoggerFactory.getLogger(EventTest.class);

    @Test
    public void publish() {
        EventBus eventBus = EventBus.createMix();

        eventBus.subscribe(new AnnotationSub());
        eventBus.subscribe(new ChildSub());

        eventBus.subscribe(AnnotationSub.class);

        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            eventBus.subscribe(TestEvent.class, (AsyncListener<TestEvent>) event -> {
                try {
                    Fibers.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("error", e);
                }
                logger.info("async receive message {} : {}", finalI, event.message);
            });

            eventBus.subscribe(TestEvent1.class, (AsyncListener<TestEvent1>) event -> logger.info("TestEvent1 async receive message {} : {}", finalI, event.message));
        }
        for (int i = 0; i < 5; i++) {
            final int finalI = i;
            eventBus.subscribe(TestEvent.class, (Listener<TestEvent>) event -> logger.info("receive message {} : {}", finalI, event.message));

            eventBus.subscribe(TestEvent1.class, (Listener<TestEvent1>) event -> logger.info("TestEvent1 receive message {} : {}", finalI, event.message));
        }

        logger.info("publish message ..");
        for (int i = 0; i < 10; i++) {
            eventBus.publish(new TestEvent("message: " + i));
            eventBus.publish(new TestEvent1("message: " + i));
        }

        try {
            synchronized (this) {
                wait(1800);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("remove all event and publish message ..");
        eventBus.unsubscribe(TestEvent.class);
        eventBus.unsubscribe(TestEvent1.class);
        for (int i = 0; i < 10; i++) {
            eventBus.publish(new TestEvent("message: " + i));
            eventBus.publish(new TestEvent1("message: " + i));
        }
    }

    public static class AnnotationSub {
        @Subscribe(TestEvent.class)
        private void doSomething(TestEvent e) {
            logger.info("AnnotationSub receive message : {}", e.message);
        }

        @Subscribe(value = TestEvent.class, async = true)
        private void doAsyncSomething(TestEvent e) {
            logger.info("Async AnnotationSub receive message : {}", e.message);
        }

        @Subscribe
        private void doSomething2(TestEvent e) {
            logger.info("CoC AnnotationSub receive message : {}", e.message);
        }

        @Subscribe(async = true)
        private void doAsyncSomething2(TestEvent e) {
            logger.info("Async CoC AnnotationSub receive message : {}", e.message);
        }

        @Subscribe
        public void doSomething3(TestEvent e, TestEvent1 e1) {
            if (e != null)
                logger.info("doSomething3 CoC AnnotationSub receive TestEvent message : {}", e.message);
            if (e1 != null)
                logger.info("doSomething3 CoC AnnotationSub receive TestEvent1 message : {}", e1.message);
        }
    }

    public static class ChildSub extends AnnotationSub {
        @Subscribe
        public void doSomething3(TestEvent e, TestEvent1 e1) {
            if (e != null)
                logger.info("doSomething3 CoC AnnotationSub receive TestEvent message : {}", e.message);
            if (e1 != null)
                logger.info("doSomething3 CoC AnnotationSub receive TestEvent1 message : {}", e1.message);
        }

    }

    public static class TestEvent implements Event {
        public String message;

        public TestEvent(String message) {
            this.message = message;
        }
    }

    public static class TestEvent1 implements Event {
        public String message;

        public TestEvent1(String message) {
            this.message = message;
        }
    }
}
