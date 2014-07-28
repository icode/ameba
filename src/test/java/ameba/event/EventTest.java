package ameba.event;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author icode
 */
public class EventTest {

    private static final Logger logger = LoggerFactory.getLogger(EventTest.class);

    @Test
    public void publish() {
        final AtomicInteger times = new AtomicInteger();
        for (int i = 0; i < 1000; i++) {
            SystemEventBus.subscribe(TestEvent.class, new AsyncListener<TestEvent>() {
                @Override
                public void onReceive(TestEvent event) {
                    logger.info("receive message : {}", event.message);
                    logger.info("times: {}", times.getAndIncrement());
                }
            });
        }

        logger.info("publish message ..");
        for (int i = 0; i < 50; i++) {
            SystemEventBus.publish(new TestEvent("message: " + i));
        }

        try {
            synchronized (this) {
                wait(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class TestEvent extends Event {
        public String message;

        public TestEvent(String message) {
            this.message = message;
        }
    }
}
