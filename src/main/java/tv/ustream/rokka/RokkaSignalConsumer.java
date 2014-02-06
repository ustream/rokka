package tv.ustream.rokka;

import tv.ustream.rokka.events.RokkaEventIterable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bingobango
 */
public class RokkaSignalConsumer<EventType>
{
    private final AtomicReference<RokkaProducerSignalContainer<EventType>> signalContainer = new AtomicReference<>();
    private final SignalApplication signalApplication;

    private RokkaEventIterable<EventType> rokkaQueueIterator = null;
    private Iterable<EventType> iterable = null;
    private final AtomicBoolean eventProgress = new AtomicBoolean(false);

    public RokkaSignalConsumer(final SignalApplication signalApplication)
    {
        if (signalApplication != null)
        {
            this.signalApplication = signalApplication;
        }
        else
        {
            throw new NullPointerException("Wrong signalApplication reference");
        }
    }

    public final boolean isWaitCommand()
    {
        return signalContainer.get() != null;
    }

    public final void signal(final RokkaSignalProducer<EventType> rokkaProducer)
    {
        RokkaProducerSignalContainer<EventType> rpsc;
        RokkaProducerSignalContainer<EventType> newRpsc;
        do
        {
            rpsc = signalContainer.get();
            if (rpsc == null)
            {
                newRpsc = new RokkaProducerSignalContainer<>(rokkaProducer);
            }
            else
            {
                newRpsc = rpsc.add(rokkaProducer);
            }
        } while (!signalContainer.compareAndSet(rpsc, newRpsc));
        if (rpsc == null && !eventProgress.get())
        {
            signalApplication.signal(this);
        }
    }

    private void generateRokkaEventQueue()
    {
        eventProgress.set(true);
        final RokkaProducerSignalContainer<EventType> pc = signalContainer.getAndSet(null);
        if (pc != null)
        {
            final RokkaSignalProducer<EventType>[] rokkaProducers = pc.values;
            final RokkaQueue<EventType>[] rokkaQueues = new RokkaQueue[rokkaProducers.length];
            int pos = 0;
            for (RokkaSignalProducer<EventType> pt : rokkaProducers)
            {
                if (pt != null)
                {
                    rokkaQueues[pos++] = pt.getActualQueueAndSetNewQueue();
                }
            }
            rokkaQueueIterator = new RokkaEventIterable(rokkaQueues, this);
        }
        else
        {
            rokkaQueueIterator = new RokkaEventIterable<>(new RokkaQueue[0], this);
        }
    }

    public final Iterator<EventType> getRokkaQueueIterator()
    {
        if (rokkaQueueIterator == null || !rokkaQueueIterator.hasNext())
        {
            generateRokkaEventQueue();
        }
        return rokkaQueueIterator.iterator();
    }

    public final void endIterable()
    {
        if (signalContainer.get() != null)
        {
            eventProgress.compareAndSet(true, false);
            signalApplication.signal(this);
        }
        else
        {
            eventProgress.compareAndSet(true, false);
        }
    }

    protected static class RokkaProducerSignalContainer<EventType>
    {
        private final RokkaSignalProducer<EventType>[] values;

        public RokkaProducerSignalContainer()
        {
            values = new RokkaSignalProducer[0];
        }

        public RokkaProducerSignalContainer(final RokkaSignalProducer<EventType>[] sourceArray)
        {
            this.values = new RokkaSignalProducer[sourceArray.length + 1];
            System.arraycopy(values, 0, this.values, 0, values.length);
        }

        public RokkaProducerSignalContainer(final RokkaSignalProducer<EventType> value)
        {
            this.values = new RokkaSignalProducer[1];
            this.values[0] = value;
        }

        public RokkaProducerSignalContainer(final RokkaSignalProducer<EventType>[] sourceArray,
                                      final RokkaSignalProducer<EventType> value)
        {
            this.values = new RokkaSignalProducer[sourceArray.length + 1];
            System.arraycopy(sourceArray, 0, this.values, 0, sourceArray.length);
            this.values[values.length - 1] = value;
        }

        public final RokkaProducerSignalContainer<EventType> add(final RokkaSignalProducer<EventType> value)
        {
            return new RokkaProducerSignalContainer(this.values, value);
        }

        public final RokkaSignalProducer<EventType>[] getValues()
        {
            return values;
        }
    }
}
