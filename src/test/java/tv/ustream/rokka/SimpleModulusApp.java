package tv.ustream.rokka;

import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * * User: bingobango
 *
 */
public class SimpleModulusApp
{
    final int writeThreadCount;
    final int eventSize;
    final Map<Integer,Long> result = new HashMap<Integer,Long>();
    final ModulusWriter[] writerClass;
    final Rokka rokka;

    public SimpleModulusApp(int writeThread, int eventSize)
    {
        this.writeThreadCount = writeThread;
        this.eventSize = eventSize;
        writerClass = new ModulusWriter[writeThread];
        Rokka.setRokkaQueueSizeCurrentThread(eventSize);
        rokka = Rokka.queue.get();
    }

    public void startTest() throws InterruptedException
    {
        ExecutorService es = Executors.newFixedThreadPool(writeThreadCount);
        for (int i = 0; i <writerClass.length; i++)
        {
            writerClass[i] = new ModulusWriter(rokka, i*eventSize, eventSize);
            es.execute(writerClass[i]);
        }
        long elemCount = 0;
        RokkaOutEvent roe;
        long time;
        while (elemCount<writeThreadCount*eventSize)
        {
            roe = rokka.removeAll();
            for (RokkaEvent re : roe)
            {
                elemCount++;
                if (re instanceof SimpleRokkaEvent)
                {
                    SimpleRokkaEvent sre = (SimpleRokkaEvent)re;
                    int m = sre.id % 10;
                    Long v = result.get(m);
                    if (v==null)
                    {
                        v = new Long(0);
                    }
                    v++;
                    result.put(m,v);
                }
            }
            Thread.sleep(2);
        }
        System.out.println("Result:"+result);
    }

    public static void main(String ...args) throws InterruptedException
    {
        SimpleModulusApp sapp = new SimpleModulusApp(3, 5000000);
        sapp.startTest();
    }

    private class ModulusWriter implements Runnable
    {
        private final Rokka rokka;
        private final int startIndex;
        private final int count;

        public ModulusWriter(Rokka mainRokka, int startIndex, int count)
        {
            this.rokka = mainRokka;
            this.startIndex = startIndex;
            this.count = count;
        }

        @Override
        public void run()
        {
            for (int i = startIndex; i < startIndex+count; i++)
            {
                SimpleRokkaEvent event = new SimpleRokkaEvent(i);
                rokka.add(event, 10);
            }
        }
    }

    private class SimpleRokkaEvent extends RokkaEvent
    {
        final int id;

        public SimpleRokkaEvent(int id)
        {
            this.id = id;
        }
    }
}
