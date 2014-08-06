package ameba.event;

import ameba.util.Times;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author icode
 */
public class TimesTest {

    @Test
    public void testParseDuration() {
        Assert.assertEquals(Times.parseDuration("4s"), 4);
        Assert.assertEquals(Times.parseDuration("4mn"), 4 * 60);
        Assert.assertEquals(Times.parseDuration("4min"), 4 * 60);
        Assert.assertEquals(Times.parseDuration("4h"), 4 * 60 * 60);
        Assert.assertEquals(Times.parseDuration("4d"), 4 * 60 * 60 * 24);
    }

}
