package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.GuiUtil.HorizontalAlignment;
import grondag.adversity.gui.GuiUtil.VerticalAlignment;
import grondag.adversity.gui.base.GuiControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;

public class Toggle extends GuiControl
{

    private boolean isOn = false;
    private String label  = "unlabedl toggle";
    
    private int targetAreaTop;
    private int targetAreaBottom;
    private int labelWidth;
    private int labelHeight;
    
    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        float boxRight = (float) (this.left + this.labelHeight);
        
        GuiUtil.drawBox(this.left, this.targetAreaTop, boxRight, this.targetAreaBottom, 1, this.isMouseOver(mouseX, mouseY) ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_ACTIVE);
        
        if(this.isOn)
        {
            GuiUtil.drawRect(this.left + 2, this.targetAreaTop + 2, boxRight - 2, this.targetAreaBottom - 2, BUTTON_COLOR_ACTIVE);
        }
        
        GuiUtil.drawAlignedStringNoShadow(mc.fontRenderer, this.label, boxRight + CONTROL_INTERNAL_MARGIN, this.targetAreaTop, 
                this.labelWidth, this.labelHeight, TEXT_COLOR_LABEL, HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        int fontHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        this.targetAreaTop = (int) Math.max(this.top, this.top + (this.height - fontHeight) / 2);
        this.targetAreaBottom = (int) Math.min(this.bottom, this.targetAreaTop + fontHeight);
        this.labelHeight = fontHeight;
        this.labelWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.label);
    }

    private boolean isMouseOver(int mouseX, int mouseY)
    {
        return !(mouseX < this.left || mouseX > this.left + this.labelHeight + CONTROL_INTERNAL_MARGIN + this.labelWidth
                || mouseY < this.targetAreaTop || mouseY > this.targetAreaBottom);
    }
    
    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        if(this.isMouseOver(mouseX, mouseY)) this.isOn = !this.isOn;
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
    {
        // ignore
        
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }

    public boolean isOn()
    {
        return isOn;
    }

    public Toggle setOn(boolean isOn)
    {
        this.isOn = isOn;
        return this;
    }

    public String getLabel()
    {
        return label;
    }

    public Toggle setLabel(String label)
    {
        this.label = label;
        this.isDirty = true;
        return this;
    }

}