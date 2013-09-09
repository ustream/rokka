package tv.ustream.rokka;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * Date: 9/3/13
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class RokkaTest
{
    private static final int QUEUE_SIZE = 10;
    private static final int TEST_EVENT_SIZE = 20;
    private static final int TIME_OUT_IN_MS = 10;
    private Rokka rokka;
    private RokkaTestEvent[] events;

    @Before
    public void setUp() throws Exception
    {
        Rokka.setRokkaQueueSizeCurrentThread(QUEUE_SIZE);
        generateTestEvents(TEST_EVENT_SIZE);
        rokka = Rokka.QUEUE.get();
    }

    @After
    public void tearDown() throws Exception
    {
        rokka.clean();
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
    public void getMaxQueueSizeShouldBeCheckValidQueueSize()
    {
        int size = rokka.getMaxQueueSize();
        Assert.assertEquals(QUEUE_SIZE, size);
    }

    @Test
    public void addAndRemoveShouldBeCheckValidEventResult() throws Exception
    {
        boolean result;

        for (int i = 0; i < QUEUE_SIZE; i++)
        {
            result = rokka.add(events[i], TIME_OUT_IN_MS);
            Assert.assertTrue(result);

            Object removeObj = rokka.remove();
            Assert.assertEquals(events[i], removeObj);
        }
    }

    @Test
    public void addShouldBeCheckTimeout() throws Exception
    {
        boolean result;
        long startTime, endTime;

        for (int i = 0; i < QUEUE_SIZE; i++)
        {
            rokka.add(events[i], TIME_OUT_IN_MS);
        }

        startTime = System.currentTimeMillis();
        result = rokka.add(events[QUEUE_SIZE + 1], TIME_OUT_IN_MS);
        endTime = System.currentTimeMillis();

        Assert.assertFalse(result);
        Assert.assertTrue((endTime - startTime) >= TIME_OUT_IN_MS);
    }

    @Test
    public void removeAll() throws Exception
    {
        boolean result;

        for (int i = 0; i < QUEUE_SIZE; i++)
        {
            result = rokka.add(events[i], TIME_OUT_IN_MS);
         }

        RokkaOutEvent roe = rokka.removeAll();
        Assert.assertNotNull(roe);

        int pos = 0;
        for (Object elem : roe)
        {
            Assert.assertEquals(events[pos++], elem);
        }
    }

    @Test
    public void removeShouldBeResultValueNull() throws Exception
    {
        Object removeElem = rokka.remove();
        Assert.assertNull(removeElem);
    }

    @Test
    public void removeAllShouldBeResultEmptyIterator() throws Exception
    {
        RokkaOutEvent roe = rokka.removeAll();
        Assert.assertNotNull(roe);

        Assert.assertFalse(roe.iterator().hasNext());
    }

    @Test
    public void addAndRemove() throws  Exception
    {
        boolean result;

        for (int i = 0; i < 5; i++)
        {
            result = rokka.add(events[i], TIME_OUT_IN_MS);
        }

        RokkaOutEvent roe = rokka.removeAll();

        for (int i = 5; i < 10; i++)
        {
            result = rokka.add(events[i], TIME_OUT_IN_MS);
        }

        rokka.remove();

        for (int i = 10; i < 16; i++)
        {
            rokka.add(events[i], TIME_OUT_IN_MS);
        }

        result = rokka.add(events[16], TIME_OUT_IN_MS);
        Assert.assertFalse(result);

        Object resultElem = rokka.remove();
        Assert.assertEquals(events[6], resultElem);

        result = rokka.add(events[16], TIME_OUT_IN_MS);
        Assert.assertTrue(result);
    }
}
