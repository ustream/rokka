package tv.ustream.rokka.pingpong;

/**
 * Created by bingobango on 2/6/14.
 */
interface Event
{
    RokkaPool.RokkaThread getMainThread();
    void execute(Object param);
}
