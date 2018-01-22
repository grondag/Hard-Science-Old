package grondag.hard_science.machines.matbuffer;

import java.util.Arrays;

public class DemandManager
{
    /**
     * 
     */
    private final MaterialBufferManager materialBufferManager;

    private final long[] demands;

    /**
     * @param materialBufferManager
     */
    DemandManager(MaterialBufferManager materialBufferManager)
    {
        this.materialBufferManager = materialBufferManager;
        this.demands = new long[this.materialBufferManager.specs.length];
    }
    
    public void addDemand(int bufferIndex, long demandNanoLiters)
    {
        this.demands[bufferIndex] += demandNanoLiters;
    }
    
    /**
     * Also blames any buffers that can't meet demand.
     */
    public boolean canAllDemandsBeMetAndBlameIfNot()
    {
        boolean isReady = true;
        
        // want to check all so that we can blame any buffer that would hold us up
        for(int i = this.materialBufferManager.specs.length - 1; i >= 0; i--)
        {
            if(this.demands[i] > this.materialBufferManager.getLevelNanoLiters(i))
            {
                isReady = false;
                this.materialBufferManager.blame(i);
            }
        }

        return isReady;
    }
    
    public void clearAllDemand()
    {
        Arrays.fill(demands, 0);
    }
    
    public void consumeAllDemandsAndClear()
    {
        for(int i = this.materialBufferManager.specs.length - 1; i >= 0; i--)
        {
            if(this.demands[i] > 0)
            {
                this.materialBufferManager.use(i, this.demands[i]);
            }
        }
        this.clearAllDemand();
    }

    /**
     * Total of all demands, in nano liters.
     */
    public long totalDemandNanoLiters()
    {
        long result = 0;
        for(int i = this.materialBufferManager.specs.length - 1; i >= 0; i--)
        {
            result += this.demands[i];
        }
        return result;
    }
}