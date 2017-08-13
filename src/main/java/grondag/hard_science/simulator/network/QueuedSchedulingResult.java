package grondag.hard_science.simulator.network;

import grondag.hard_science.Log;

/**
 * Intended for use as a child class within queued transport providers
 */
public abstract class QueuedSchedulingResult extends AbstractScheduledTransportResult
{
    IScheduleCallback callback;

    @Override
    public long unitsInput()
    {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public long unitsOutput()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean start()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean cancel()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean requestCallback(IScheduleCallback callback)
    {
        if(this.callback == null)
        {
            this.callback = callback;
            return true; 
        }
        else
        {
            Log.warn("Transport schedule callback requested when callback already exists.  This is a bug.");
            return false;
        }
    };
    
    @Override
    protected void doStatusCallback()
    {
        if(this.callback != null) this.callback.updateStatus(this);
    }

}
