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
    final public int id;

    public RokkaTestEvent(int id)
    {
        this.id = id;
    }
}
