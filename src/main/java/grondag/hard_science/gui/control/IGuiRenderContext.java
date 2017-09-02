package grondag.hard_science.gui.control;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public interface IGuiRenderContext
{
    public Minecraft minecraft();
    public RenderItem renderItem();
    public GuiScreen screen();
    public FontRenderer fontRenderer();
    public void drawToolTip(ItemStack hoverStack, int mouseX, int mouseY);
    
    /** controls that are being hovered over while rendering should call this to
     * receive a callback after all controls have been rendered to draw a tooltip.
     */
    public abstract void setHoverControl(GuiControl<?> control);
    
    /**
     * Draws the given text as a tooltip.
     */
    public default void drawToolTip(String text, int mouseX, int mouseY)
    {
        this.screen().drawHoveringText(text, mouseX, mouseY);
    }

    public default void drawToolTip(List<String> textLines, int mouseX, int mouseY)
    {
        this.screen().drawHoveringText(textLines, mouseX, mouseY);
    }
    
    public default void drawLocalizedToolTip(String lang_key, int mouseX, int mouseY)
    {
        this.drawToolTip(I18n.translateToLocal(lang_key), mouseX, mouseY);
    }
    
    public default void drawLocalizedToolTip(int mouseX, int mouseY, String...lang_keys)
    {
        if(lang_keys.length == 0) return;
        
        ArrayList<String> list = new ArrayList<String>(lang_keys.length);
        
        for(String key : lang_keys)
        {
            list.add(I18n.translateToLocal(key));
        }
        this.drawToolTip(list, mouseX, mouseY);
    }
    
    public default void drawLocalizedToolTipBoolean(boolean bool, String true_key, String false_key, int mouseX, int mouseY)
    {
        this.drawToolTip(I18n.translateToLocal(bool ? true_key : false_key), mouseX, mouseY);
    }
}
