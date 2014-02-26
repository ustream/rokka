package tv.ustream.rokka;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bingobango
 */
public class RokkaSignalProducer<EventType> extends RokkaBaseProducer<EventType>
{
    private final RokkaQueue<EventType> queue1;
    private final RokkaQueue<EventType> queue2;
    private final AtomicReference<RokkaQueue<EventType>> actualRokkaQueueRef =
            new AtomicReference<RokkaQueue<EventType>>();
    private final AtomicBoolean isFreeLock = new AtomicBoolean(true);

    private final RokkaSignalConsumer<EventType> signalConsumer;
    private final AtomicBoolean signalEnabled = new AtomicBoolean(false);

    public RokkaSignalProducer(final int queueSize, final RokkaSignalConsumer<EventType> rokkaSignalConsumer)
    {
        queue1 = new RokkaQueue(queueSize);
        queue2 = new RokkaQueue(queueSize);
        actualRokkaQueueRef.set(queue1);

        signalConsumer = rokkaSignalConsumer;
        signalEnabled.set(signalConsumer != null);
    }

    @Override
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
                    boolean callSignalConsumer = false;
                    if (signalEnabled.compareAndSet(true, false)) // && tq.getPosition() == 1
                    {
                        callSignalConsumer = true;
                    }
                    isFreeLock.set(true);
                    if (callSignalConsumer)
                    {
                        signalConsumer.signal(this);
                    }
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
                    boolean callSignalConsumer = false;
                    if (signalEnabled.compareAndSet(true, false))
                    {
                        callSignalConsumer = true;
                    }
                    isFreeLock.set(true);
                    if (callSignalConsumer)
                    {
                        signalConsumer.signal(this);
                    }
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

    public final void enabledSignalFromConsumer()
    {
        boolean callSignalConsumer = false;
        do
        {
            if (isFreeLock.compareAndSet(true, false))
            {
                if (signalConsumer != null && !signalEnabled.get())
                {
                    final RokkaQueue<EventType> tq = actualRokkaQueueRef.get();
                    if (tq.getPosition() > 0)
                    {
                        callSignalConsumer = true;
                    }
                    else
                    {
                        signalEnabled.compareAndSet(false, true);
                    }
                }
                isFreeLock.set(true);
                break;
            }
            Thread.yield();
        }
        while (true);
        if (callSignalConsumer)
        {
            signalConsumer.signal(this, false);
        }
    }

    @Override
    public final RokkaQueue getActualQueueAndSetNewQueue()
    {
        do
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
        } while (true);
    }
}
