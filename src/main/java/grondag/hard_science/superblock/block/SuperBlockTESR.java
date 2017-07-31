package grondag.hard_science.superblock.block;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.RenderMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
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
        if(!block.renderModeSet.hasTESR) return;
        
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
        IBlockState state = ((IExtendedBlockState)world.getBlockState(te.getPos())).withProperty(SuperBlock.MODEL_STATE,  te.getCachedModelState());
        
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
     
        
        if (translucent ) 
        {
            
            //FIXME: order of these is borked if doesn't coincide with layer order
            // has to be base/lamp/cut, followed by MIDDLE then OUTER.
            // If all are translucent, could be TESR, SHADED, TESR
            // or SHADED TESER SHADED
            // or TESR SHADED  or SHADED TESR
            // or TESR or SHADED
            
            // given that models are rendered in block  layer  OR in TESR,
            // can have container set up to be SOLID/TRANSLUCENT  or PASS 0 / PASS 1A / PASS 1B / PASS 1C
            if(block.renderModeSet.includes(RenderMode.TRANSLUCENT_TESR))
            {
                dispatcher.getBlockModelRenderer().renderModelFlat(world, ModModels.MODEL_DISPATCH.delegate_tesr, state, te.getPos(), bufferBuilder, true, 0L);
            }
            if(block.renderModeSet.includes(RenderMode.TRANSLUCENT_SHADED))
            {
                dispatcher.getBlockModelRenderer().renderModelSmooth(world, ModModels.MODEL_DISPATCH.delegate_block, state, te.getPos(), bufferBuilder, true, 0L);
            }

            bufferBuilder.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                    (float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);
        }
        else
        {
            if(block.renderModeSet.includes(RenderMode.SOLID_SHADED))
            {
                dispatcher.getBlockModelRenderer().renderModelSmooth(world, ModModels.MODEL_DISPATCH.delegate_block, state, te.getPos(), bufferBuilder, true, 0L);
            }
            if(block.renderModeSet.includes(RenderMode.SOLID_TESR))
            {
                dispatcher.getBlockModelRenderer().renderModelFlat(world, ModModels.MODEL_DISPATCH.delegate_tesr, state, te.getPos(), bufferBuilder, true, 0L);
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
