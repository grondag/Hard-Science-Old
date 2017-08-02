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
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.disableRescaleNormal();

        if(te == null)
        {
//            renderItem()
        }
        else
        {
            if(te != null)
            {
                
                // Translate from player look point to the origin
                // Translate from origin to TE coordinates if doing an in-world render
                BlockPos blockpos = te.getPos();
                GlStateManager.translate((float)(x - (double)blockpos.getX()), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ()));

                renderBlock(te);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    
    private void renderBlock(SuperTileEntity te)
    {
        SuperBlock block = (SuperBlock) te.getBlockType();
        if(block.blockRenderMode != BlockRenderMode.TESR) return;
        
        if(MinecraftForgeClient.getRenderPass() == 0)
        {
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
            GlStateManager.disableAlpha();
            renderBlockInner(te, block, false);
            GlStateManager.enableAlpha();
            ForgeHooksClient.setRenderLayer(null);
        }
        else if(MinecraftForgeClient.getRenderPass() == 1)
        {
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.TRANSLUCENT);
            renderBlockInner(te, block, true);
            ForgeHooksClient.setRenderLayer(null);
        }
    }
    
    private void renderBlockInner(SuperTileEntity te, SuperBlock block, boolean translucent)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
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
        
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
       
        
        if (translucent ) 
        {
            
            if(modelState.getRenderPassSet().renderLayout.containsBlockRenderLayer(BlockRenderLayer.TRANSLUCENT))
            {
                PerQuadModelRenderer.INSTANCE.renderModel(world, this.tesrDelegate, state, te.getPos(), bufferBuilder, true, 0L);

                bufferBuilder.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                        (float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);
            }
        }
        else
        {
            if(modelState.getRenderPassSet().renderLayout.containsBlockRenderLayer(BlockRenderLayer.SOLID))
            {
                PerQuadModelRenderer.INSTANCE.renderModel(world, this.tesrDelegate, state, te.getPos(), bufferBuilder, true, 0L);
            }
        }
        
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
    }
    
    @Override
    public void renderTileEntityFast(SuperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial,
            BufferBuilder buffer)
    {
        // TODO Auto-generated method stub
        super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer);
    }
}
