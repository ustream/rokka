package tv.ustream.rokka.pingpong;

/**
 * Created by bingobango on 2/6/14.
 */
public final class Aggr implements Event
{
    private long total = 0;
    private int counter = 0;
    private final int maxCount;
    private final RokkaPool.RokkaThread mainThread;

    public Aggr(final RokkaPool.RokkaThread mainThread, final int maxCount)
    {
        this.maxCount = maxCount;
        this.mainThread = mainThread;
    }

    @Override
    public RokkaPool.RokkaThread getMainThread()
    {
        return mainThread;
    }

    public void execute(final Object data)
    {
        total += (Integer) data;

        counter++;

        if (counter >= maxCount)
        {
            System.out.println("Total: " + (total) + " per thread: " + (total / PingPongSignalTest.THREADCOUNT)
                               + " counter: " + counter);
            System.exit(0);
        }
    }
}
