package tv.ustream.rokka;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class AvailableProcessorCountPublisherToOneProcessorThroughputTestSynchronized
{
    private static final int PUBLISHER_THREAD_NUMBER = Runtime.getRuntime().availableProcessors();
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    private final List<BaseTestEvent> queue;

    private final ExecutorService executor = Executors.newFixedThreadPool(PUBLISHER_THREAD_NUMBER);

    public static final BaseTestEvent BASE_TEST_EVENT = new BaseTestEvent();

    public AvailableProcessorCountPublisherToOneProcessorThroughputTestSynchronized()
    {
        queue = new ArrayList<>(QUEUE_SIZE);
    }

    private void startTest()
    {
        System.out.println("Start " + getClass().getSimpleName()
                + " ,queue size:" + QUEUE_SIZE + " ,add iterations per thread:" + ITERATIONS);

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                long successCounter = 0;
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++)
                {
                    synchronized (queue)
                    {
                        queue.add(BASE_TEST_EVENT);
                        successCounter++;
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("Sum add time:" + (end - start) + ".ms ,success:" + successCounter
                        + " ,tcps:" + (ITERATIONS * 1000) / (end - start));
            }
        };
        for (int i = 0; i < PUBLISHER_THREAD_NUMBER; i++)
        {
            executor.execute(r);
        }
        long removeElemCount = 0;

        long startTime = System.currentTimeMillis();
        Object[] resultElems;
        while (removeElemCount < ITERATIONS * PUBLISHER_THREAD_NUMBER)
        {
            synchronized (queue)
            {
                resultElems = queue.toArray(new Object[0]);
                queue.clear();
            }
            removeElemCount += resultElems.length;

            try
            {
                Thread.sleep(1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        long endTime = (System.currentTimeMillis() - startTime);
        System.out.println("Sum remove time:" + endTime + ".ms ,count:" + removeElemCount
                + " ,tcps:" + (removeElemCount * 1000 / endTime));
        executor.shutdown();
    }

    @Test
    public final void test()
    {
        AvailableProcessorCountPublisherToOneProcessorThroughputTestSynchronized
                test = new AvailableProcessorCountPublisherToOneProcessorThroughputTestSynchronized();
        test.startTest();
    }

}
