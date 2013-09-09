package tv.ustream.rokka.perfTest;

import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class OnePublisherToOneProcessorThroughPutTestLinkedBlockingQueue
{
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    private final LinkedBlockingQueue<RokkaEvent> queue;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final BaseTestEvent BASE_TEST_EVENT = new BaseTestEvent();

    protected OnePublisherToOneProcessorThroughPutTestLinkedBlockingQueue()
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
                        + " ,retry:" + retry + " ,tcps:" + (ITERATIONS / (end - start) * 1000));
            }
        };
        executor.execute(r);
        long removeElemCount = 0;
        RokkaOutEvent removeElems;
        long startTime = System.currentTimeMillis();
        Object resultElem;
        while (removeElemCount < ITERATIONS)
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

    public static void main(final String[] args) throws Exception
    {
        OnePublisherToOneProcessorThroughPutTestLinkedBlockingQueue
                test = new OnePublisherToOneProcessorThroughPutTestLinkedBlockingQueue();
        test.startTest();
    }
}
