package grondag.hard_science.gui.control;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShapePicker extends TabBar<ModelShape>
{

    private static final ItemStack[] ITEMS = new ItemStack[ModelShape.values().length];
    
    static
    {
        for(ModelShape shape : ModelShape.GUI_AVAILABLE_SHAPES)
        {
            ModelState modelState = new ModelState();
            modelState.setShape(shape);
            ItemStack stack = ModSuperModelBlocks.findAppropriateSuperModelBlock(BlockSubstance.FLEXSTONE, modelState).getSubItems().get(0);
            SuperItemBlock.setModelState(stack, modelState);
            ITEMS[shape.ordinal()] = stack;
        }
    }
    
    public ShapePicker()
    {
        super(ModelShape.GUI_AVAILABLE_SHAPES);
        this.setItemsPerRow(8);
    }

    @Override
    protected void drawItem(ModelShape item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks)
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
}
