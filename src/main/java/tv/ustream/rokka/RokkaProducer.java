package tv.ustream.rokka;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: bingobango
 */
public class RokkaProducer<EventType> extends RokkaBaseProducer<EventType>
{
    private final RokkaQueue<EventType> queue1;
    private final RokkaQueue<EventType> queue2;
    private final AtomicReference<RokkaQueue<EventType>> actualRokkaQueueRef =
                            new AtomicReference<RokkaQueue<EventType>>();
    private final AtomicBoolean isFreeLock = new AtomicBoolean(true);

    public RokkaProducer(final int queueSize)
    {
        queue1 = new RokkaQueue(queueSize);
        queue2 = new RokkaQueue(queueSize);
        actualRokkaQueueRef.set(queue1);
    }

    public final int getMaxQueueSize()
    {
        return queue1.getQueueSize();
    }

    @Override
    public final void add(final EventType elem)
    {
        do
        {
            if (isFreeLock.compareAndSet(true, false))
            {
                final RokkaQueue<EventType> tq = actualRokkaQueueRef.get();
                if (tq.add(elem))
                {
                    isFreeLock.set(true);
                    return;
                }
                else
                {
                    isFreeLock.set(true);
                    try
                    {
                        Thread.yield();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        while (true);
    }

    @Override
    public final boolean add(final EventType elem, final int timeOut)
    {
        long startTime = System.currentTimeMillis();
        do
        {
            if (isFreeLock.compareAndSet(true, false))
            {
                final RokkaQueue<EventType> tq = actualRokkaQueueRef.get();
                if (tq.add(elem))
                {
                    isFreeLock.set(true);
                    return true;
                }
                else
                {
                    isFreeLock.set(true);
                    try
                    {
                        Thread.yield();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        while (startTime + timeOut > System.currentTimeMillis());
        return false;
    }

    @Override
    public final RokkaQueue getActualQueueAndSetNewQueue()
    {
        for (;;)
        {
            if (isFreeLock.compareAndSet(true, false))
            {
                if (actualRokkaQueueRef.get() == queue1)
                {
                    actualRokkaQueueRef.set(queue2);
                    isFreeLock.set(true);
                    return queue1;
                }
                else
                {
                    actualRokkaQueueRef.set(queue1);
                    isFreeLock.set(true);
                    return queue2;
                }
            }
        }
    }
}
