package tv.ustream.rokka;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by bingobango on 2/26/14.
 */
public class RokkaSemaphore extends ReentrantLock
{
    private final Condition condition;
    private final AtomicBoolean isWaiting = new AtomicBoolean(false);

    public RokkaSemaphore()
    {
        condition = newCondition();
    }

    public final void release()
    {
        if (isWaiting.compareAndSet(false, true))
        {
            lock();
            try
            {
                condition.signalAll();
            }
            finally
            {
                unlock();
            }
        }
    }

    public final void acquire()
    {
        if (!isWaiting.compareAndSet(true, false))
        {
            do
            {
                lock();
                try
                {
                    condition.awaitNanos(10000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    unlock();
                }
            } while (!isWaiting.get());
        }
    }
}
