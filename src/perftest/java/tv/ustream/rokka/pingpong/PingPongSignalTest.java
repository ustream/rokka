package tv.ustream.rokka.pingpong;

/**
 * Created by bingobango on 2/5/14.
 */
public final class PingPongSignalTest
{
    public static final int ROKKA_QUEUE_SIZE = 65535;
    public static final int THREADCOUNT = 6;
    public static final int ACTORCOUNT = 180000;
    public static final int RUNTIMESEC = 20;

    private final RokkaPool scheduler;
    private final Aggr aggr;

    public static final PingPongSignalTest MAIN = new PingPongSignalTest();

    private PingPongSignalTest()
    {
        scheduler = new RokkaPool(THREADCOUNT);
        aggr = new Aggr(scheduler.getRokkaThread((int) (Math.random() * THREADCOUNT)), ACTORCOUNT);
        System.out.print("actorpair:" + ACTORCOUNT + " runtime: " + RUNTIMESEC + " ");
        // write your code here

        int k = 0;
        for (int i = 0; i < ACTORCOUNT; i++)
        {
            if (k >= THREADCOUNT)
            {
                k = 0;
            }
            Actor aActor = new Actor(scheduler, scheduler.getRokkaThread(k++), RUNTIMESEC);
            if (k >= THREADCOUNT)
            {
                k = 0;
            }
            Actor bActor = new Actor(scheduler, scheduler.getRokkaThread(k++), RUNTIMESEC);
            bActor.tell(new Ping(aActor, 0));
        }
    }

    public Aggr getAggr()
    {
        return aggr;
    }

    public static void main(final String[] args)
    {
    }
}
