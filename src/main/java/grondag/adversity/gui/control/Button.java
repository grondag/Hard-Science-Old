package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static grondag.adversity.gui.GuiUtil.VerticalAlignment.*;
import static grondag.adversity.gui.control.GuiControl.*;
import static grondag.adversity.gui.GuiUtil.HorizontalAlignment.*;

@SideOnly(Side.CLIENT)
public class Button extends GuiButton
{
    public int buttonColor = BUTTON_COLOR_ACTIVE;
    public int disabledColor = BUTTON_COLOR_INACTIVE;
    public int hoverColor = BUTTON_COLOR_FOCUS;
    public int textColor = TEXT_COLOR_ACTIVE;

    public Button(int buttonId, int x, int y, int width, int height, String buttonText)
    {
        super(buttonId, x, y, width, height, buttonText);
    }

    public void resize(int x, int y, int width, int height)
    {
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.hovered);
            int color = i == 0 ? this.disabledColor : i == 2 ? this.hoverColor : this.buttonColor;

            GuiUtil.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width - 1, this.yPosition + this.height - 1, color);
            FontRenderer fontrenderer = mc.fontRenderer;
            GuiUtil.drawAlignedStringNoShadow(fontrenderer, this.displayString, this.xPosition, this.yPosition, this.width, this.height, this.textColor, CENTER, MIDDLE);
        }
    }
}
