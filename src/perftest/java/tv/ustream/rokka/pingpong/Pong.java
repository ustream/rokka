package tv.ustream.rokka.pingpong;

/**
 * Created by bingobango on 2/6/14.
 */

public class Pong
{
    private Actor listener;
    private long time;

    Pong(final Actor listener, final long time)
    {
        this.listener = listener;
        this.time = time;
    }

    public final Actor getListener()
    {
        return listener;
    }
}
