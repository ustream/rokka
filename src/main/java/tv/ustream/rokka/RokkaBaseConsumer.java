package tv.ustream.rokka;

import tv.ustream.rokka.events.RokkaEventIterable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class RokkaBaseConsumer<EventType>
{
    private final AtomicReference<RokkaProducerContainer<EventType>> rokkaProducerContainer =
                                                            new AtomicReference<RokkaProducerContainer<EventType>>();

    private Iterator<EventType> rokkaQueueIterator = null;

    public RokkaBaseConsumer()
    {
        rokkaProducerContainer.set(new RokkaProducerContainer<EventType>());
    }


    public final void addProducer(final RokkaProducer rokkaProducer)
    {
        RokkaProducerContainer rpc;
        RokkaProducerContainer newRpc;
        do
        {
            rpc = rokkaProducerContainer.get();
            newRpc = rpc.add(rokkaProducer);
        } while (!rokkaProducerContainer.compareAndSet(rpc, newRpc));
    }

    public final AtomicReference<RokkaProducerContainer<EventType>> getAtomicReferenceRokkaProducerContainer()
    {
        return this.rokkaProducerContainer;
    }

    public final RokkaProducerContainer<EventType> getRokkaProducerContainer()
    {
        return this.rokkaProducerContainer.get();
    }

    private void generateRokkaEventQueue()
    {
        final RokkaProducerContainer<EventType> pc = getRokkaProducerContainer();
        final RokkaProducer<EventType>[] rokkaProducers = pc.values;
        final RokkaQueue<EventType>[] rokkaQueues = new RokkaQueue[rokkaProducers.length];
        int pos = 0;
        for (RokkaProducer<EventType> pt : rokkaProducers)
        {
            if (pt != null)
            {
                rokkaQueues[pos++] = pt.getActualQueueAndSetNewQueue();
            }
        }
        rokkaQueueIterator = new RokkaEventIterable(rokkaQueues).iterator();
    }

    public final Iterator<EventType> getRokkaQueueIterator()
    {
        if (rokkaQueueIterator == null || !rokkaQueueIterator.hasNext())
        {
            generateRokkaEventQueue();
        }
        return rokkaQueueIterator;
    }

    public final EventType remove()
    {
        if (rokkaQueueIterator == null || !rokkaQueueIterator.hasNext())
        {
            generateRokkaEventQueue();
        }
        return rokkaQueueIterator.next();
    }

    public final void clear()
    {
        if (rokkaQueueIterator == null || !rokkaQueueIterator.hasNext())
        {
            generateRokkaEventQueue();
        }
        EventType et;
        do
        {
            et = rokkaQueueIterator.next();
        } while (et != null);
    }

    protected static class RokkaProducerContainer<EventType>
    {
        private final RokkaProducer<EventType>[] values;

        public RokkaProducerContainer()
        {
            values = new RokkaProducer[0];
        }

        public RokkaProducerContainer(final RokkaProducer<EventType>[] sourceArray)
        {
            this.values = new RokkaProducer[sourceArray.length + 1];
            System.arraycopy(values, 0, this.values, 0, values.length);
        }

        public RokkaProducerContainer(final RokkaProducer<EventType> value)
        {
            this.values = new RokkaProducer[1];
            this.values[0] = value;
        }

        public RokkaProducerContainer(final RokkaProducer<EventType>[] sourceArray,
                                      final RokkaProducer<EventType> value)
        {
            this.values = new RokkaProducer[sourceArray.length + 1];
            System.arraycopy(sourceArray, 0, this.values, 0, sourceArray.length);
            this.values[values.length - 1] = value;
        }

        public final RokkaProducerContainer add(final RokkaProducer<EventType> value)
        {
            return new RokkaProducerContainer(this.values, value);
        }

        public final RokkaProducer<EventType>[] getValues()
        {
            return values;
        }
    }
}
