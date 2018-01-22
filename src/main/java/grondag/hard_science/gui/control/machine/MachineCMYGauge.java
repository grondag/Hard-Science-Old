package grondag.hard_science.gui.control.machine;

import grondag.hard_science.Log;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.matbuffer.MaterialBufferDelegate;
import grondag.hard_science.machines.matbuffer.MaterialBufferManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class MachineCMYGauge extends AbstractMachineControl<MachineCMYGauge, RadialRenderBounds>
{
    private final MaterialBufferDelegate cyan;
    private final MaterialBufferDelegate magenta;
    private final MaterialBufferDelegate yellow;
    
    public MachineCMYGauge(MachineTileEntity tileEntity, int cyanIndex, int magentaIndex, int yellowIndex, RadialRenderBounds spec)
    {
        super(tileEntity, spec);
        MaterialBufferManager mbm = tileEntity.clientState().bufferManager;
        if(mbm == null || cyanIndex >= mbm.bufferCount() || magentaIndex >= mbm.bufferCount() || yellowIndex >= mbm.bufferCount()) 
        {
            Log.warn("Machine CMY gauge GUI could not be initialized.  Bad buffer of buffer index.");
            this.cyan = null;
            this.magenta= null;
            this.yellow = null;
        }
        else
        {
            this.cyan = mbm.getBuffer(cyanIndex);
            this.magenta = mbm.getBuffer(magentaIndex);
            this.yellow = mbm.getBuffer(yellowIndex);
        }
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        super.handleCoordinateUpdate();
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawLocalizedToolTip(mouseX, mouseY, 
                I18n.translateToLocal(cyan.tooltipKey()) + String.format(": %,.9fL", cyan.getLevelNanoLiters() / 1000000000.0),
                I18n.translateToLocal(magenta.tooltipKey()) + String.format(": %,.9fL", magenta.getLevelNanoLiters() / 1000000000.0),
                I18n.translateToLocal(yellow.tooltipKey()) + String.format(": %,.9fL", yellow.getLevelNanoLiters() / 1000000000.0));
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderCMY(this.renderBounds, cyan, magenta, yellow, 0xFF);    
    }
    
    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
    }

}
