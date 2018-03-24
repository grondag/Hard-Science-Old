package grondag.hard_science.gui.control;

import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.ModelShape;
import grondag.exotic_matter.model.ModelState;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.init.ModSubstances;
import grondag.hard_science.init.ModSuperModelBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShapePicker extends TabBar<ModelShape<?>>
{

    private static final ItemStack[] ITEMS = new ItemStack[ModelShape.guiAvailableShapes().size()];
    
    static
    {
        for(ModelShape<?> shape : ModelShape.guiAvailableShapes())
        {
            ISuperModelState modelState = new ModelState();
            modelState.setShape(shape);
            ItemStack stack = ModSuperModelBlocks.findAppropriateSuperModelBlock(ModSubstances.FLEXSTONE, modelState).getSubItems().get(0).copy();
            SuperBlockStackHelper.setStackModelState(stack, modelState);
            ITEMS[shape.ordinal()] = stack;
        }
    }
    
    public ShapePicker()
    {
        super(ModelShape.guiAvailableShapes());
        this.setItemsPerRow(8);
    }

    @Override
    protected void drawItem(ModelShape<?> item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks, boolean isHighlighted)
    {
        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, ITEMS[item.ordinal()], left, top, (int)this.actualItemSize());
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

    @Override
    protected void drawToolTip(ModelShape<?> item, IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawToolTip(item.localizedName(), mouseX, mouseY);
    }
}
