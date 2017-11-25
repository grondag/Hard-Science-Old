package grondag.hard_science.superblock.placement.spec;

import grondag.hard_science.library.serialization.ModNBTTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Parent of placement types that have a regular geometric shape
 * and can be hollow, have a surface, interior, etc.
 */
abstract class VolumetricPlacementSpec extends SingleStackPlacementSpec
{
    protected boolean isHollow;

    protected VolumetricPlacementSpec() {};
    
    protected VolumetricPlacementSpec(PlacementSpecBuilder builder, ItemStack sourceStack)
    {
        super(builder, sourceStack);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.isHollow = tag.getBoolean(ModNBTTag.PLACMENT_IS_HOLLOW);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setBoolean(ModNBTTag.PLACMENT_IS_HOLLOW, this.isHollow);
    }
}