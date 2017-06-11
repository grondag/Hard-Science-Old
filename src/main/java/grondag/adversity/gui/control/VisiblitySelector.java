package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;

import static grondag.adversity.gui.GuiUtil.VerticalAlignment.*;
import static grondag.adversity.gui.GuiUtil.HorizontalAlignment.*;

public class VisiblitySelector extends GuiControl
{
    private final VisibilityPanel target;
    
    private double buttonHeight;
    
    public VisiblitySelector(VisibilityPanel target)
    {
        this.target = target;
    }
    
    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        FontRenderer fontrenderer = mc.fontRenderer;
        double y = this.top;
        
        int hoverIndex = this.getButtonIndex(mouseX, mouseY);
        
        for(int i = 0; i < target.size(); i++)
        {
            String label = target.getLabel(i);
            
            GuiUtil.drawBoxRightBottom(this.left, y, this.right, y + this.buttonHeight, 1, BUTTON_COLOR_ACTIVE);
            int buttonColor = i == hoverIndex ? BUTTON_COLOR_FOCUS : i == this.target.getVisiblityIndex() ? BUTTON_COLOR_ACTIVE : BUTTON_COLOR_INACTIVE;
            GuiUtil.drawRect(this.left + 2 , y + 2, this.right - 2, y + this.buttonHeight - 2, buttonColor);

            int textColor = i == hoverIndex ? TEXT_COLOR_FOCUS : i == this.target.getVisiblityIndex() ? TEXT_COLOR_ACTIVE : TEXT_COLOR_INACTIVE;
            GuiUtil.drawAlignedStringNoShadow(fontrenderer, label, (float)this.left, (float)y, (float)this.width, (float)this.buttonHeight, textColor, CENTER, MIDDLE);
            
            y += this.buttonHeight;
        }
         
    }

    private int getButtonIndex(int mouseX, int mouseY)
    {
        this.refreshContentCoordinatesIfNeeded();
        if(mouseX < this.left || mouseX > this.right || this.buttonHeight == 0)
            return NO_SELECTION;
        
        int selection = (int) ((mouseY - this.top) / this.buttonHeight);
        
        return (selection < 0 || selection >= this.target.size()) ? NO_SELECTION : selection;
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.target.size() > 0)
        {
            this.buttonHeight = this.height / this.target.size();
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        int clickIndex = this.getButtonIndex(mouseX, mouseY);
        
        if(clickIndex != NO_SELECTION && clickIndex != this.target.getVisiblityIndex())
        {
            this.target.setVisiblityIndex(clickIndex);
        }
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

}
