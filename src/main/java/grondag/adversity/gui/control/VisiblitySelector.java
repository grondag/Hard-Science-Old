package grondag.adversity.gui.control;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.base.GuiControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;

public class VisiblitySelector extends GuiControl
{
    private ArrayList<Pair<String, GuiControl>> controls = new ArrayList<Pair<String, GuiControl>>();
    
    private static final int NO_SELECTION = -1;
    private int visibleIndex = NO_SELECTION;
    
    private double buttonHeight;
    
    public void add(String name, GuiControl control)
    {
        this.controls.add(Pair.of(name, control));
        if(control.isVisible())
        {
            if(this.visibleIndex != NO_SELECTION)
            {
                this.controls.get(this.visibleIndex).getRight().setVisible(false);
            }
            this.visibleIndex = this.controls.size() - 1;
        }
    }
    
    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        FontRenderer fontrenderer = mc.fontRenderer;
        double y = this.top;
        
        int hoverIndex = this.getButtonIndex(mouseX, mouseY);
        
        for(int i = 0; i < controls.size(); i++)
        {
            String label = controls.get(i).getLeft();
            
            int textColor = GuiControl.TEXT_COLOR_DEFAULT;
            
            GuiUtil.drawBox(this.left, y, this.right, y + this.buttonHeight, 1, BUTTON_COLOR_DEFAULT);
            
            if(i == hoverIndex)
            {
                textColor = 0xFF000000;
                GuiUtil.drawRect(this.left + 1 , y + 1, this.right - 1, y + this.buttonHeight - 1, FOCUS_COLOR_DEFAULT);
            }
            else if(i == visibleIndex)
            {
                GuiUtil.drawRect(this.left + 1 , y + 1, this.right - 1, y + this.buttonHeight - 1, BUTTON_COLOR_DEFAULT);
            }
            
            GuiUtil.drawCenteredStringNoShadow(fontrenderer, label, (float)(this.left + this.width / 2), (float)(y + this.buttonHeight / 2), textColor);
            
            y += this.buttonHeight;
        }
         
    }

    private int getButtonIndex(int mouseX, int mouseY)
    {
        this.refreshContentCoordinatesIfNeeded();
        if(mouseX < this.left || mouseX > this.right || this.buttonHeight == 0)
            return NO_SELECTION;
        
        int selection = (int) ((mouseY - this.top) / this.buttonHeight);
        
        return (selection < 0 || selection >= this.controls.size()) ? NO_SELECTION : selection;
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.controls.size() > 0)
        {
            this.buttonHeight = this.height / this.controls.size();
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY)
    {
        int clickIndex = this.getButtonIndex(mouseX, mouseY);
        
        if(clickIndex != NO_SELECTION && clickIndex != this.visibleIndex)
        {
            this.controls.get(this.visibleIndex).getRight().setVisible(false);
            this.visibleIndex = clickIndex;
            this.controls.get(clickIndex).getRight().setVisible(true);
        }
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY)
    {
        // ignore
    }

}
