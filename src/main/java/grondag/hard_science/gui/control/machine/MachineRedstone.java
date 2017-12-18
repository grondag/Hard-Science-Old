package grondag.hard_science.gui.control.machine;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.control.machine.RenderBounds.RadialRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineRedstone extends AbstractMachineControl<MachineRedstone, RadialRenderBounds>
{
    private final Tessellator tesselator;
    private final BufferBuilder buffer;
    
    public MachineRedstone(MachineTileEntity te, RadialRenderBounds bounds)
    {
        super(te, bounds);
        this.tesselator = Tessellator.getInstance();
        this.buffer = tesselator.getBuffer();
    }
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.isMouseOver(mouseX, mouseY))
        {
            GuiUtil.drawBoxRightBottom(this.getLeft(), this.getTop(), this.getRight(), this.getBottom(), 1, BUTTON_COLOR_FOCUS);
        }
        MachineControlRenderer.renderRedstoneControl(this.tileEntity, this.tesselator, this.buffer, this.renderBounds, 255);
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        this.tileEntity.toggleRedstoneControl(null);
        GuiUtil.playPressedSound(mc);
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawLocalizedToolTip(mouseX, mouseY,
                this.tileEntity.clientState().isRedstoneControlEnabled() ? "machine.redstone_enabled" : "machine.redstone_disabled",
                this.tileEntity.clientState().statusState.hasRedstonePower() ? "machine.redstone_live" : "machine.redstone_dead" );
    }
    
}
