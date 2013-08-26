package com.ustream.tv.rokka.events;

import com.ustream.tv.rokka.events.RokkaBatchedEvent;
import com.ustream.tv.rokka.events.RokkaEvent;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * Date: 6/27/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class RokkaOutEvent implements Iterable<RokkaEvent>
{
    private final RokkaBatchedEvent[] events;

    public RokkaOutEvent(RokkaBatchedEvent[] events)
    {
        this.events = events;
    }

    @Override
    public Iterator<RokkaEvent> iterator() {
        return new ResultEventsIterator();
    }

    class ResultEventsIterator implements Iterator<RokkaEvent>
    {
        private int actualPos = 0;
        private int arrayIndex = 0;
        private RokkaEvent nextElem;

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
            if ( events.length > actualPos )
            {
                RokkaBatchedEvent event = events[actualPos];
                if ( event != null )
                {
                    if ( event.rokkaEventList.size() > arrayIndex )
                    {
                        nextElem = event.rokkaEventList.get(arrayIndex++);
                    }
                    else
                    {
                        actualPos++;
                        arrayIndex=0;
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
        public RokkaEvent next()
        {
            if ( nextElem == null )
            {
                return null;
            }
            RokkaEvent result = nextElem;
            generatenextElem();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
