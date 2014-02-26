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

    private final AtomicReference<RokkaProducerSignalContainer<EventType>> workingSignalContainer =
                                                                                            new AtomicReference<>();

    private final AtomicBoolean signalContainerLock = new AtomicBoolean(true);

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
        signal(rokkaProducer, true);
    }

    public final void signal(final RokkaSignalProducer<EventType> rokkaProducer, final boolean callSingalApp)
    {
        RokkaProducerSignalContainer<EventType> newRpsc;
        do
        {
            if (signalContainerLock.compareAndSet(true, false))
            {
                final RokkaProducerSignalContainer<EventType> rpsc = signalContainer.get();
                if (rpsc == null)
                {
                    newRpsc = new RokkaProducerSignalContainer<>(rokkaProducer);
                }
                else
                {
                    newRpsc = rpsc.add(rokkaProducer);
                }
                signalContainer.set(newRpsc);
                signalContainerLock.set(true);
                if (rpsc == null && callSingalApp)
                {
                    signalApplication.signal(this);
                }
                break;
            }
        } while(true);
    }

    private void generateRokkaEventQueue()
    {
        do
        {
            if (signalContainerLock.compareAndSet(true, false))
            {
                RokkaProducerSignalContainer<EventType> pc = signalContainer.getAndSet(null);
                workingSignalContainer.set(pc);
                signalContainerLock.set(true);
                break;
            }
        } while (true);
        final RokkaProducerSignalContainer<EventType> rpsc = workingSignalContainer.get();
        if (rpsc != null)
        {

            final RokkaSignalProducer<EventType>[] rokkaProducers = rpsc.values;
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
        if (rokkaQueueIterator == null)
        {
            generateRokkaEventQueue();
        }
        return rokkaQueueIterator.iterator();
    }

    public final void endIterable()
    {
        rokkaQueueIterator = null;
        final RokkaProducerSignalContainer<EventType> pc = workingSignalContainer.getAndSet(null);
        if (pc != null)
        {
            for (RokkaSignalProducer<EventType> rpsc : pc.values)
            {
                rpsc.enabledSignalFromConsumer();
            }
        }
        boolean isSendSignalToApplication = false;
        do
        {
            if (signalContainerLock.compareAndSet(true, false))
            {
                if (signalContainer.get() != null)
                {
                    isSendSignalToApplication = true;
                }
                signalContainerLock.set(true);
                break;
            }
        } while (true);

        if (isSendSignalToApplication)
        {
            signalApplication.signal(this);
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
