package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderItem;

public abstract class GuiControl extends Gui
{    
    protected double top;
    protected double left;
    protected double height;
    protected double width;
    protected double outerMargin;
    protected double innerMargin;
    
    protected double contentTop;
    protected double contentLeft;
    protected double contentHeight;
    protected double contentWidth;
    protected double contentBottom;
    protected double contentRight;
    
    protected double backgroundTop;
    protected double backgroundLeft;
    protected double backgroundBottom;
    protected double backgroundRight;

    private int backgroundColor;
    
    public static final int BUTTON_COLOR_DEFAULT = 0x9AFFFFFF;
    public static final int DISABLED_COLOR_DEFAULT = 0x4AFFFFFF;
    public static final int FOCUS_COLOR_DEFAULT = 0xFFFFFFFF;
    public static final int TEXT_COLOR_DEFAULT = 0xFF000000;
    
    protected GuiControl() {};
    
    protected GuiControl(double left, double top, double width, double height)
    {
        this.resize(left, top, width, height);
    }
    
    public void resize(double left, double top, double width, double height)
    {
        this.top = top;
        this.left = left;
        this.height = height;
        this.width = width;
        this.refreshContentCoordinates();
    }
    
    public void drawControl(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        this.drawBackground(partialTicks);
        this.drawContent(mc, itemRender, mouseX, mouseY, partialTicks);
    }
    
    protected abstract void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks);
    
    /** called after any coordinate-related input changes */
    protected abstract void handleCoordinateUpdate();
    
    protected void drawBackground(float partialTicks)
    {
        GuiUtil.drawRect(backgroundLeft(), backgroundTop(), backgroundRight(), backgroundBottom(), getBackgroundColor());
    }
    
    public void handleMouseInput(int mouseX, int mouseY)
    {

    }
    
    protected void refreshContentCoordinates()
    {
        double totalMargin = this.innerMargin + this.outerMargin;
        this.contentTop = this.top + totalMargin;
        this.contentLeft = this.left + totalMargin;
        this.contentHeight = this.height - totalMargin - totalMargin;
        this.contentWidth = this.width - totalMargin - totalMargin;
        this.contentBottom = this.contentTop + this.contentHeight;
        this.contentRight = this.contentLeft + this.contentWidth;
        
        this.backgroundTop = this.top + this.outerMargin;
        this.backgroundLeft = this.left + this.outerMargin;
        this.backgroundBottom = this.top + this.height - this.outerMargin;
        this.backgroundRight = this.left + this.width - this.outerMargin;
        
        this.handleCoordinateUpdate();
    }

    public double getTop()
    {
        return top;
    }

    public void setTop(double top)
    {
        this.top = top;
        this.refreshContentCoordinates();
    }

    public double getLeft()
    {
        return left;
    }

    public void setLeft(double left)
    {
        this.left = left;
        this.refreshContentCoordinates();
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
        this.refreshContentCoordinates();
    }

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = width;
        this.refreshContentCoordinates();
    }

    public double getOuterMargin()
    {
        return outerMargin;
    }

    public void setOuterMargin(double outerMargin)
    {
        this.outerMargin = outerMargin;
        this.refreshContentCoordinates();
    }

    public double getInnerMargin()
    {
        return innerMargin;
    }

    public void setInnerMargin(double innerMargin)
    {
        this.innerMargin = innerMargin;
        this.refreshContentCoordinates();
    }

    public double contentTop()
    {
        return contentTop;
    }

    public double contentLeft()
    {
        return contentLeft;
    }

    public double contentHeight()
    {
        return contentHeight;
    }

    public double contentWidth()
    {
        return contentWidth;
    }

    public double contentBottom()
    {
        return contentBottom;
    }

    public double contentRight()
    {
        return contentRight;
    }

    public double backgroundTop()
    {
        return backgroundTop;
    }

    public double backgroundLeft()
    {
        return backgroundLeft;
    }

    public double backgroundBottom()
    {
        return backgroundBottom;
    }

    public double backgroundRight()
    {
        return backgroundRight;
    }

    public int getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }
}
