package tv.ustream.rokka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.ustream.rokka.events.RokkaEvent;
import tv.ustream.rokka.events.RokkaOutEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rokka: High Performance Inter-Thread Messaging Libaray
 *
 * @author bingobango
 */
public final class Rokka
{
    private static final int DEFAULT_MAX_ROKKA_ARRAY_SIZE = 1 * 1000 * 1000;
    private final Logger log = LoggerFactory.getLogger(Rokka.class);
    private static final int RADIX = 10;

    public static int convertStringtoInteger(final String stringValue, final int defaultValueInt)
    {
        if (stringValue != null)
        {
            try
            {
                return Integer.valueOf(stringValue, RADIX);
            }
            catch (Exception e)
            {
                return defaultValueInt;
            }
        }
        return defaultValueInt;
    }

    public static void setRokkaQueueSizeCurrentThread(final int queueSize)
    {
        if (queueSize < 1)
        {
            throw new IndexOutOfBoundsException();
        }
        String cThreadName = Thread.currentThread().getName();
        System.setProperty("rokka." + cThreadName, "" + queueSize);
    }

    public static final ThreadLocal<Rokka> QUEUE = new ThreadLocal<Rokka>()
    {
        @Override
        protected Rokka initialValue()
        {
            System.out.println("Creating Rokka Queue for thread: " + Thread.currentThread().getName());
            int size = convertStringtoInteger(
                    System.getProperty("rokka." + Thread.currentThread().getName(), null),
                    DEFAULT_MAX_ROKKA_ARRAY_SIZE);
            return new Rokka(size);
        }
    };

    private final int maxQueueSize;
    private final Object[] array;

    private final Thread thread;

    private static final Object EMPTY = new Object();

    private final AtomicLong writeAtomicIndex = new AtomicLong(0);
    private volatile long readIndex = 0;

    public Rokka(final int queueSize)
    {
        maxQueueSize = queueSize;
        array = new Object[maxQueueSize];
        Arrays.fill(array, EMPTY);
        this.thread = Thread.currentThread();
    }

    public boolean add(final List<RokkaEvent> data, final long timeOutInMs)
    {
        return addData(data, timeOutInMs);
    }

    public boolean add(final RokkaEvent data, final long timeOutInMs)
    {
        return addData(data, timeOutInMs);
    }

    private boolean addData(final Object data, final long timeOutInMs)
    {
        long startTime = System.currentTimeMillis();
        for (;;)
        {
            long writeIndex = writeAtomicIndex.get();
            long tmpWriteIndex =  writeIndex - readIndex;
            if (tmpWriteIndex < maxQueueSize)
            {
                if (writeAtomicIndex.compareAndSet(writeIndex, writeIndex + 1))
                {
                    array[(int) (writeIndex % maxQueueSize)] = data;
                    return true;
                }
            }
            if (timeOutInMs > 0 && (System.currentTimeMillis() - startTime) >= timeOutInMs)
            {
                return false;
            }
        }
    }

    private void threadCheck()
    {
        if (thread != Thread.currentThread())
        {
            throw new IllegalStateException("Invalid Thread Access");
        }
    }

    public Object remove()
    {
        threadCheck();

        Object result = null;
        int tmpReadIndex = (int) (readIndex % maxQueueSize);
        int maxIndex = 0;
        if (array[tmpReadIndex] == EMPTY)
        {
            return null;
        }
        else
        {
            long maxPos = writeAtomicIndex.get();
            maxIndex = (int) (maxPos % maxQueueSize);
            int arrayPos = (int) (readIndex % maxQueueSize);
            if (array[arrayPos] != EMPTY)
            {
                result = array[arrayPos];
                array[arrayPos] = EMPTY;
                readIndex++;
            }
        }
        return result;
    }

    public RokkaOutEvent removeAll()
    {
        threadCheck();

        int tmpReadIndex = (int) (readIndex % maxQueueSize);
        Object[] result;
        int maxIndex = 0;
        if (array[tmpReadIndex] == EMPTY)
        {
            result = new Object[0];
        }
        else
        {
            long maxPos = writeAtomicIndex.get();
            maxIndex = (int) (maxPos % maxQueueSize);
            result = new Object[(int) (maxPos - readIndex)];
            long i;
            int arrayPos;
            for (i = readIndex; i < maxPos; i++)
            {
                arrayPos = (int) (i % maxQueueSize);
                if (array[arrayPos] != EMPTY)
                {
                    result[(int) (i - readIndex)] = array[arrayPos];
                    array[arrayPos] = EMPTY;
                }
                else
                {
                    break;
                }
            }
            readIndex += (i - readIndex);
        }
        final RokkaOutEvent rokkaOutEvent = new RokkaOutEvent(result, tmpReadIndex, maxIndex);
        return rokkaOutEvent;
    }

    public void clean() throws Exception
    {
        if (thread != Thread.currentThread())
        {
            throw new Exception("Invalid Thread Access");
        }

        Arrays.fill(array, EMPTY);
        writeAtomicIndex.set(0);
        readIndex = 0;
    }

    public int getMaxQueueSize()
    {
        return maxQueueSize;
    }
}

