package tv.ustream.rokka.events;


import tv.ustream.rokka.RokkaQueue;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public class RokkaEventIterable<EventType> implements Iterable<EventType>
{
    private final RokkaQueue<EventType>[] rokkaQueues;

    public RokkaEventIterable(final RokkaQueue<EventType>[] sources)
    {
        this.rokkaQueues = sources;
    }

    @Override
    public final Iterator<EventType> iterator()
    {
        return new ResultEventsIterator();
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

        @Override
        public boolean hasNext()
        {
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
