package grondag.hard_science.gui.control;

import java.util.List;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.color.ColorMap;
import grondag.hard_science.superblock.color.ColorMap.EnumColorMap;
import grondag.hard_science.superblock.texture.TextureRotationType;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
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
      
        Rotation rotation = item.rotation.rotationType() == TextureRotationType.RANDOM 
                ? Rotation.values()[(int) ((System.currentTimeMillis() >> 11) & 3)]
                : item.rotation.rotation;
                
        TextureAtlasSprite tex = mc.getTextureMapBlocks().getAtlasSprite(item.getSampleTextureName());
        GuiUtil.drawTexturedRectWithColor(left, top, this.zLevel, tex, (int)this.actualItemSize(), (int)this.actualItemSize(), color, item.textureScale, rotation);
    }
}
