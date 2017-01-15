package grondag.adversity.feature.volcano.lava;


import net.minecraft.util.math.BlockPos;

public class AgedBlockPos //implements Comparable<AgedBlockPos>
{
//    private static int nextSequenceId = 0;
    
    private int tick;
    public final BlockPos pos;
//    private int sequenceID;
    
    public AgedBlockPos(BlockPos pos, int tick)
    {
        this.pos = pos;
        this.tick = tick;
//        this.resequence();
    }
    
//    /** 
//     * Updates internal sequence id to force this to the end of sort order within the same tick age.
//     */
//    public void resequence()
//    {
//        this.sequenceID = nextSequenceId++;
//    }
    
    public void setTick(int tickIn)
    {
        this.tick = tickIn;
    }

    public int getTick()
    {
        return this.tick;
    }
    
//    @Override
//    public int compareTo(AgedBlockPos other)
//    {
//        return ComparisonChain.start()
//                .compare(this.tick, other.tick)
//                .compare(this.sequenceID, other.sequenceID)
//                .result();
//    }
}
