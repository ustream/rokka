package com.ustream.tv.rokka.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * Date: 6/27/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
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
