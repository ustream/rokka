package tv.ustream.rokka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.rokka.events.RokkaBatchedEvent;
import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rokka: High Performance Inter-Thread Messaging Libaray
 *
 * @author bingobango
 */
public final class Rokka
{
    private final Logger log = LoggerFactory.getLogger(Rokka.class);

    public static final ThreadLocal<Rokka> queue = new ThreadLocal<Rokka>()
    {
        @Override
        protected Rokka initialValue()
        {
            System.out.println("Creating Rokka Queue for thread: " + Thread.currentThread().getName());
            //TODO configure size!
            return new Rokka(20 * 1000 * 1000);
        }
    };
    private final int maxQueueSize;
    private final RokkaBatchedEvent[] queues;
    private final AtomicInteger nextFreeWriteIndex = new AtomicInteger(0);
    private volatile int nextReadIndex = 0;

    private Rokka(int queueSize)
    {
        maxQueueSize = queueSize;
        queues = new RokkaBatchedEvent[maxQueueSize];
    }

    public boolean add(List<RokkaEvent> datas, long timeOutInMs)
    {
        return add(new RokkaBatchedEvent(datas), timeOutInMs);
    }

    public boolean add(RokkaEvent data, long timeOutInMs)
    {
        int a = 0;
        int b = a << 2;

        return add(new RokkaBatchedEvent(data), timeOutInMs);
    }

    private boolean add(RokkaBatchedEvent rokkaBatchedEvent, long timeOutInMs)
    {
        int freeWriteIndex;

        int generateWriteNextIndex;
        long startTime = System.currentTimeMillis();

        for (;;)
        {
            freeWriteIndex = nextFreeWriteIndex.get();
            if (freeWriteIndex>=0)
            {
                generateWriteNextIndex = freeWriteIndex + 1;
            }
            else
            {
                generateWriteNextIndex=0;
            }
            if ( generateWriteNextIndex >= maxQueueSize )
            {
                generateWriteNextIndex = nextReadIndex == 0 ? -1 : 0;
            }
            if ( generateWriteNextIndex != nextReadIndex )
            {
                if (nextFreeWriteIndex.compareAndSet(freeWriteIndex, generateWriteNextIndex))
                {
                    if (freeWriteIndex < 0)
                    {
                        freeWriteIndex=0;
                    }
                    queues[freeWriteIndex] = rokkaBatchedEvent;
                    return true;
                }
            }
            if ( timeOutInMs > 0 && (System.currentTimeMillis() - startTime) >= timeOutInMs )
            {
                return false;
            }
        }
    }

    public int getAvaibleReadQueueSize()
    {
        int tmpWriteNextIndex = nextFreeWriteIndex.get();
        if (tmpWriteNextIndex >= nextReadIndex)
        {
            return tmpWriteNextIndex - nextReadIndex;
        } else
        {
            if (tmpWriteNextIndex == -1)
            {
                tmpWriteNextIndex = 0;
            }
            return maxQueueSize - nextReadIndex + tmpWriteNextIndex;
        }
    }

    public RokkaBatchedEvent remove()
    {
        RokkaBatchedEvent result = null;
        int readSize = getAvaibleReadQueueSize();
        if (readSize > 0)
        {
            result = queues[nextReadIndex];
            queues[nextReadIndex] = null; //clear reference from the buffer
            nextReadIndex++;
            if (nextReadIndex >= maxQueueSize)
            {
                nextReadIndex = 0;
            }
        } else
        {
            System.out.println("no free elem:" + nextReadIndex + " :: " + nextFreeWriteIndex.get());
        }
        System.out.println("readSize:" + readSize + " ,nextReadIndex:" + nextReadIndex + " ,maxQueueSize:" + maxQueueSize + " ,nextFreeWriteIndex.get():" + nextFreeWriteIndex.get());
        return result;
    }

    public RokkaOutEvent removeAll()
    {
        //log.info("remove {} " , Thread.currentThread() );

        int splitSize = getAvaibleReadQueueSize();
        return removeArrange(splitSize);
    }

    public RokkaOutEvent removeArrange(int size)
    {
        int splitSize = getAvaibleReadQueueSize();
        RokkaBatchedEvent[] result = new RokkaBatchedEvent[splitSize > size ? size : splitSize];
        splitSize = result.length;
//        System.out.println("splitSize:"+splitSize+" ,nextReadIndex:"+nextReadIndex+" ,maxQueueSize:"+maxQueueSize);
        if (splitSize > 0)
        {
            if (nextReadIndex + splitSize > maxQueueSize)
            {
                System.arraycopy(queues, nextReadIndex, result, 0, (maxQueueSize - nextReadIndex));
                System.arraycopy(queues, 0, result, (maxQueueSize - nextReadIndex), splitSize - (maxQueueSize - nextReadIndex));
                Arrays.fill(queues, nextReadIndex, (maxQueueSize - nextReadIndex), null);
                Arrays.fill(queues, 0, splitSize - (maxQueueSize - nextReadIndex), null);
                nextReadIndex = splitSize - (maxQueueSize - nextReadIndex);
            } else
            {
                System.arraycopy(queues, nextReadIndex, result, 0, splitSize);
                Arrays.fill(queues, nextReadIndex, nextReadIndex + splitSize, null);
                nextReadIndex = nextReadIndex + splitSize;
                if (nextReadIndex >= maxQueueSize)
                {
                    nextReadIndex = maxQueueSize;
                }
            }
        }
//        System.out.println("set splitSize:"+splitSize+" ,nextReadIndex:"+nextReadIndex+" ,maxQueueSize:"+maxQueueSize);
        RokkaOutEvent rokkaOutEvent = new RokkaOutEvent(result);
        return rokkaOutEvent;
    }


}

