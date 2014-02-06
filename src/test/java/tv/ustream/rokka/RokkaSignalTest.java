package tv.ustream.rokka;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertTrue;

/**
 * Created by bingobango.
 */
public class RokkaSignalTest implements SignalApplication
{
    private static final int QUEUE_SIZE = 10;
    private static final int TEST_EVENT_SIZE = 20;
    private static final int TIME_OUT_IN_MS = 10;
    private RokkaSignalConsumer<RokkaTestEvent> rokkaConsumer;
    private RokkaSignalProducer<RokkaTestEvent> rokkaProducer;
    private RokkaTestEvent[] events;

    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private AtomicInteger signalCounter;

    @Before
    public final void setUp() throws Exception
    {
        rokkaConsumer = new RokkaSignalConsumer(this);

        generateTestEvents(TEST_EVENT_SIZE);
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
    public final void signalTest() throws  Exception
    {
        signalCounter = new AtomicInteger(0);

        TestProducerThread testProducerThread = new TestProducerThread();
        testProducerThread.start();
        int transaction = 0;
        countDownLatch.await();
        do
        {
            Iterator<RokkaTestEvent> iter = rokkaConsumer.getRokkaQueueIterator();
            while (iter.hasNext())
            {
                RokkaTestEvent rokkaEvent = iter.next();
                if (rokkaEvent != null)
                {
                    transaction++;
                }
            }
        } while (rokkaConsumer.isWaitCommand());
        assertTrue(transaction == 20);
        assertTrue(signalCounter.get() >= 2);
    }

    @After
    public final void tearDown() throws Exception
    {
        if (events != null)
        {
            Arrays.fill(events, null);
        }
    }

    @Override
    public final void signal(final RokkaSignalConsumer rokkaSignalConsumer)
    {
        signalCounter.incrementAndGet();
        countDownLatch.countDown();
    }

    private class TestProducerThread extends Thread
    {
        public TestProducerThread()
        {
            rokkaProducer = new RokkaSignalProducer<>(QUEUE_SIZE, rokkaConsumer);
        }

        public void run()
        {
            for (int i = 0; i < events.length; i++)
            {
                rokkaProducer.add(events[i]);
            }
        }
    }
}
