package tv.ustream.rokka;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class OnePublisherToOneProcessorThroughputTestReentrantLock
{
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    private final List<BaseTestEvent> queue;
    private final ReentrantLock lock;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final BaseTestEvent BASE_TEST_EVENT = new BaseTestEvent();

    public OnePublisherToOneProcessorThroughputTestReentrantLock()
    {
        queue = new ArrayList<>(QUEUE_SIZE);
        lock = new ReentrantLock();
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
                    lock.lock();
                    try
                    {
                        queue.add(BASE_TEST_EVENT);
                        successCounter++;
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("Sum add time:" + (end - start) + ".ms ,success:" + successCounter
                        + " ,tcps:" + (ITERATIONS * 1000 / (end - start)));
            }
        };
        executor.execute(r);
        long removeElemCount = 0;

        long startTime = System.currentTimeMillis();
        Object[] resultElems;
        while (removeElemCount < ITERATIONS)
        {
            lock.lock();
            try
            {
                resultElems = queue.toArray(new Object[0]);
                queue.clear();
            }
            finally
            {
                lock.unlock();
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
        OnePublisherToOneProcessorThroughputTestReentrantLock
                test = new OnePublisherToOneProcessorThroughputTestReentrantLock();
        test.startTest();
    }
}
