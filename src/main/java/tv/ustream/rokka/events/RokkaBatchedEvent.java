package tv.ustream.rokka.events;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bingobango
 */
public class RokkaBatchedEvent
{
    public final List<RokkaEvent> rokkaEventList;


    public RokkaBatchedEvent(RokkaEvent rokkaEvent)
    {
        rokkaEventList = new ArrayList<RokkaEvent>(1);
        rokkaEventList.add(rokkaEvent);
    }

    public RokkaBatchedEvent(List<RokkaEvent> rokkaEvent)
    {
        rokkaEventList = rokkaEvent;
    }
}
