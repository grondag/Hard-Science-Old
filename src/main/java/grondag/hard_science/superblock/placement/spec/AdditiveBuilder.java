package grondag.hard_science.superblock.placement.spec;

import grondag.exotic_matter.placement.PlacementPosition;
import grondag.exotic_matter.placement.PlacementPreviewRenderMode;
import grondag.exotic_matter.simulator.IWorldTask;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AdditiveBuilder extends SurfaceBuilder
{
    public AdditiveBuilder(ItemStack placedStack, EntityPlayer player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate()
    {
        // TODO Auto-generated method stub
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

}