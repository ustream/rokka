package tv.ustream.rokka.pingpong;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by bingobango on 2/5/14.
 */
public final class PingPongSignalTest
{
    public static final int ROKKA_QUEUE_SIZE = 65535;
    public static final int THREADCOUNT = 12;
    public static final int ACTORCOUNT = 180000;
    public static final int RUNTIMESEC = 20;

    private final RokkaPool scheduler;
    private final Aggr aggr;

    public static PingPongSignalTest MAIN;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public PingPongSignalTest()
    {
        MAIN = this;
        scheduler = new RokkaPool(THREADCOUNT);
        aggr = new Aggr(scheduler.getRokkaThread((int) (Math.random() * THREADCOUNT)), ACTORCOUNT);
    }

    private void start()
    {

        System.out.print("actorpair:" + ACTORCOUNT + " runtime: " + RUNTIMESEC + " " + " thread: " + THREADCOUNT);
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

    public void stopThread()
    {
        countDownLatch.countDown();
    }

    @Test
    public void test() throws Exception
    {
        PingPongSignalTest ppst = new PingPongSignalTest();
        ppst.start();
        ppst.countDownLatch.await();
        ppst.scheduler.stop();
    }

    public Aggr getAggr()
    {
        return aggr;
    }
}
