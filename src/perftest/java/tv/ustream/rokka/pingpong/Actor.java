package tv.ustream.rokka.pingpong;

/**
 * Created by bingobango on 2/6/14.
 */
public class Actor implements Event
{
    private RokkaPool scheduler;
    private int runtimesec;
    private int counter;
    private long started;
    private RokkaPool.RokkaThread mainThread;

    public Actor(final RokkaPool scheduler, final RokkaPool.RokkaThread mainThread, final int runtimesec)
    {
        this.scheduler = scheduler;
        this.runtimesec = runtimesec;
        this.mainThread = mainThread;
    }

    public final RokkaPool.RokkaThread getMainThread()
    {
        return mainThread;
    }

    public final void execute(final Object data)
    {
        if (data instanceof Ping)
        {
            mainThread.addPoll(((Ping) data).getListener(), new Pong(this, 0));
        }
        else if (data instanceof Pong)
        {
            if (counter == 0)
            {
                started = System.currentTimeMillis();
                //System.out.println("Started " + started);
            }

            if (System.currentTimeMillis() - started > (runtimesec * 1000))
            {
                mainThread.addPoll(PingPongSignalTest.MAIN.getAggr(), (counter / PingPongSignalTest.RUNTIMESEC));
            }
            else
            {
                counter++;
                mainThread.addPoll(((Pong) data).getListener(), new Ping(this, 0));
            }
        }
    }

    public final void tell(final Object obj)
    {
        mainThread.addPoll(this, obj);
    }
}
