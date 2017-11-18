package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.world.PackedBlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Data carrier for fixed region definition to reduce number of
 * methods and method calls for fixed regions.  Fixed regions 
 * can be arbitrarily large and don't have to be cubic - shape
 * depends on interpretation by the placement builder.
 */
public class FixedRegionBounds
{
    public final BlockPos fromPos;
    public final boolean fromIsCentered;
    public final BlockPos toPos;
    public final boolean toIsCentered;
    
    public FixedRegionBounds(BlockPos fromPos, boolean fromIsCentered, BlockPos toPos, boolean toIsCentered)
    {
        this.fromPos = fromPos;
        this.fromIsCentered = fromIsCentered;
        this.toPos = toPos;
        this.toIsCentered = toIsCentered;
    }

    FixedRegionBounds(NBTTagCompound tag)
    {
        final long from = tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS);
        this.fromPos = PackedBlockPos.unpack(from);
        this.fromIsCentered = PackedBlockPos.getExtra(from) == 1;
        final long to = tag.getLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS);
        this.toPos = PackedBlockPos.unpack(to);
        this.toIsCentered = PackedBlockPos.getExtra(to) == 1;
    }

    void saveToNBT(NBTTagCompound tag)
    {
        tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS, PackedBlockPos.pack(this.fromPos, this.fromIsCentered ? 1 : 0));
        tag.setLong(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS, PackedBlockPos.pack(this.toPos, this.toIsCentered ? 1 : 0));
    }
    
    static boolean isPresentInTag(NBTTagCompound tag)
    {
        return tag.hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_START_POS) 
                && tag.hasKey(ModNBTTag.PLACEMENT_FIXED_REGION_END_POS);
    }
}