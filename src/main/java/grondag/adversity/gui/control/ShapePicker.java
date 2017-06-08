package grondag.adversity.gui.control;

import grondag.adversity.gui.GuiUtil;
import grondag.adversity.gui.base.TabBar;
import grondag.adversity.init.ModSuperModelBlocks;
import grondag.adversity.superblock.items.SuperItemBlock;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.support.BlockSubstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

public class ShapePicker extends TabBar<ModelShape>
{

    private static final ItemStack[] ITEMS = new ItemStack[ModelShape.GUI_AVAILABLE_SHAPES.size()];
    
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
      
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        
        GuiUtil.renderItemAndEffectIntoGui(mc, itemRender, ITEMS[item.ordinal()], left, top, (int)this.actualItemSize());
    }
}
