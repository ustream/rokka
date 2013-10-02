package tv.ustream.rokka;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class RokkaTest
{
    private static final int QUEUE_SIZE = 10;
    private static final int TEST_EVENT_SIZE = 20;
    private static final int TIME_OUT_IN_MS = 10;
    private RokkaBaseConsumer<RokkaTestEvent> rokkaConsumer;
    private RokkaProducer<RokkaTestEvent> rokkaProducer;
    private RokkaTestEvent[] events;

    @Before
    public final void setUp() throws Exception
    {
        rokkaProducer = new RokkaProducer<>(QUEUE_SIZE);

        rokkaConsumer = new RokkaBaseConsumer();
        rokkaConsumer.addProducer(rokkaProducer);

        generateTestEvents(TEST_EVENT_SIZE);
    }

    @After
    public final void tearDown() throws Exception
    {
        if (events != null)
        {
            Arrays.fill(events, null);
        }
    }

    private void generateTestEvents(final int number)
    {
        if (number < 1)
        {
            throw new IndexOutOfBoundsException();
        }

        events = new RokkaTestEvent[number];
        for (int i = 0; i < number; i++)
        {
            events[i] = new RokkaTestEvent(i);
        }
    }

    @Test
    public final void getMaxQueueSizeShouldBeCheckValidQueueSize()
    {
        int size = rokkaProducer.getMaxQueueSize();
        Assert.assertEquals(QUEUE_SIZE, size);
    }

    @Test
    public final void addAndRemoveShouldBeCheckValidEventResult() throws Exception
    {
        boolean result;

        for (int i = 0; i < QUEUE_SIZE; i++)
        {
            result = rokkaProducer.add(events[i], TIME_OUT_IN_MS);
            Assert.assertTrue(result);

            RokkaTestEvent removeObj = rokkaConsumer.remove();
            Assert.assertEquals(events[i], removeObj);
        }
    }

    @Test
    public final void addShouldBeCheckTimeout() throws Exception
    {
        boolean result;
        long startTime, endTime;

        for (int i = 0; i < QUEUE_SIZE; i++)
        {
            rokkaProducer.add(events[i], TIME_OUT_IN_MS);
        }

        startTime = System.currentTimeMillis();
        result = rokkaProducer.add(events[QUEUE_SIZE + 1], TIME_OUT_IN_MS);
        endTime = System.currentTimeMillis();

        Assert.assertFalse(result);
        Assert.assertTrue((endTime - startTime) >= TIME_OUT_IN_MS);
    }

    @Test
    public final void removeShouldBeResultValueNull() throws Exception
    {
        RokkaTestEvent removeElem = rokkaConsumer.remove();
        Assert.assertNull(removeElem);
    }

    @Test
    public final void addAndRemove() throws  Exception
    {
        boolean result;

        for (int i = 0; i < 5; i++)
        {
            result = rokkaProducer.add(events[i], TIME_OUT_IN_MS);
        }

        rokkaConsumer.clear();

        for (int i = 5; i < 15; i++)
        {
            result = rokkaProducer.add(events[i], TIME_OUT_IN_MS);
        }

        result = rokkaProducer.add(events[16], TIME_OUT_IN_MS);
        Assert.assertFalse(result);

        RokkaTestEvent resultElem = rokkaConsumer.remove();
        Assert.assertEquals(events[5], resultElem);

        result = rokkaProducer.add(events[16], TIME_OUT_IN_MS);
        Assert.assertTrue(result);
    }
}
