package grondag.hard_science.machines.support;

import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.superblock.items.SuperItemBlock;
import net.minecraft.item.ItemStack;

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
    
}
