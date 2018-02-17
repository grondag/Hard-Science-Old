package grondag.hard_science.matter;

import java.util.List;

import grondag.hard_science.HardScience;
import grondag.hard_science.matter.MassUnits.Mass;
import grondag.hard_science.matter.VolumeUnits.Volume;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

/**
 * Package sizes for solid material blocks
 * and fluid containers. 
 */
public class MatterCube2 extends Item
{
    public final long nanoLiters;
    public final long nanoGrams;
    public final Matter matter;
    
    public MatterCube2(Matter matter, Volume volume)
    {
        super();
        this.matter = matter;
        this.nanoLiters = volume.nanoLiters();
        this.nanoGrams = (long) (matter.density() * this.nanoLiters);
        this.setCreativeTab(HardScience.tabMod);
        String name = matter.systemName() 
                + "_cube_" 
                + volume.unit().name().toLowerCase()
                + volume.quantity;
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
    }
    
    public MatterCube2(Matter matter, Mass mass)
    {
        super();
        this.matter = matter;
        this.nanoGrams = mass.nanoGrams();
        this.nanoLiters = (long) (this.nanoGrams / matter.density());
        this.setCreativeTab(HardScience.tabMod);
        String name = matter.systemName() 
                + "_cube_" 
                + mass.unit().name().toLowerCase()
                + mass.quantity;
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return I18n.translateToLocal("matter." + this.matter.systemName().toLowerCase()).trim();
    }
    
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(this.matter.phase() == MatterPhase.SOLID)
            tooltip.add(MassUnits.formatMass(this.nanoGrams, false));
        else
            tooltip.add(VolumeUnits.formatVolume(this.nanoLiters, false));
        
    }
    
    
}
