package tv.ustream.rokka;

/**
 * Created by bingobango
 */
public abstract class RokkaBaseProducer<EventType>
{
    public abstract void add(final EventType elem);
    public abstract boolean add(final EventType elem, final int timeOutinMs);
    public abstract RokkaQueue getActualQueueAndSetNewQueue();
    public abstract int getMaxQueueSize();
}
