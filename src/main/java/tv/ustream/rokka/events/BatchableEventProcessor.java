package tv.ustream.rokka.events;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public interface BatchableEventProcessor<EventType>
{
    void receiveBatchableEvent(final EventType[] events, final int length);
}
