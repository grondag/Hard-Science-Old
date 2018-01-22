package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.library.serialization.ModNBTTag;

public class VolumetricBufferSpec
{
    public final VolumetricIngredientList inputs;
    public final long maxCapacityNanoLiters;
    
    /**
     * Level at which no more inputs can be accepted.
     */
    public final long fillLineNanoLiters;
    
    public final String nbtTag;
    
    public final String tooltipKey;
    
    public VolumetricBufferSpec(VolumetricIngredientList inputs, long maxCapacityNanoLiters, String nbtKey, String tooltipKey)
    {
        // would be strange, but whatever...  sometime I do strange things.
        if(maxCapacityNanoLiters < 1) maxCapacityNanoLiters = 1;
        
        this.inputs = inputs;
        this.maxCapacityNanoLiters = maxCapacityNanoLiters;
        this.fillLineNanoLiters = this.maxCapacityNanoLiters - inputs.minNanoLitersPerItem + 1;
        this.nbtTag = nbtKey;
        this.tooltipKey = "machine.buffer_" + tooltipKey;
    }
    
    public boolean isHDPE()
    {
        return this.nbtTag == ModNBTTag.MATERIAL_HDPE;
    }
}