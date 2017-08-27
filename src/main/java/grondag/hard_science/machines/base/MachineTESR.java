package grondag.hard_science.machines.base;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.library.render.PerQuadModelRenderer;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.texture.Textures;
import jline.internal.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineTESR extends SuperBlockTESR
{
    public static final MachineTESR INSTANCE = new MachineTESR();
    
    @Override
    public void render(SuperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if(te != null && MinecraftForgeClient.getRenderPass() == 1)
        {
            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();

            // the .5 is to move origin to block center so that we can rotate to correct facing
            GlStateManager.translate(x + .5f, y + .5f, z + .5f);
            GlStateManager.rotate(te.getCachedModelState().getAxisRotation().degreesInverse, 0, 1, 0);
            // move origin back to block corner
            GlStateManager.translate(-.5f, -.5f, -.5f);

            // not drawing anything with normals, shouldn't need this
            //GlStateManager.disableRescaleNormal();
            
            // prevent z-fighting
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1, -1);
            
            // would expect MC/forget to already have this, but not taking chances
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            
            // no need to disable culling because won't every see controls from the back
//            GlStateManager.disableCull();

            // flat lighting
            RenderHelper.disableStandardItemLighting();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            
            renderControlFace((MachineTileEntity)te, buffer);
            
            GlStateManager.disablePolygonOffset();
            GlStateManager.popMatrix();
            GlStateManager.popAttrib();
        }
    }
    
    private void renderControlFace(MachineTileEntity te, BufferBuilder buffer)
    {
     
       
        final int skyLight = 0x00f0;
        final int blockLight = 0x00f0;

        TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(Textures.DECAL_DIAGONAL_BARS.getTextureName(0));
        
        final double uMin = texture.getMinU();
        final double uMax = texture.getMaxU();
        final double vMin = texture.getMinV();
        final double vMax = texture.getMaxV();

        BlockPos pos = te.getPos();
//        buffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
        addVertexWithUV(buffer, 1, 0, 0, uMax, vMin, skyLight, blockLight);
        addVertexWithUV(buffer, 0, 0, 0, uMin, vMin, skyLight, blockLight);
        addVertexWithUV(buffer, 0, 1, 0, uMin, vMax, skyLight, blockLight);
        addVertexWithUV(buffer, 1, 1, 0, uMax, vMax, skyLight, blockLight);

//        buffer.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();

        RenderHelper.enableStandardItemLighting();
    }
  
}
