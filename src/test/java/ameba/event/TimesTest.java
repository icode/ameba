package ameba.event;

import ameba.util.Times;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author icode
 */
public class TimesTest {

    @Test
    public void testParseToSeconds() {
        Assert.assertEquals(Times.parseToSeconds("4s"), 4);
        Assert.assertEquals(Times.parseToSeconds("4min"), 4 * 60);
        Assert.assertEquals(Times.parseToSeconds("4min 4s"), 4 * 60 + 4);
        Assert.assertEquals(Times.parseToSeconds("4h"), 4 * 60 * 60);
        Assert.assertEquals(Times.parseToSeconds("4h 4min"), (4 * 60 * 60) + (4 * 60));
        Assert.assertEquals(Times.parseToSeconds("4h 4min 4s"), (4 * 60 * 60) + (4 * 60) + 4);
        Assert.assertEquals(Times.parseToSeconds("4d"), 4 * 60 * 60 * 24);
        Assert.assertEquals(Times.parseToSeconds("4d 4h"), (4 * 60 * 60 * 24) + (4 * 60 * 60));
        Assert.assertEquals(Times.parseToSeconds("4d 4h 4min"), (4 * 60 * 60 * 24) + (4 * 60 * 60) + (4 * 60));
        Assert.assertEquals(Times.parseToSeconds("4d 4h 4min 4s"), (4 * 60 * 60 * 24) + (4 * 60 * 60) + (4 * 60) + 4);
    }

}
