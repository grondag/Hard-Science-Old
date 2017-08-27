package grondag.hard_science.machines.support;

import javax.annotation.Nullable;

import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MachineItemBlock extends SuperItemBlock
{
    /**
     * Anything under this tag will not be sent to clients.
     * If your machine only needs a single tag, can use this directly.
     * Otherwise create sub-tags under this tag.
     */
    public static final String NBT_SERVER_SIDE_TAG = "SrvData";
    
    public static final int MAX_DAMAGE = 100;
    
    public static final int CAPACITY_COLOR = 0xFF6080FF;
        
    public MachineItemBlock(MachineBlock block)
    {
        super(block);
        this.setMaxDamage(MAX_DAMAGE);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack)
    {
        return CAPACITY_COLOR;
    }
    

    /**
     * <i>Grondag: don't want to overflow size limits or burden 
     * network by sending details of embedded storage that will 
     * never be used on the client anyway.</i><br><br>
     * 
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        NBTTagCompound result = super.getNBTShareTag(stack);
        if(result != null && result.hasKey(NBT_SERVER_SIDE_TAG))
        {
            result = result.copy();
            result.removeTag(NBT_SERVER_SIDE_TAG);
        }
        return result;
    }
}
