package grondag.hard_science.library.concurrency;

import javax.annotation.Nonnull;

/**
 * Re-generates array needed by counted job
 * if it marked dirty.  Requires that you call
 * {@link #setDirty()} any time you modify the collection
 *
 */
public abstract class SimpleCountedJobBacker implements ICountedJobBacker
{
    private volatile Object[] operands = null;
    
    public void setDirty()
    {
        synchronized(this)
        {
            this.operands = null;
        }
    }

    @Nonnull
    protected abstract Object[] generateOperands();
    
    @Override
    public Object[] getOperands()
    {
        Object[] result = this.operands;
        if(result == null)
        {
            synchronized(this)
            {
                if(this.operands == null)
                {
                    this.operands = this.generateOperands();
                }
                result = this.operands;
            }
        }
        return result;
    }

    @Override
    public int size()
    {
        Object[] ops = this.getOperands();
        return ops == null ? 0 : ops.length;
    }
}
