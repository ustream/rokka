package tv.ustream.rokka.events;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * To change this template use File | Settings | File Templates.
 */
public interface EventProcessor<EventType>
{
    void receiveEvent(final EventType event);
}
