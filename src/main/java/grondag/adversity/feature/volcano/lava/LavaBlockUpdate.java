package grondag.adversity.feature.volcano.lava;

import net.minecraft.util.math.BlockPos;

public class LavaBlockUpdate
{
    public final BlockPos pos;
    public final float level;
    
    public LavaBlockUpdate(BlockPos pos, float level)
    {
        this.pos = pos;
        this.level = level;
    }
}
