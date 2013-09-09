package tv.ustream.rokka.perfTest;

import tv.ustream.rokka.Rokka;
import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * Date: 9/4/13
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnePublisherToOneProcessorThroughputTest
{
    private static final int QUEUE_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 200L;
    public final Rokka rokka;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);

    public static final BaseTestEvent event = new BaseTestEvent();

    public enum DaemonThreadFactory implements ThreadFactory
    {
        INSTANCE;

        @Override
        public Thread newThread(final Runnable r)
        {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }

    public OnePublisherToOneProcessorThroughputTest()
    {
        Rokka.setRokkaQueueSizeCurrentThread(QUEUE_SIZE);
        rokka = Rokka.queue.get();
    }


    public void startTest()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                long successCounter = 0;
                long start = System.currentTimeMillis();
                long failed = 0;
                for (int i = 0; i<ITERATIONS; i++)
                {
                    if (rokka.add(event, 10))
                    {
                        successCounter++;
                    }
                    else
                    {
                        i--;
                        failed++;
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("Sum add time:"+(end-start)+".ms ,success:"+successCounter+" ,failed:"+failed+" , tcps:"+(ITERATIONS/(end-start)*1000));
            }
        };
        executor.execute(r);
        long removeElemCount=0;
        RokkaOutEvent removeElems;
        long startTime = System.currentTimeMillis();
        while (removeElemCount<ITERATIONS)
        {
            removeElems = rokka.removeAll();
            for (RokkaEvent baseEvent : removeElems)
            {
                removeElemCount++;
            }

            try
            {
                Thread.sleep(1);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        long endTime = (System.currentTimeMillis() - startTime);
        System.out.println("Sum remove time:"+endTime+".ms ,count:"+removeElemCount+" , tcps:" + (removeElemCount/endTime*1000));

    }

    public static void main(String[] args) throws Exception
    {
        OnePublisherToOneProcessorThroughputTest test = new OnePublisherToOneProcessorThroughputTest();
        test.startTest();
    }

}
