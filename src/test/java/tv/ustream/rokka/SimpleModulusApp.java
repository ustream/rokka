package tv.ustream.rokka;

import java.util.HashMap;
import java.util.Iterator;
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
    private final RokkaBaseConsumer<SimpleRokkaEvent> rokka;

    protected SimpleModulusApp(final int writeThread, final int eventSize)
    {
        this.writeThreadCount = writeThread;
        this.eventSize = eventSize;
        writerClass = new ModulusWriter[writeThread];
        rokka = new RokkaBaseConsumer<SimpleRokkaEvent>();
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
        Iterator<SimpleRokkaEvent> simpleRokkaEventIterator;
        while (elemCount < writeThreadCount * eventSize)
        {
            simpleRokkaEventIterator = rokka.getRokkaQueueIterator();

            while (simpleRokkaEventIterator.hasNext())
            {
                SimpleRokkaEvent sre = simpleRokkaEventIterator.next();
                elemCount++;
                int m = sre.getId() % 10;
                Long v = result.get(m);
                if (v == null)
                {
                    v = new Long(0);
                }
                v++;
                result.put(m, v);
            }
            Thread.sleep(2);
        }
        System.out.println("Result:" + result);
        es.shutdown();
    }

    public static void main(final String ...args) throws InterruptedException
    {
        SimpleModulusApp sapp = new SimpleModulusApp(3, 10000000);
        sapp.startTest();
    }

    private class ModulusWriter implements Runnable
    {
        private final RokkaBaseConsumer<SimpleRokkaEvent> rokka;
        private final int startIndex;
        private final int count;

        public ModulusWriter(final RokkaBaseConsumer<SimpleRokkaEvent> mainRokka, final int startIndex, final int count)
        {
            this.rokka = mainRokka;
            this.startIndex = startIndex;
            this.count = count;
        }

        @Override
        public void run()
        {
            RokkaProducer<SimpleRokkaEvent> rokkaProducer = new RokkaProducer<SimpleRokkaEvent>(65535);
            rokka.addProducer(rokkaProducer);
            int i;
            for (i = startIndex; i < startIndex + count; i++)
            {
                SimpleRokkaEvent event = new SimpleRokkaEvent(i);
                if (!rokkaProducer.add(event, 10))
                {
                    i--;
                }
            }
        }
    }

    private class SimpleRokkaEvent
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
