package grondag.adversity.gui.control;

import java.util.List;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.superblock.color.ColorMap;
import grondag.adversity.superblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.texture.TexturePalletteRegistry.TexturePallette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TexturePicker extends TabBar<TexturePallette>
{

    public ColorMap colorMap;
    
    public TexturePicker(List<TexturePallette> items, double left, double top)
    {
        super(items);
        this.setItemsPerRow(8);
    }

    @Override
    protected void drawItem(TexturePallette item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks)
    {
        int color = this.colorMap == null ? 0xFFFFFFFF : this.colorMap.getColor(EnumColorMap.BASE);
      
        TextureAtlasSprite tex = mc.getTextureMapBlocks().getAtlasSprite(item.getSampleTextureName());
        GuiUtil.drawTexturedRectWithColor(left, top, this.zLevel, tex, (int)this.actualItemSize(), (int)this.actualItemSize(), color, item.textureScale);
    }
}
