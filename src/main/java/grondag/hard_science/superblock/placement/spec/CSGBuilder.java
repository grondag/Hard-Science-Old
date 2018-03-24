package grondag.hard_science.superblock.placement.spec;

import grondag.exotic_matter.placement.PlacementPreviewRenderMode;
import grondag.exotic_matter.simulator.IWorldTask;
import grondag.exotic_matter.world.IBlockRegion;
import grondag.hard_science.superblock.blockmovetest.PlacementPosition;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CSGBuilder extends VolumetricBuilder
{
    public CSGBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate()
    {
        // TODO: Logic will be similar to VolumetricBuilder
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
    {
        // TODO Auto-generated method stub

    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
    {
        // TODO Auto-generated method stub

    }
    
    @Override
    public IWorldTask worldTask(EntityPlayerMP player)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IBlockRegion region()
    {
        // TODO Auto-generated method stub
        return null;
    }
}