package grondag.hard_science.superblock.block;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.render.PerQuadModelRenderer;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.SuperDispatcher.DispatchDelegate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SuperBlockTESR extends TileEntitySpecialRenderer<SuperTileEntity>
{
    public static final SuperBlockTESR INSTANCE = new SuperBlockTESR();
    
    private final DispatchDelegate tesrDelegate = ModModels.MODEL_DISPATCH.delegates[BlockRenderMode.TESR.ordinal()];
    
    @Override
    public void render(SuperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if(te != null)
        {
            
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            BlockPos pos = te.getPos();
            buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
            renderBlock(te, buffer);
            buffer.setTranslation(0, 0, 0);
        }
    }

    
    protected void renderBlock(SuperTileEntity te, BufferBuilder buffer)
    {
        SuperBlock block = (SuperBlock) te.getBlockType();
        if(block.blockRenderMode != BlockRenderMode.TESR) return;
        
        if(MinecraftForgeClient.getRenderPass() == 0)
        {
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
            
            // FIXME: only do this when texture demands it and use FastTESR other times
            GlStateManager.disableAlpha();
            renderBlockInner(te, block, false, buffer);
            GlStateManager.enableAlpha();
            ForgeHooksClient.setRenderLayer(null);
        }
        else if(MinecraftForgeClient.getRenderPass() == 1)
        {
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.TRANSLUCENT);
            renderBlockInner(te, block, true, buffer);
            ForgeHooksClient.setRenderLayer(null);
        }
    }
    
    protected void renderBlockInner(SuperTileEntity te, SuperBlock block, boolean translucent, BufferBuilder buffer)
    {
      
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        if(translucent)
        {
            GlStateManager.disableCull();
        }

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        
      
        World world = te.getWorld();
        ModelState modelState = te.getCachedModelState();
        IBlockState state = ((IExtendedBlockState)world.getBlockState(te.getPos())).withProperty(SuperBlock.MODEL_STATE,  modelState);
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
       
        
        if (translucent ) 
        {
            
            if(modelState.getRenderPassSet().renderLayout.containsBlockRenderLayer(BlockRenderLayer.TRANSLUCENT))
            {
                PerQuadModelRenderer.INSTANCE.renderModel(world, this.tesrDelegate, state, te.getPos(), buffer, true, 0L);

                // FIXME: do this if TESR?
                buffer.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                        (float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);
            }
        }
        else
        {
            if(modelState.getRenderPassSet().renderLayout.containsBlockRenderLayer(BlockRenderLayer.SOLID))
            {
                PerQuadModelRenderer.INSTANCE.renderModel(world, this.tesrDelegate, state, te.getPos(), buffer, true, 0L);
            }
        }
        
        Tessellator.getInstance().draw();

        RenderHelper.enableStandardItemLighting();
    }
}
