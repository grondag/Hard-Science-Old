package grondag.hard_science.materials;

import java.util.List;

import grondag.hard_science.HardScience;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class MatterCube extends Item
{
    /** number of times a 1M block was divided to get this cube */
    public final Matter matter;
    public final CubeSize cubeSize;
    
    public MatterCube(Matter matter, CubeSize cubeSize)
    {
        super();
        this.matter = matter;
        this.cubeSize = cubeSize;
        this.setCreativeTab(HardScience.tabMod);
        String name = matter.name().toLowerCase() + "_cube_" + cubeSize.divisionLevel;
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return I18n.translateToLocal("matter." + this.matter.name().toLowerCase()).trim();
    }
    
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(this.cubeSize.toolTip());
        tooltip.add(this.matter.packageType.toolTip());
        
    }
    
    
}
