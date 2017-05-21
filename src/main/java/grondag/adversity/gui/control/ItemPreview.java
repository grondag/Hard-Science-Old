package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.base.GuiControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public class ItemPreview extends GuiControl
{
    public ItemStack previewItem;
    
    private double contentLeft;
    private double contentTop;
    private double contentScale;
  
    @Override
    public void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        if(this.previewItem != null)
        {
            GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, this.previewItem, this.contentLeft, this.contentTop, this.contentScale);
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        double contentSize = Math.min(this.width, this.height);
        this.contentLeft = this.left + (this.width - contentSize) / 2;
        this.contentTop = this.top + (this.height - contentSize) / 2;
        this.contentScale = contentSize / 15;
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

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }
    
}
