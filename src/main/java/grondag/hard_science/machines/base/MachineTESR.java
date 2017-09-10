package grondag.hard_science.machines.base;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.library.varia.HorizontalAlignment;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.superblock.block.SuperBlockTESR;
import grondag.hard_science.superblock.block.SuperTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class MachineTESR extends SuperBlockTESR
{
    
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
        
        //FIXME - don't call if not displaying anything than can change
        mte.notifyServerPlayerWatching();
        
        GlStateManager.pushMatrix();

        // the .5 is to move origin to block center so that we can rotate to correct facing
        GlStateManager.translate(x + .5f, y + .5f, z + .5f);
        GlStateManager.rotate(te.getCachedModelState().getAxisRotation().degreesInverse, 0, 1, 0);
        // move origin back to upper left corner to match GUI semantics
        GlStateManager.translate(0.5f, 0.5f, -.5f);
        GlStateManager.scale(-1.0f, -1.0f, 1.0f);

        MachineControlRenderer.setupMachineRendering();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // fade in controls as player approaches
        // FIXME: make dropoff distance (8) and dropoff depth (2) configurable
        int displayAlpha = (int)(alpha * (Useful.clamp(0.0, 1.0, 1 - (Math.sqrt(mte.getLastDistanceSquared()) - 8) / 2) * 255));
  
        MachineControlRenderer.renderMachineText(tessellator, buffer, MachineControlRenderer.BOUNDS_NAME, mte.machineName(), HorizontalAlignment.CENTER, displayAlpha);
        MachineControlRenderer.renderTextureInBounds(tessellator, buffer, MachineControlRenderer.BOUNDS_SYMBOL, mte.getSymbolGlTextureId(), displayAlpha);

        if(mte.hasOnOff())
        {
            MachineControlRenderer.renderBinaryTexture(tessellator, buffer, MachineControlRenderer.BOUNDS_ON_OFF, ModModels.TEX_MACHINE_ON_OFF, mte.isOn(), displayAlpha);
        }
        
        if(mte.hasRedstoneControl())
        {
            MachineControlRenderer.renderRedstoneControl(mte, tessellator, buffer, MachineControlRenderer.BOUNDS_REDSTONE, displayAlpha);
        }
        
        renderControlFace(tessellator, buffer, mte, displayAlpha);
        
//        TextureHelper.setTextureClamped(true);

        MachineControlRenderer.restoreWorldRendering();
        GlStateManager.popMatrix();

        
    }
    
    protected abstract void renderControlFace(Tessellator tessellator, BufferBuilder buffer, MachineTileEntity te, int alpha);
   
  
}
