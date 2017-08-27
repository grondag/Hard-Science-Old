package grondag.hard_science.machines.base;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.render.TextureHelper;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.texture.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.MinecraftForgeClient;
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
            
//            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            
            
            GlStateManager.bindTexture(ModModels.TEST_TEXTURE.getGlTextureId());
//            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            TextureHelper.setTextureClamped(false);
            
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            
            renderControlFace((MachineTileEntity)te, buffer);
            
            TextureHelper.setTextureClamped(true);

            GlStateManager.disablePolygonOffset();
            GlStateManager.popMatrix();
            // FIXME: see if can avoid this
            GlStateManager.popAttrib();
        }
    }
    
    private void renderControlFace(MachineTileEntity te, BufferBuilder buffer)
    {
     
       
        final int skyLight = 0x00f0;
        final int blockLight = 0x00f0;

//        TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks()
//                .getAtlasSprite(Textures.DECAL_DIAGONAL_BARS.getTextureName(0));
//        
//        final double uMin = texture.getMinU();
//        final double uMax = texture.getMaxU() * 2;
//        final double vMin = texture.getMinV();
//        final double vMax = texture.getMaxV() * 2;
        
        final double uMin = 0;
        final double uMax = 2;
        final double vMin = 0;
        final double vMax = 2;

//        BlockPos pos = te.getPos();
//        buffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
        addVertexWithUV(buffer, 1, 0, 0, uMax, vMax, skyLight, blockLight);
        addVertexWithUV(buffer, 0, 0, 0, uMin, vMax, skyLight, blockLight);
        addVertexWithUV(buffer, 0, 1, 0, uMin, vMin, skyLight, blockLight);
        addVertexWithUV(buffer, 1, 1, 0, uMax, vMin, skyLight, blockLight);

//        buffer.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();

        RenderHelper.enableStandardItemLighting();
    }
  
}
