package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.base.GuiControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public class ItemPreview extends GuiControl
{
    public ItemStack previewItem;

    public ItemPreview(double left, double top, double size)
    {
        super(left, top, size, size);
    }
    
    public void resize(double left, double top, double size)
    {
        this.resize(left, top, size, size);
    }

    @Override
    public void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        double scale = this.width / 15;
        if(this.previewItem != null)
        {
            GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, this.previewItem, this.left, this.top, scale);
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        // nothing special
    }

    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        // nothing special
    }
    
    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
    {
        // nothing special
    }
}
