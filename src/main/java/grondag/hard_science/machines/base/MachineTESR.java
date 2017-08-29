package grondag.hard_science.machines.base;

import org.lwjgl.opengl.GL11;

import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.render.TextureHelper;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineTESR extends SuperBlockTESR
{
    public static final MachineTESR INSTANCE = new MachineTESR();
    
    @Override
    public void render(SuperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        /**
         * To see the control face, player has to be in front of it.
         */
        //FIXME: put back
//        switch(te.getCachedModelState().getAxisRotation())
//        {
//        case ROTATE_NONE:
//            if(z <= 0) return;
//            break;
//            
//        case ROTATE_90:
//            if(x >= 0) return;
//            break;
//            
//        case ROTATE_180:
//            if(z >= 0) return;
//            break;
//            
//        case ROTATE_270:
//            if(x <= 0) return;
//            break;
//            
//        default:
//            return;
//        }

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
//        RenderHelper.disableStandardItemLighting();
//        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableLighting();
        
        
//            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        
        MachineTileEntity mte = (MachineTileEntity)te;
        
        // TE will send keepalive packets to server to get updated machine status for rendering
        mte.notifyServerPlayerWatching();
        
        renderControlFace(mte, buffer);
        
        TextureHelper.setTextureClamped(true);

        GlStateManager.disablePolygonOffset();
        GlStateManager.popMatrix();
        // FIXME: see if can avoid this
        GlStateManager.popAttrib();
        
    }
    
    private void renderControlFace(MachineTileEntity te, BufferBuilder buffer)
    {
     
      

//        TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks()
//                .getAtlasSprite(Textures.DECAL_DIAGONAL_BARS.getTextureName(0));
//        
//        final double uMin = texture.getMinU();
//        final double uMax = texture.getMaxU() * 2;
//        final double vMin = texture.getMinV();
//        final double vMax = texture.getMaxV() * 2;
        

//        BlockPos pos = te.getPos();
////        buffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
//        addVertexWithUV(buffer, 1, 0, 0, uMax, vMax, skyLight, blockLight);
//        addVertexWithUV(buffer, 0, 0, 0, uMin, vMax, skyLight, blockLight);
//        addVertexWithUV(buffer, 0, 1, 0, uMin, vMin, skyLight, blockLight);
//        addVertexWithUV(buffer, 1, 1, 0, uMax, vMin, skyLight, blockLight);
        
        
//                TextureHelper.setTextureClamped(false);
        
        // fade in controls as player approaches
        // FIXME: make render distance configurable
        int clampedDistance = Math.min(16, (int) Math.sqrt(te.getLastDistanceSquared()));
        int alpha = clampedDistance < 8 ? 0xFF : 0xFF * (16 - clampedDistance) / 8;
                
        //FIXME: remove
        GlStateManager.disableCull();

        GlStateManager.bindTexture(te.isOn() ? ModModels.TEX_MACHINE_ON : ModModels.TEX_MACHINE_OFF);
        renderControlQuad(buffer, 0.82, 0.82, 0.98, 0.98, 0, 0, 1, 1, alpha, 0xFF, 0xFF, 0xFF);
        Tessellator.getInstance().draw();
        
        if(te.hasRedstonePowerSignal())
        {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
            renderControlQuad(buffer, 0.82, 0.06, 0.98, 0.22, 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMinU(), 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMinV(), 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMaxU(), 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMaxV(), 
                    alpha, 0xFF, 0xFF, 0xFF);
            
            //        buffer.setTranslation(0, 0, 0);
            Tessellator.getInstance().draw();
        }
//
//        GlStateManager.translate(1F, 1F, 0F);
//        float f3 = 0.0075F * 2;
//        GlStateManager.scale(1F, 1F, -1F);
//        GlStateManager.disableLighting();
//        GL11.glNormal3f(1.0F, 1.0F, 1.0F);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
  
//        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
//        fontRenderer.drawString("BB-1047", 0, 0, 0xFFFFFFFF);
//        GlStateManager.disableDepth();
        ModModels.FONT_MONO.drawString(0,  0, "R", 1f, 1f, 0f, 0.5f, 0.5f, 0.5f, 1.0f);
//        RenderHelper.enableStandardItemLighting();
    }
    
    /**
     * Renders a textured quad on the face of the machine.
     * x,y coordinates are 0-1 position on the face.  0,0 is lower left.
     * u,v coordinate are also 0-1 within the currently bound texture.
     * Always full brightness.
     */
    private void renderControlQuad
    (
            BufferBuilder buffer, 
            double xMin, double yMin, double xMax, double yMax, 
            double uMin, double vMin, double uMax, double vMax,
            int alpha, int red, int green, int blue)
    {
        buffer.pos(1-xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(1-xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(1-xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(1-xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        
//        addVertexWithUV(buffer, xMax, yMin, 0, uMax, vMax, skyLight, blockLight);
//        addVertexWithUV(buffer, xMin, yMin, 0, uMin, vMax, skyLight, blockLight);
//        addVertexWithUV(buffer, xMin, yMax, 0, uMin, vMin, skyLight, blockLight);
//        addVertexWithUV(buffer, xMax, yMax, 0, uMax, vMin, skyLight, blockLight);
    }
  
}
