package grondag.hard_science.virtualblock;

import java.util.List;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


//FIXME: experimental - remove

@SideOnly(Side.CLIENT)
public class VirtualBlockTESR extends TileEntitySpecialRenderer<VirtualBlockTileEntity>
{
    public static final VirtualBlockTESR INSTANCE = new VirtualBlockTESR();
    
    @Override
    public void render(VirtualBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        // Translate from player look point to the origin
        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();
        
     
        
        // Render the block
        if(te == null)
        {
//            renderItem()
        }
        else
        {
            renderBlock(te);
        }


        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void renderItem()
    {
        
    }
    
    private void renderBlock(VirtualBlockTileEntity te)
    {
        RenderHelper.disableStandardItemLighting();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
     // Translate from origin to TE coordinates if doing an in-world render
        if(te != null)
        {
            GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
        }

        World world = te.getWorld();
        SuperBlock block = (SuperBlock) ModBlocks.basalt_cobble;
        IBlockState state = ModBlocks.basalt_cobble.getExtendedState(block.getDefaultState(), world, te.getPos());
        IBakedModel model = ModModels.MODEL_DISPATCH.delegates[BlockRenderMode.TESR.ordinal()];
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        
        BlockRenderLayer saveLayer = MinecraftForgeClient.getRenderLayer();
        
        for (BlockRenderLayer layer : BlockRenderLayer.values()) 
        {
            if(block.canRenderInLayer(state, layer))
            {
                ForgeHooksClient.setRenderLayer(layer);
                dispatcher.getBlockModelRenderer().renderModel(world, model, state, te.getPos(), bufferBuilder, true);
            }
        }
        ForgeHooksClient.setRenderLayer(saveLayer);
        
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
    }
    
    @Override
    public void renderTileEntityFast(VirtualBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial,
            BufferBuilder buffer)
    {
        // TODO Auto-generated method stub
        super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer);
    }
    
    @SuppressWarnings("unused")
    private void renderModel(IBakedModel model, int color, ItemStack stack)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            this.renderQuads(bufferbuilder, model.getQuads((IBlockState)null, enumfacing, 0L), color, stack);
        }

        this.renderQuads(bufferbuilder, model.getQuads((IBlockState)null, (EnumFacing)null, 0L), color, stack);
        tessellator.draw();
    }
    

    private void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack)
    {
        boolean flag = color == -1 && !stack.isEmpty();
        int i = 0;

        for (int j = quads.size(); i < j; ++i)
        {
            BakedQuad bakedquad = quads.get(i);
            int k = color;

            if (flag && bakedquad.hasTintIndex())
            {
                k = Color.WHITE;

                if (EntityRenderer.anaglyphEnable)
                {
                    k = TextureUtil.anaglyphColor(k);
                }

                k = k | -16777216;
            }

            net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(renderer, bakedquad, k);
        }
    }
}
