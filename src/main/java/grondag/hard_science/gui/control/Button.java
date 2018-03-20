package grondag.hard_science.gui.control;

import static grondag.exotic_matter.varia.HorizontalAlignment.*;
import static grondag.exotic_matter.varia.VerticalAlignment.*;
import static grondag.hard_science.gui.control.GuiControl.*;

import javax.annotation.Nonnull;

import grondag.hard_science.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            int color = i == 0 ? this.disabledColor : i == 2 ? this.hoverColor : this.buttonColor;

            GuiUtil.drawRect(this.x, this.y, this.x + this.width - 1, this.y + this.height - 1, color);
            FontRenderer fontrenderer = mc.fontRenderer;
            GuiUtil.drawAlignedStringNoShadow(fontrenderer, this.displayString, this.x, this.y, this.width, this.height, this.textColor, CENTER, MIDDLE);
        }
    }
}
