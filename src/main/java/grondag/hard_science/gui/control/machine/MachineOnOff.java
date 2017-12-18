package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineOnOff extends AbstractMachineControl<MachineOnOff, RadialRenderBounds>
{
    
    public MachineOnOff(MachineTileEntity te, RadialRenderBounds bounds)
    {
        super(te, bounds);
    }
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.isMouseOver(mouseX, mouseY))
        {
            GuiUtil.drawBoxRightBottom(this.getLeft(), this.getTop(), this.getRight(), this.getBottom(), 1, BUTTON_COLOR_FOCUS);
        }
        MachineControlRenderer.renderBinarySprite(this.renderBounds, ModModels.TEX_MACHINE_ON_OFF, this.tileEntity.clientState().isOn(), 0xFFFFFFFF);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        this.tileEntity.togglePower(null);
        GuiUtil.playPressedSound(mc);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawLocalizedToolTipBoolean(this.tileEntity.clientState().isOn(), "machine.is_on", "machine.is_off", mouseX, mouseY);
    }
    
}
