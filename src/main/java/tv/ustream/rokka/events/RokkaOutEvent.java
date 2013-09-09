package tv.ustream.rokka.events;

import javax.management.Query;
import java.util.Iterator;
import java.util.List;

/**
 * @author bingobango
 */
public class RokkaOutEvent implements Iterable<RokkaEvent>
{
    private final Object[] events;
    private final int startPos;
    private final int endPos;

    public RokkaOutEvent(final Object[] events, int startPos, int endPos)
    {
        this.events = events;
        this.startPos = startPos;
        this.endPos = endPos;
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
                Object event = events[actualPos];
                if ( event != null )
                {
                    if ( event instanceof List)
                    {
                        List tmpRokkaEventList = (List)event;
                        if ( tmpRokkaEventList.size() > arrayIndex )
                        {
                            nextElem = (RokkaEvent)tmpRokkaEventList.get(arrayIndex++);
                        }
                        else
                        {
                            actualPos++;
                            arrayIndex = 0;
                            generatenextElem();
                        }
                    } else if ( event instanceof RokkaEvent )
                    {
                        nextElem = (RokkaEvent)event;
                        actualPos++;
                    }
                    else
                    {
                        nextElem = null;
                    }
                }
                else
                {
//                    System.out.println("NULL " + events.length + " :: " + actualPos + " --> " + event + " ,startPos:" + startPos +" ,endPos:" + endPos );
                    nextElem = null;
                    /*
                    actualPos++;
                    generatenextElem();
                    */
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
