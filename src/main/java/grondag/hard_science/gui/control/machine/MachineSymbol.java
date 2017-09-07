package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;

public class MachineSymbol extends AbstractMachineControl<MachineSymbol>
{

    public MachineSymbol(MachineTileEntity tileEntity)
    {
        super(tileEntity);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderTextureInBounds(this.renderBounds, this.tileEntity.getSymbolGlTextureId(), 255);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // NOOP
        
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawToolTip(this.tileEntity.getBlockType().getLocalizedName(), mouseX, mouseY);
    }

}
