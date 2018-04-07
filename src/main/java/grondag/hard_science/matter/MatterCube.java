package grondag.hard_science.matter;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.init.IItemModelRegistrant;
import grondag.hard_science.HardScience;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;

/**
 * Package sizes for solid material blocks
 * and fluid containers. 
 */
public class MatterCube extends Item implements IItemModelRegistrant
{
    /** number of times a 1M block was divided to get this cube */
    public final MatterPackaging matter;
    public final CubeSize cubeSize;
    
    public MatterCube(MatterPackaging matter, CubeSize cubeSize)
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
    public String getItemStackDisplayName(@Nonnull ItemStack stack)
    {
        return I18n.translateToLocal("matter." + this.matter.name().toLowerCase()).trim();
    }
    
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(this.cubeSize.toolTip());
        tooltip.add(this.matter.packageType.toolTip());
        
    }

    @Override
    public void handleBake(ModelBakeEvent event)
    {
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "inventory"),
                new MatterCubeItemModel(this));        
    }
    
    
}
