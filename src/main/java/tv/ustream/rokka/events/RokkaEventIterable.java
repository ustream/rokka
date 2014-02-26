package tv.ustream.rokka.events;


import tv.ustream.rokka.RokkaQueue;
import tv.ustream.rokka.RokkaSignalConsumer;

import java.util.Iterator;

/**
 * User: bingobango
 */
public class RokkaEventIterable<EventType> implements Iterable<EventType>
{
    private final RokkaQueue<EventType>[] rokkaQueues;
    private final RokkaSignalConsumer<EventType> rokkaSignalConsumer;
    private ResultEventsIterator iterator = null;

    public RokkaEventIterable(final RokkaQueue<EventType>[] sources)
    {
        this(sources, null);
    }

    public RokkaEventIterable(final RokkaQueue<EventType>[] sources,
                              final RokkaSignalConsumer<EventType> rokkaSignalConsumer)
    {
        this.rokkaQueues = sources;
        this.rokkaSignalConsumer = rokkaSignalConsumer;
    }

    @Override
    public final Iterator<EventType> iterator()
    {
        if (iterator == null)
        {
            iterator = new ResultEventsIterator();
        }
        return iterator;
    }

    public final boolean hasNext()
    {
        return iterator != null && iterator.hasNextNoAction();
    }

    public final void clear()
    {
        for (RokkaQueue<EventType> rokkaQueue : rokkaQueues)
        {
            rokkaQueue.clear();
        }
    }

    class ResultEventsIterator implements Iterator<EventType>
    {
        private int actualPos = 0;
        private int arrayIndex = 0;
        private EventType nextElem;

        public ResultEventsIterator()
        {
            generatenextElem();
        }

        public boolean hasNextNoAction()
        {
            return nextElem != null;
        }

        @Override
        public boolean hasNext()
        {
            if (nextElem == null && rokkaSignalConsumer != null)
            {
                rokkaSignalConsumer.endIterable();
            }
            return nextElem != null;
        }

        private void generatenextElem()
        {
            if (rokkaQueues.length > actualPos)
            {
                RokkaQueue<EventType> rokkaQueue = rokkaQueues[actualPos];
                if (rokkaQueue != null)
                {
                    if (rokkaQueue.getPosition() > arrayIndex)
                    {
                        nextElem = rokkaQueue.getQueueElem(arrayIndex++);
                    }
                    else
                    {
                        actualPos++;
                        arrayIndex = 0;
                        rokkaQueue.clear();
                        generatenextElem();
                    }
                }
                else
                {
                    nextElem = null;
                }
            }
            else
            {
                nextElem = null;
            }
        }

        @Override
        public EventType next()
        {
            if (nextElem == null)
            {
                if (rokkaSignalConsumer != null)
                {
                    rokkaSignalConsumer.endIterable();
                }
                return null;
            }
            EventType result = nextElem;
            generatenextElem();
            return result;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
