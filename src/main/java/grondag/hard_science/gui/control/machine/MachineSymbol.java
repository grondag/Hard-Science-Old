package grondag.hard_science.gui.control.machine;

import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;

public class MachineSymbol extends AbstractMachineControl<MachineSymbol, RadialRenderBounds>
{

    public MachineSymbol(MachineTileEntity tileEntity, RadialRenderBounds bounds)
    {
        super(tileEntity, bounds);
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        MachineControlRenderer.renderSpriteInBounds(this.renderBounds, this.tileEntity.getSymbolSprite(), 0xFFFFFFFF, Rotation.ROTATE_NONE);
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
