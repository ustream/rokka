package tv.ustream.rokka;

import org.junit.Test;
import tv.ustream.rokka.events.EventProcessor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class AvailableProcessorCountPublisherToOneProcessorThroughputTest
{
    private static final int PUBLISHER_THREAD_NUMBER = Runtime.getRuntime().availableProcessors();
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;

    private final ExecutorService executor = Executors.newFixedThreadPool(PUBLISHER_THREAD_NUMBER);

    public static final BaseTestEvent BASE_TEST_EVENT = new BaseTestEvent();

    private final RokkaBaseConsumer<BaseTestEvent> rokka;
    private long startTime;
    private final CountDownLatch cdl = new CountDownLatch(1);

    private final TestEventProcessor<BaseTestEvent> eventProcessor;

    public AvailableProcessorCountPublisherToOneProcessorThroughputTest()
    {
        eventProcessor = new TestEventProcessor<BaseTestEvent>(ITERATIONS * PUBLISHER_THREAD_NUMBER);
        rokka = new RokkaThreadConsumer<BaseTestEvent>("RokkaBaseConsumer", 3, eventProcessor);
    }

    private void startTest() throws Exception
    {
        System.out.println("Start " + getClass().getSimpleName()
                + " ,queue size:" + QUEUE_SIZE + " ,add iterations per thread:" + ITERATIONS);

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                final RokkaProducer rp = new RokkaProducer(QUEUE_SIZE);
                rokka.addProducer(rp);
                long successCounter = 0;
                long start = System.currentTimeMillis();
                long retry = 0;
                for (int i = 0; i < ITERATIONS; i++)
                {
                    if (rp.add(BASE_TEST_EVENT, 10))
                    {
                        successCounter++;
                    }
                    else
                    {
                        i--;
                        retry++;
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("Sum add time:" + (end - start) + ".ms ,success:"
                        + successCounter + " ,retry:" + retry + " ,tcps:"
                        + (ITERATIONS * 1000 / (end - start)));
            }
        };
        for (int i = 0; i < PUBLISHER_THREAD_NUMBER; i++)
        {
            executor.execute(r);
        }
        startTime = System.currentTimeMillis();
        cdl.await();
    }

    private void endTest(final long count)
    {
        long endTime = (System.currentTimeMillis() - startTime);
        System.out.println("Sum remove time:" + endTime + ".ms ,count:" + count
                + " ,tcps:" + (count * 1000 / endTime));
        executor.shutdown();
        cdl.countDown();
    }


    @Test
    public final void test() throws Exception
    {
        AvailableProcessorCountPublisherToOneProcessorThroughputTest
                test = new AvailableProcessorCountPublisherToOneProcessorThroughputTest();
        test.startTest();
    }

    private class TestEventProcessor<T> implements EventProcessor<T>
    {
        private long counter = 0;
        private final long maxLimit;

        public TestEventProcessor(final long limit)
        {
            this.maxLimit = limit;
        }

        @Override
        public void receiveEvent(final T event)
        {
            counter++;
            if (counter == maxLimit)
            {
                endTest(counter);
            }
        }
    }
}
