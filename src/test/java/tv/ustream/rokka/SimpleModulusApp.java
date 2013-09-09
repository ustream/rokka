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
    private final int writeThreadCount;
    private final int eventSize;
    private final Map<Integer, Long> result = new HashMap<Integer, Long>();
    private final ModulusWriter[] writerClass;
    private final Rokka rokka;

    protected SimpleModulusApp(final int writeThread, final int eventSize)
    {
        this.writeThreadCount = writeThread;
        this.eventSize = eventSize;
        writerClass = new ModulusWriter[writeThread];
        Rokka.setRokkaQueueSizeCurrentThread(eventSize);
        rokka = Rokka.QUEUE.get();
    }

    private void startTest() throws InterruptedException
    {
        ExecutorService es = Executors.newFixedThreadPool(writeThreadCount);
        for (int i = 0; i < writerClass.length; i++)
        {
            writerClass[i] = new ModulusWriter(rokka, i * eventSize, eventSize);
            es.execute(writerClass[i]);
        }
        long elemCount = 0;
        RokkaOutEvent roe;
        long time;
        while (elemCount < writeThreadCount * eventSize)
        {
            roe = rokka.removeAll();
            for (RokkaEvent re : roe)
            {
                elemCount++;
                if (re instanceof SimpleRokkaEvent)
                {
                    SimpleRokkaEvent sre = (SimpleRokkaEvent) re;
                    int m = sre.getId() % 10;
                    Long v = result.get(m);
                    if (v == null)
                    {
                        v = new Long(0);
                    }
                    v++;
                    result.put(m, v);
                }
            }
            Thread.sleep(2);
        }
        System.out.println("Result:" + result);
        es.shutdown();
    }

    public static void main(final String ...args) throws InterruptedException
    {
        SimpleModulusApp sapp = new SimpleModulusApp(3, 1000000);
        sapp.startTest();
    }

    private class ModulusWriter implements Runnable
    {
        private final Rokka rokka;
        private final int startIndex;
        private final int count;

        public ModulusWriter(final Rokka mainRokka, final int startIndex, final int count)
        {
            this.rokka = mainRokka;
            this.startIndex = startIndex;
            this.count = count;
        }

        @Override
        public void run()
        {
            for (int i = startIndex; i < startIndex + count; i++)
            {
                SimpleRokkaEvent event = new SimpleRokkaEvent(i);
                if (!rokka.add(event, 10))
                {
                    i--;
                }
            }
        }
    }

    private class SimpleRokkaEvent extends RokkaEvent
    {
        private final int id;

        public SimpleRokkaEvent(final int id)
        {
            this.id = id;
        }

        public final int getId()
        {
            return id;
        }
    }
}
