package tv.ustream.rokka.events;

/**
 * @author bingobango
 */
public abstract class RokkaEvent
{
    private final long createTime = System.currentTimeMillis();

    public final long getCreateTime()
    {
        return createTime;
    }
}
