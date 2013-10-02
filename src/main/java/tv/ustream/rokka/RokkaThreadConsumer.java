package tv.ustream.rokka;

import tv.ustream.rokka.events.EventProcessor;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class RokkaThreadConsumer<EventType> extends RokkaBaseConsumer<EventType> implements Runnable
{
    private final AtomicBoolean isRun = new AtomicBoolean(true);
    private final int sleepTime;
    private EventProcessor<EventType> eventProcessor = null;

    private final Thread eventThread;

    public RokkaThreadConsumer(final String rokkaConsumerThreadName, final int sleepTime,
                               final EventProcessor eventProcessor)
    {
        this(rokkaConsumerThreadName, sleepTime);
        this.eventProcessor = eventProcessor;
    }

    private RokkaThreadConsumer(final String rokkaConsumerThreadName, final int sleepTime)
    {
        this.sleepTime = sleepTime;
        getAtomicReferenceRokkaProducerContainer().set(new RokkaProducerContainer<EventType>());
        eventThread = new Thread(this);
        eventThread.setName(rokkaConsumerThreadName);
        eventThread.start();
    }

    public final void stopConsumer()
    {
        isRun.set(false);
    }

    @Override
    public final void run()
    {
        int lastInt = 0;
        long trans = 0;
        RokkaQueue<EventType> tq = null;
        long st = System.currentTimeMillis();
        Iterator<EventType> iterator;
        while (isRun.get())
        {
            long diff = System.currentTimeMillis() - st;
            if (diff <= sleepTime)
            {
                try
                {
                    Thread.sleep(sleepTime - diff);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            st = System.currentTimeMillis();
            iterator = getRokkaQueueIterator();
            while (iterator.hasNext())
            {
                try
                {
                    this.eventProcessor.receiveEvent(iterator.next());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}

