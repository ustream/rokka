package tv.ustream.rokka;

import tv.ustream.rokka.events.RokkaEvent;

/**
 * Created with IntelliJ IDEA.
 * User: bingobango
 * Date: 9/4/13
 * Time: 9:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class RokkaTestEvent extends RokkaEvent
{
    private final int id;

    public RokkaTestEvent(final int id)
    {
        this.id = id;
    }

    public final int getId()
    {
        return id;
    }
}
