package grondag.hard_science.gui.control;

import grondag.hard_science.gui.Layout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiControl extends Gui
{      
    public static final int BUTTON_COLOR_ACTIVE = 0x9AFFFFFF;
    public static final int BUTTON_COLOR_INACTIVE = 0x2AFFFFFF;
    public static final int BUTTON_COLOR_FOCUS = 0xFFBAF6FF;
    public static final int TEXT_COLOR_ACTIVE = 0xFF000000;
    public static final int TEXT_COLOR_INACTIVE = 0xFFEEEEEE;
    public static final int TEXT_COLOR_FOCUS = 0xFF000000;
    public static final int TEXT_COLOR_LABEL = 0xFFFFFFFF;
    
    public static final int CONTROL_INTERNAL_MARGIN = 5;
    public static final int CONTROL_EXTERNAL_MARGIN = 5;    
    public static final int CONTROL_BACKGROUND = 0x4AFFFFFF;
    protected static final int NO_SELECTION = -1;
    
    protected double top;
    protected double left;
    protected double height;
    protected double width;
    protected double bottom;
    protected double right;
    
    protected int horizontalWeight = 1;
    protected int verticalWeight = 1;
    
    protected Layout horizontalLayout = Layout.WEIGHTED;
    protected Layout verticalLayout = Layout.WEIGHTED;
    
    protected int backgroundColor = 0;
    
    protected boolean isDirty = false;
    
    protected boolean isVisible = true;
    
    /** cumulative scroll distance from all events */
    protected int scrollDistance;
    /** cumulative distance before scroll is recognized */
    protected int scrollIncrementDistance = 128;
    /** last scroll increment - used to compute a delta */
    protected int lastScrollIncrement = 0;
    
    /** 
     * If a control has consistent shape, is height / width. 
     * Multiply width by this number to get height. 
     * Divide height by this number to get width.
     */
    protected double aspectRatio = 1.0;
    
    public GuiControl resize(double left, double top, double width, double height)
    {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.isDirty = true;
        return this;
    }
    
    public void drawControl(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        this.refreshContentCoordinatesIfNeeded();
        if(this.isVisible) this.drawContent(mc, itemRender, mouseX, mouseY, partialTicks);
    }
    
    protected abstract void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks);
    
    /** called after any coordinate-related input changes */
    protected abstract void handleCoordinateUpdate();
    
    protected abstract void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton);
    
    protected abstract void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton);
    
    protected abstract void handleMouseScroll(int mouseX, int mouseY, int scrollDelta);
    
    public void mouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.isVisible)
        {
            this.refreshContentCoordinatesIfNeeded();
            if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return;
            this.handleMouseClick(mc, mouseX, mouseY, clickedMouseButton);
        }
    }
    
    public void mouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.isVisible)
        {
            this.refreshContentCoordinatesIfNeeded();
            if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return;
            this.handleMouseDrag(mc, mouseX, mouseY, clickedMouseButton);
        }
    }
    
    public void mouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        if(this.isVisible)
        {
            this.refreshContentCoordinatesIfNeeded();
            if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return;
            this.scrollDistance += scrollDelta;
            this.handleMouseScroll(mouseX, mouseY, scrollDelta);
        }
    }
    
    protected int mouseIncrementDelta()
    {
        int newIncrement = this.scrollDistance / this.scrollIncrementDistance;
        int result = newIncrement - this.lastScrollIncrement;
        if(result != 0)
        {
            this.lastScrollIncrement = newIncrement;
        }
        return result;
    }
    
    protected void refreshContentCoordinatesIfNeeded()
    {
        if(this.isDirty)
        {
            this.bottom = this.top + this.height;
            this.right = this.left + this.width;
            
            this.handleCoordinateUpdate();
            this.isDirty = false;
        }
    }

    public double getTop()
    {
        return top;
    }

    public GuiControl setTop(double top)
    {
        this.top = top;
        this.isDirty = true;
        return this;
    }
    
    public double getBottom()
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.bottom;
    }

    public double getLeft()
    {
        return left;
    }

    public GuiControl setLeft(double left)
    {
        this.left = left;
        this.isDirty = true;
        return this;
    }

    public double getRight()
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.right;
    }
    
    public double getHeight()
    {
        return height;
    }

    public GuiControl setHeight(double height)
    {
        this.height = height;
        this.isDirty = true;
        return this;
    }

    public double getWidth()
    {
        return width;
    }

    public GuiControl setWidth(double width)
    {
        this.width = width;
        this.isDirty = true;
        return this;
    }
   
    public int getBackgroundColor()
    {
        return backgroundColor;
    }

    public GuiControl setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public double getAspectRatio()
    {
        return aspectRatio;
    }

    public GuiControl setAspectRatio(double aspectRatio)
    {
        this.aspectRatio = aspectRatio;
        return this;
    }

    public int getHorizontalWeight()
    {
        return horizontalWeight;
    }

    public GuiControl setHorizontalWeight(int horizontalWeight)
    {
        this.horizontalWeight = horizontalWeight;
        return this;
    }

    public int getVerticalWeight()
    {
        return verticalWeight;
    }

    public GuiControl setVerticalWeight(int verticalWeight)
    {
        this.verticalWeight = verticalWeight;
        return this;
    }

    public Layout getHorizontalLayout()
    {
        return horizontalLayout;
    }

    public GuiControl setHorizontalLayout(Layout horizontalLayout)
    {
        this.horizontalLayout = horizontalLayout;
        return this;
    }

    public Layout getVerticalLayout()
    {
        return verticalLayout;
    }

    public GuiControl setVerticalLayout(Layout verticalLayout)
    {
        this.verticalLayout = verticalLayout;
        return this;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }
}