package tv.ustream.rokka;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class AvailableProcessorCountPublisherToOneProcessorThroughputTestLinkedBlockingQueue
{
    private static final int PUBLISHER_THREAD_NUMBER = Runtime.getRuntime().availableProcessors();
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    private final LinkedBlockingQueue<BaseTestEvent> queue;

    private final ExecutorService executor = Executors.newFixedThreadPool(PUBLISHER_THREAD_NUMBER);

    public static final BaseTestEvent BASE_TEST_EVENT = new BaseTestEvent();

    public AvailableProcessorCountPublisherToOneProcessorThroughputTestLinkedBlockingQueue()
    {
        queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
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
                long retry = 0;
                for (int i = 0; i < ITERATIONS; i++)
                {
                    try
                    {
                        if (!queue.offer(BASE_TEST_EVENT, 5L, TimeUnit.MILLISECONDS))
                        {
                            i--;
                            retry++;
                        }
                        else
                        {
                            successCounter++;
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("Sum add time:" + (end - start) + ".ms ,success:" + successCounter
                        + " ,retry:" + retry + " ,tcps:" + (ITERATIONS * 1000 / (end - start)));
            }
        };
        for (int i = 0; i < PUBLISHER_THREAD_NUMBER; i++)
        {
            executor.execute(r);
        }
        long removeElemCount = 0;

        long startTime = System.currentTimeMillis();
        Object resultElem;
        while (removeElemCount < ITERATIONS * PUBLISHER_THREAD_NUMBER)
        {
            resultElem = queue.poll();

            if (resultElem != null)
            {
                removeElemCount++;
                continue;
            }

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
                + " ,tcps:" + (removeElemCount / endTime * 1000));
        executor.shutdown();
    }

    @Test
    public final void test()
    {
        AvailableProcessorCountPublisherToOneProcessorThroughputTestLinkedBlockingQueue
                test = new AvailableProcessorCountPublisherToOneProcessorThroughputTestLinkedBlockingQueue();
        test.startTest();
    }

}
