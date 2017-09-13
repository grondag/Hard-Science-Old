package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Rotation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public interface PlacementItem
{

    
    /** Face corresponding with the orthogonalAxis and orientation for placement. Not the face on which it is placed. */
    public default void setFace(ItemStack stack, EnumFacing face)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        tag.setInteger(ModNBTTag.PLACEMENT_FACE, face.ordinal());
        stack.setTagCompound(tag);
    }
    
    /** Face corresponding with the orthogonalAxis and orientation for placement. Not the face on which it is placed. */
    public default EnumFacing getFace(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag == null) ? EnumFacing.UP : EnumFacing.values()[tag.getInteger(ModNBTTag.PLACEMENT_FACE)];
    }
    
    public default void cycleFace(ItemStack stack)
    {
        this.setFace(stack, Useful.nextEnumValue(this.getFace(stack)));
    }
    
    public default void setRotation(ItemStack stack, Rotation rotation)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        tag.setInteger(ModNBTTag.PLACEMENT_ROTATION, rotation.ordinal());
        stack.setTagCompound(tag);
    }

    public default Rotation getRotation(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag == null) ? Rotation.ROTATE_NONE : Rotation.values()[tag.getInteger(ModNBTTag.PLACEMENT_ROTATION)];
    }
    
    public default void cycleRotation(ItemStack stack)
    {
        this.setRotation(stack, Useful.nextEnumValue(this.getRotation(stack)));
    }
    
    public default void setMode(ItemStack stack, PlacementMode mode)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        tag.setInteger(ModNBTTag.PLACEMENT_MODE, mode.ordinal());
        stack.setTagCompound(tag);
    }
    
    public default PlacementMode getMode(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag == null) ? PlacementMode.STATIC : PlacementMode.values()[tag.getInteger(ModNBTTag.PLACEMENT_MODE)];       
    }
    
    public default void cycleMode(ItemStack stack)
    {
        this.setMode(stack, Useful.nextEnumValue(this.getMode(stack)));
    }
}
