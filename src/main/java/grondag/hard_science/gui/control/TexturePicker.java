package grondag.hard_science.gui.control;

import java.util.List;

import grondag.hard_science.CommonProxy;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.texture.TextureRotationType;
import grondag.hard_science.superblock.texture.TexturePalletteRegistry.TexturePallette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TexturePicker extends TabBar<TexturePallette>
{
    public int borderColor = 0xFFFFFFFF;
    public int baseColor = 0;
    public boolean renderAlpha = true;
    
    public TexturePicker(List<TexturePallette> items, double left, double top)
    {
        super(items);
        this.setItemsPerRow(8);
    }

    @Override
    protected void drawItem(TexturePallette item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks, boolean isHighlighted)
    {

        int size = this.actualItemPixels();
        
        // if texture is translucent provide a background
        if(this.renderAlpha) GuiUtil.drawRect(left, top, left + size, top + size, this.baseColor);

        Rotation rotation = item.rotation.rotationType() == TextureRotationType.RANDOM 
                ? Rotation.values()[(int) ((CommonProxy.currentTimeMillis() >> 11) & 3)]
                : item.rotation.rotation;
                
        TextureAtlasSprite tex = mc.getTextureMapBlocks().getAtlasSprite(item.getSampleTextureName());
        GuiUtil.drawTexturedRectWithColor(left, top, this.zLevel, tex, size, size, this.borderColor, item.textureScale, rotation, renderAlpha);
    }

    @Override
    protected void setupItemRendering()
    {
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);        
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }
}
