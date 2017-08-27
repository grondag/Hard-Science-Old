package grondag.hard_science.machines.base;

import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import jline.internal.Log;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineTESR extends SuperBlockTESR
{
    public static final MachineTESR INSTANCE = new MachineTESR();
    
    @Override
    protected void renderBlock(SuperTileEntity te, BufferBuilder buffer)
    {
//        super.renderBlock(te, buffer);
        
//        SuperBlock block = (SuperBlock) te.getBlockType();
//        if(block.blockRenderMode != BlockRenderMode.TESR) return;
        
//        if(MinecraftForgeClient.getRenderPass() == 0)
//        {
//            ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
//            
//            // FIXME: only do this when texture demands it and use FastTESR other times
//            GlStateManager.disableAlpha();
//            renderBlockInner(te, block, false, buffer);
//            GlStateManager.enableAlpha();
//            ForgeHooksClient.setRenderLayer(null);
//        }
//        else 
            if(MinecraftForgeClient.getRenderPass() == 1)
        {
//                Log.info("booP");
//            ForgeHooksClient.setRenderLayer(BlockRenderLayer.TRANSLUCENT);
//            renderBlockInner(te, block, true, buffer);
//            ForgeHooksClient.setRenderLayer(null);
        }
    }
  
}
