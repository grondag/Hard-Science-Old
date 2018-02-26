package grondag.hard_science.simulator.fobs;

public class TaskPriority implements Comparable<TaskPriority>
{
    public static final TaskPriority NONE = new TaskPriority(Integer.MAX_VALUE);

//    private static long maxSequence;

    private final long value;


    public TaskPriority(long fromLong)
    {
        this.value = fromLong;
    }

    public long toLong()
    {
        return this.value;
    }

    @Override
    public int compareTo(TaskPriority o)
    {
        return Long.compareUnsigned(this.value, o.value);
    }
}
