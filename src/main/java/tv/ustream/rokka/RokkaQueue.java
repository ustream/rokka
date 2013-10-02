package tv.ustream.rokka;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public final class RokkaQueue<EventType>
{
    private final EventType[] queue;
    private int position = 0;

    public RokkaQueue(final int queueSize)
    {
        queue = (EventType[]) new Object[queueSize];
    }

    public boolean add(final EventType elem)
    {
        if (position < queue.length)
        {
            queue[position++] = elem;
            return true;
        }
        return false;
    }

    public void clear()
    {
        position = 0;
        Arrays.fill(queue, null);
    }

    public int getPosition()
    {
        return position;
    }

    public boolean isFull()
    {
        return position >= queue.length;
    }

    public boolean isEmpty()
    {
        return position == 0;
    }

    public EventType getQueueElem(final int pos)
    {
        return queue[pos];
    }

    public EventType[] getQueues()
    {
        return queue;
    }

    public int getQueueSize()
    {
        return queue.length;
    }
}