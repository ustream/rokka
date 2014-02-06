package tv.ustream.rokka.pingpong;

/**
 * Created by bingobango on 2/6/14.
 */
public class Ping
{
    private Actor listener;
    private long time;

    Ping(final Actor listener, final long time)
    {
        this.listener = listener;
        this.time = time;
    }

    public final Actor getListener()
    {
        return listener;
    }
}
