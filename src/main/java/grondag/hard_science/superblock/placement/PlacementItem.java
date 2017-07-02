package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public interface PlacementItem
{
    public static final String TAG_ROTATION = "adv_placement_rotation";
    public static final String TAG_FACE = "adv_placement_face";
    public static final String TAG_MODE = "adv_placement_mode";
    
    public abstract ModelState getModelState(ItemStack stack);
    
    /** Face corresponding with the axis and orientation for placement. Not the face on which it is placed. */
    public default void setFace(ItemStack stack, EnumFacing face)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null){
            tag = new NBTTagCompound();
        }
        tag.setInteger(TAG_FACE, face.ordinal());
        stack.setTagCompound(tag);
    }
    
    /** Face corresponding with the axis and orientation for placement. Not the face on which it is placed. */
    public default EnumFacing getFace(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag == null) ? EnumFacing.UP : EnumFacing.values()[tag.getInteger(TAG_FACE)];
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
        tag.setInteger(TAG_ROTATION, rotation.ordinal());
        stack.setTagCompound(tag);
    }

    public default Rotation getRotation(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag == null) ? Rotation.ROTATE_NONE : Rotation.values()[tag.getInteger(TAG_ROTATION)];
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
        tag.setInteger(TAG_MODE, mode.ordinal());
        stack.setTagCompound(tag);
    }
    
    public default PlacementMode getMode(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag == null) ? PlacementMode.STATIC : PlacementMode.values()[tag.getInteger(TAG_MODE)];       
    }
    
    public default void cycleMode(ItemStack stack)
    {
        this.setMode(stack, Useful.nextEnumValue(this.getMode(stack)));
    }
}
