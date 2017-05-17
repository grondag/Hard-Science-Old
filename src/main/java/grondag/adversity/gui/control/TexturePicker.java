package grondag.adversity.gui.control;

import java.util.List;

import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.base.TabBar;
import grondag.adversity.superblock.texture.TexturePalletteProvider.TexturePallette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;

public class TexturePicker extends TabBar<TexturePallette>
{

    public ColorMap colorMap;
    
    public TexturePicker(List<TexturePallette> items, double left, double top)
    {
        super(items);
    }

    @Override
    protected void drawItem(TexturePallette item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks)
    {
        int color = this.colorMap == null ? 0xFFFFFFFF : this.colorMap.getColor(EnumColorMap.BASE);
      
        GuiUtil.drawTexturedRectWithColor(left, top, this.zLevel, item.getPreviewSprite(), (int)this.actualItemSize(), (int)this.actualItemSize(), color, item.textureScale);
    }
}
