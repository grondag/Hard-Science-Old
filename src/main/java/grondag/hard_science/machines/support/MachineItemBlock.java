package grondag.hard_science.machines.support;

import javax.annotation.Nullable;

import grondag.hard_science.library.serialization.AbstractSerializer;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MachineItemBlock extends SuperItemBlock
{
    
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
        if(result != null && result.hasKey(AbstractSerializer.NBT_SERVER_SIDE_TAG))
        {
            result = result.copy();
            result.removeTag(AbstractSerializer.NBT_SERVER_SIDE_TAG);
        }
        return result;
    }
}
