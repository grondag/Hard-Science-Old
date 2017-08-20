package grondag.hard_science.gui.control;

import grondag.hard_science.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemPreview extends GuiControl
{
    public ItemStack previewItem;
    
    private double contentLeft;
    private double contentTop;
    private double contentSize;
  
    @Override
    public void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        if(this.previewItem != null)
        {
            GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, this.previewItem, this.contentLeft, this.contentTop, this.contentSize);
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.contentSize = Math.min(this.width, this.height);
        this.contentLeft = this.left + (this.width - contentSize) / 2;
        this.contentTop = this.top + (this.height - contentSize) / 2;
    }

    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // nothing special
    }
    
    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // nothing special
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }
    
}
