package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public interface PlacementItem
{
    /** Face corresponding with the orthogonalAxis and orientation for placement. Not the face on which it is placed. */
    public static void setFace(ItemStack stack, EnumFacing face)
    {
        Useful.saveEnumToTag(Useful.getOrCreateTagCompound(stack), ModNBTTag.PLACEMENT_FACE, face);
    }
    
    /** Face corresponding with the orthogonalAxis and orientation for placement. Not the face on which it is placed. */
    public static EnumFacing getFace(ItemStack stack)
    {
        return Useful.safeEnumFromTag(stack.getTagCompound(), ModNBTTag.PLACEMENT_FACE, EnumFacing.UP);
    }
    
    public static void cycleFace(ItemStack stack)
    {
        setFace(stack, Useful.nextEnumValue(getFace(stack)));
    }
    
    public static void setRotation(ItemStack stack, Rotation rotation)
    {
        Useful.saveEnumToTag(Useful.getOrCreateTagCompound(stack), ModNBTTag.PLACEMENT_ROTATION, rotation);
    }

    public static Rotation getRotation(ItemStack stack)
    {
        return Useful.safeEnumFromTag(stack.getTagCompound(), ModNBTTag.PLACEMENT_ROTATION, Rotation.ROTATE_NONE);
    }
    
    public static void cycleRotation(ItemStack stack)
    {
        setRotation(stack, Useful.nextEnumValue(getRotation(stack)));
    }
    
    public static void setMode(ItemStack stack, PlacementMode mode)
    {
        Useful.saveEnumToTag(Useful.getOrCreateTagCompound(stack), ModNBTTag.PLACEMENT_MODE, mode);
    }
    
    public static PlacementMode getMode(ItemStack stack)
    {
        return Useful.safeEnumFromTag(stack.getTagCompound(), ModNBTTag.PLACEMENT_MODE,  PlacementMode.FACE);
    }
    
    public static void cycleMode(ItemStack stack)
    {
        setMode(stack, Useful.nextEnumValue(getMode(stack)));
    }
    
   
    public static void setStackLightValue(ItemStack stack, int lightValue)
    {
        Useful.getOrCreateTagCompound(stack).setByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE, (byte)lightValue);
    }
    
    public static byte getStackLightValue(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getByte(ModNBTTag.SUPER_MODEL_LIGHT_VALUE);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance)
    {
        Useful.saveEnumToTag(Useful.getOrCreateTagCompound(stack), ModNBTTag.SUPER_MODEL_SUBSTANCE, substance);
    }
    
    public static BlockSubstance getStackSubstance(ItemStack stack)
    {
        return Useful.safeEnumFromTag(stack.getTagCompound(), ModNBTTag.SUPER_MODEL_SUBSTANCE,  BlockSubstance.FLEXSTONE);
    }
    
    public static ModelState getStackModelState(ItemStack stack)
    {
        ModelState stackState = ModelState.deserializeFromNBTIfPresent(stack.getTagCompound());
        
        //WAILA or other mods might create a stack with no NBT
        if(stackState != null) return stackState;
        
        if(stack.getItem() instanceof SuperItemBlock)
        {
            return ((SuperBlock)((SuperItemBlock)stack.getItem()).getBlock()).getDefaultModelState();
        }
        
        return null;
    }
    
    public static void setStackModelState(ItemStack stack, ModelState modelState)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(modelState == null)
        {
            if(tag != null) tag.removeTag(ModNBTTag.MODEL_STATE);
            return;
        }
        
        if(tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        
        modelState.serializeNBT(tag);
    }
    
    public static boolean isPlacementItem(ItemStack stack)
    {
        return stack.getItem() instanceof PlacementItem;
    }
}
