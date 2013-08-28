import tv.ustream.rokka.Rokka;
import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bingobango
 */
public class RokkaTest {

    private static class TestAddClass implements Runnable
    {
        private final Rokka queueThread;
        private final int threadId;
        private final int initSize;

        public TestAddClass(int threadId, Rokka queueThread, int initSize)
        {
            this.threadId = threadId;
            this.queueThread = queueThread;
            this.initSize = initSize;
        }

        public void run()
        {
            for ( int j = 0; j < 1000; j++)
            {
                List<RokkaEvent> tmp = new ArrayList<RokkaEvent>(initSize);
                for ( int i = 0; i < initSize/1000; i++ )
                {
                    tmp.add(new AcceptEvent(null));
                }
                long stTime, endTime;
                stTime = System.nanoTime();
                queueThread.add(tmp, 500L);
                endTime = System.nanoTime();
                System.out.println("Add["+threadId+"] sum time:"+(endTime-stTime)+".ns");
            }
        }
    }

    public static void main(String... param)
    {
        long stTime, endTime;
        stTime = System.nanoTime();
        Rokka tlq = Rokka.queue.get();
        endTime = System.nanoTime();

        int EventSize = 30 *1000 *1000;
        int ADDTHREAD_NUMBER = 4;
        Thread[] threads = new Thread[ADDTHREAD_NUMBER];
        TestAddClass[] runnableTest = new TestAddClass[ADDTHREAD_NUMBER];
        for ( int i = 0; i<runnableTest.length; i++ )
        {
            runnableTest[i] = new TestAddClass(i,tlq, EventSize);
            threads[i] = new Thread(runnableTest[i]);
        }

        for ( int i = 0; i < threads.length; i++ )
        {
            threads[i].start();
        }

        int tmpCounter = 0;
        RokkaOutEvent removeElems;
        int size = 0;
        int sumSize = 0;
        while (sumSize < (ADDTHREAD_NUMBER*EventSize))
        {
            size = 0;
            stTime = System.nanoTime();
            try
            {
                removeElems = tlq.removeAll();
                for ( RokkaEvent baseEvent : removeElems)
                {
                    size++;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                break;
            }
            endTime = System.nanoTime();
            if ( size > 0)
            {
                sumSize+=size;
                System.out.println("removeElems() size:"+size+",sum time:"+(endTime-stTime)+".ns -> " + ((endTime-stTime)/1000000)+".ms");
            }
            try
            {
                Thread.sleep(10);
            } catch (Exception e)
            {}
            tmpCounter++;
        }
}}
