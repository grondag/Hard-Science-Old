package grondag.hard_science.gui.control;

import java.util.List;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.GuiUtil.HorizontalAlignment;
import grondag.hard_science.gui.GuiUtil.VerticalAlignment;
import grondag.hard_science.simulator.wip.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.wip.ItemResource;
import grondag.hard_science.simulator.wip.StorageType.StorageTypeStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemStackPicker extends TabBar<AbstractResourceWithQuantity<StorageTypeStack>>
{
    protected final FontRenderer fontRenderer;
    
    public ItemStackPicker(List<AbstractResourceWithQuantity<StorageTypeStack>> items, FontRenderer fontRenderer)
    {
        super(items);
        this.fontRenderer = fontRenderer;
        this.setItemsPerRow(9);
        this.setItemSpacing(2);
        this.setItemSelectionMargin(1);
        this.setSelectionEnabled(false);
        this.setCaptionHeight(fontRenderer.FONT_HEIGHT * 6 / 10 + 4);
    }
    
    
    @Override
    protected void drawContent(Minecraft mc, RenderItem itemRender, int mouseX, int mouseY, float partialTicks)
    {
        super.drawContent(mc, itemRender, mouseX, mouseY, partialTicks);
    }

    private String getQuantityLabel(long qty)
    {
        if(qty < 1000)
        {
            return Long.toString(qty);
        }
        else if(qty < 10000)
        {
            return String.format("%.1fK", (float) qty / 1000);
        }
        else if(qty < 100000)
        {
            return Long.toString(qty / 1000) + "K";
        }
        else
        {
            return "many";
        }
    }


    @Override
    protected void drawItem(AbstractResourceWithQuantity<StorageTypeStack> item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks)
    {
        int x = (int)left;
        int y = (int)top;
        
        ItemStack stack = ((ItemResource)item.resource()).sampleItemStack();
        GlStateManager.enableLighting();
        itemRender.renderItemAndEffectIntoGUI(((ItemResource)item.resource()).sampleItemStack(), x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, "");
        
        // itemRender doesn't clean this up, messes up highlight boxes
        this.drawQuantity(item.getQuantity(), x, y);
    }
    
    protected void drawQuantity(long qty, int left, int top)
    {
        String qtyLabel = this.getQuantityLabel(qty);
        
        boolean wasUnicode = this.fontRenderer.getUnicodeFlag();
        if(wasUnicode) this.fontRenderer.setUnicodeFlag(false);
        
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        
        GlStateManager.translate(left, top + 18, 0);
        GlStateManager.scale(0.6, 0.6, 1);
        
        GuiUtil.drawAlignedStringNoShadow(this.fontRenderer, qtyLabel, 0, 0, 16 * 10 / 6 , this.getCaptionHeight() * 10 / 6, 
                0xFFFFFFFF, HorizontalAlignment.CENTER, VerticalAlignment.TOP);

        

//        GlStateManager.enableDepth();
//        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        
        if(wasUnicode) this.fontRenderer.setUnicodeFlag(true);
    }


    @Override
    protected void setupItemRendering()
    {
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);        
    }
}
