package tv.ustream.rokka.perfTest;

import tv.ustream.rokka.Rokka;
import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class OnePublisherToOneProcessorThroughputTest
{
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    private final Rokka rokka;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final BaseTestEvent BASE_TEST_EVENT = new BaseTestEvent();

    protected OnePublisherToOneProcessorThroughputTest()
    {
        Rokka.setRokkaQueueSizeCurrentThread(QUEUE_SIZE);
        rokka = Rokka.QUEUE.get();
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
                    if (rokka.add(BASE_TEST_EVENT, 10))
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
        executor.execute(r);
        long removeElemCount = 0;
        RokkaOutEvent removeElems;
        long startTime = System.currentTimeMillis();
        while (removeElemCount < ITERATIONS)
        {
            removeElems = rokka.removeAll();
            for (RokkaEvent baseEvent : removeElems)
            {
                removeElemCount++;
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
                + " ,tcps:" + (removeElemCount * 1000 / endTime));
        executor.shutdown();
    }

    public static void main(final String[] args) throws Exception
    {
        OnePublisherToOneProcessorThroughputTest test = new OnePublisherToOneProcessorThroughputTest();
        test.startTest();
    }

}
