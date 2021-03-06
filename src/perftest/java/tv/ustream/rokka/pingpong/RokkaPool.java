package tv.ustream.rokka.pingpong;

import tv.ustream.rokka.RokkaSemaphore;
import tv.ustream.rokka.RokkaSignalConsumer;
import tv.ustream.rokka.RokkaSignalProducer;
import tv.ustream.rokka.SignalApplication;

import java.util.Iterator;

/**
 * Created by bingobango on 2/6/14.
 */
public class RokkaPool
{
    private final RokkaThread[] rokkaThreads;

    public RokkaPool(final int threadCount)
    {
        rokkaThreads = new RokkaThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            rokkaThreads[i] = new RokkaThread(threadCount, i);
        }
        for (int i = 0; i < threadCount; i++)
        {
            for (int j = 0; j < threadCount; j++)
            {
                rokkaThreads[i].createRokkaProducer(rokkaThreads[j].getConsumer(), j);
            }
        }

        for (int i = 0; i < threadCount; i++)
        {
            rokkaThreads[i].start();
        }
    }

    public final void stop()
    {
        for (int i = 0; i < rokkaThreads.length; i++)
        {
            rokkaThreads[i].stopThread();
        }
    }

    public final RokkaThread getRokkaThread(final int index)
    {
        return rokkaThreads[index];
    }

    public class RokkaThread extends Thread implements SignalApplication
    {
        private final RokkaSignalConsumer<RokkaQueueElem> consumer;
        private final RokkaSignalProducer<RokkaQueueElem>[] producers;
        private final int index;

        private final RokkaSemaphore semaphore = new RokkaSemaphore();

        private volatile boolean running = true;

        public RokkaThread(final int consumerThreadCount, final int index)
        {
            this.index = index;
            consumer = new RokkaSignalConsumer<>(this);
            producers = new RokkaSignalProducer[consumerThreadCount];
        }

        public final void createRokkaProducer(final RokkaSignalConsumer<RokkaQueueElem> rokkaSignalConsumer,
                                              final int index)
        {
            producers[index] = new RokkaSignalProducer(PingPongSignalTest.ROKKA_QUEUE_SIZE, rokkaSignalConsumer);
        }

        public final void addPoll(final Event event, final Object param)
        {
            if (event != null)
            {
                int threadIndex = event.getMainThread().getIndex();
                producers[threadIndex].add(new RokkaQueueElem(event, param));
            }
        }

        public final void stopThread()
        {
            running = false;
            semaphore.release();
        }

        public final RokkaSignalConsumer<RokkaQueueElem> getConsumer()
        {
            return consumer;
        }

        public final int getIndex()
        {
            return index;
        }

        @Override
        public final void signal(final RokkaSignalConsumer rokkaSignalConsumer)
        {
            semaphore.release();
        }

        public final void run()
        {
            setName("RokkaThread-" + index);
            long t = 0;
            RokkaQueueElem rqe;
            do
            {
                semaphore.acquire();

                Iterator<RokkaQueueElem> iterator = consumer.getRokkaQueueIterator();

                while (iterator.hasNext())
                {
                    rqe = iterator.next();
                    rqe.getEvent().execute(rqe.getParam());
                    t++;
                }
            } while (running);

            System.out.println("Exit rokka thread[" + index + "]");
        }
    }

    public static class RokkaQueueElem
    {
        private final Event event;
        private final Object param;

        public RokkaQueueElem(final Event event, final Object param)
        {
            this.event = event;
            this.param = param;
        }

        public final Event getEvent()
        {
            return event;
        }

        public final Object getParam()
        {
            return param;
        }
    }
}


