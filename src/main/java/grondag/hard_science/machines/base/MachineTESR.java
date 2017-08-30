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
        switch(te.getCachedModelState().getAxisRotation())
        {
        case ROTATE_NONE:
            if(z <= 0) 
                return;
            break;
            
        case ROTATE_90:
            if(x >= 0) 
                return;
            break;
            
        case ROTATE_180:
            if(z >= 0) 
                return;
            break;
            
        case ROTATE_270:
            if(x <= 0) 
                return;
            break;
            
        default:
            return;
        }

        MachineTileEntity mte = (MachineTileEntity)te;
        
        // TE will send keepalive packets to server to get updated machine status for rendering
        mte.notifyServerPlayerWatching();
        
        GlStateManager.pushMatrix();

        // the .5 is to move origin to block center so that we can rotate to correct facing
        GlStateManager.translate(x + .5f, y + .5f, z + .5f);
        GlStateManager.rotate(te.getCachedModelState().getAxisRotation().degreesInverse, 0, 1, 0);
        // move origin back to upper left corner to match GUI semantics
        GlStateManager.translate(0.5f, 0.5f, -.5f);
        GlStateManager.scale(-1.0f, -1.0f, 1.0f);

        // prevent z-fighting
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);
        
        // would expect MC/forget to already have this, but not taking chances
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        
        
//            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        
        renderControlFace(mte, buffer);
        
//        TextureHelper.setTextureClamped(true);

        GlStateManager.disablePolygonOffset();
        GlStateManager.popMatrix();

        
    }
    
    private void renderControlFace(MachineTileEntity te, BufferBuilder buffer)
    {
 
        
        // fade in controls as player approaches
        // FIXME: make render distance configurable
        int clampedDistance = Math.min(32, (int) Math.sqrt(te.getLastDistanceSquared()));
        int alpha = clampedDistance < 16 ? 0xFF : 0xFF * (32 - clampedDistance) / 16;

        ModModels.FONT_ORBITRON.drawLine(0.2f, 0.06f, te.machineName(), 0.12f, 0f, 255, 255, 255, alpha);   

        GlStateManager.bindTexture(te.isOn() ? ModModels.TEX_MACHINE_ON : ModModels.TEX_MACHINE_OFF);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        renderControlQuad(buffer, 0.82, 0.02, 0.98, 0.18, 0, 0, 1, 1, alpha, 0xFF, 0xFF, 0xFF);
        Tessellator.getInstance().draw();
        
        if(te.hasRedstonePowerSignal())
        {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            GlStateManager.bindTexture(ModModels.TEX_BLOCKS);
            renderControlQuad(buffer, 0.82, 0.78, 0.98, 0.94, 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMinU(), 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMinV(), 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMaxU(), 
                    ModModels.SPRITE_REDSTONE_TORCH_LIT.getMaxV(), 
                    alpha, 0xFF, 0xFF, 0xFF);
            Tessellator.getInstance().draw();
        }

  
    }
    
    /**
     * Renders a textured quad on the face of the machine.
     * x,y coordinates are 0-1 position on the face.  0,0 is upper left.
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
        buffer.pos(xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).lightmap(0x00f0, 0x00f0).endVertex();
        buffer.pos(xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMin).lightmap(0x00f0, 0x00f0).endVertex();
    }
  
}
