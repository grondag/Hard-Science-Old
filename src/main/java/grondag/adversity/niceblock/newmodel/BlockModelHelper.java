package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.color.IColorProvider.ColorSubset;
import grondag.adversity.niceblock.newmodel.color.NiceColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
Handles client-side block events and
provides services related to models

Parameters
FirstColor
ColorCount

Handlers
getItemStackDisplayName
getLocalizedName
getExtendedState
getExtendedStateForItem
getParticleColor

Handlers via ModelController
canRenderInLayer
getCollisionHelper

Holds  references to
ModelController
 */
public abstract class BlockModelHelper
{
    protected NiceBlock block;
    public final ModelDispatcher dispatcher;

    protected BlockModelHelper(ModelDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }
    
    /** called by NiceBlock during its constructor */
    public void setBlock(NiceBlock block)
    {
        this.block = block;
    }
    
    public abstract ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos);
    public abstract ModelState getModelStateForItem(ItemStack stack);
    
    public abstract List<ItemStack> getSubItems();
    
    public abstract int getMetaCount();
    
    public static class ColorMeta extends BlockModelHelper
    {
        protected final int colorIndexes[];
        
        public ColorMeta(ModelDispatcher dispatcher, ColorSubset colorSubset)
        {
            super(dispatcher);
            // prevent meta > 15 or index out of range on color lookup due to derpy parameters
            this.colorIndexes = dispatcher.controller.getColorProvider().getSubset(colorSubset);
        }

        @Override
        public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            return new ModelState.Color(dispatcher.controller.getBlockShapeIndex(block, state, world, pos),
                    colorIndexes[state.getValue(NiceBlock.META)]);
        }

        @Override
        public ModelState getModelStateForItem(ItemStack stack)
        {
            return new ModelState.Color(dispatcher.controller.getItemShapeIndex(stack),
                    colorIndexes[Math.max(0, Math.min(stack.getMetadata(), colorIndexes.length - 1))]);
        }

        @Override
        public List<ItemStack> getSubItems()
        {
            ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
            for(int i = 0; i < colorIndexes.length; i++)
            {
                itemBuilder.add(new ItemStack(block.item, 1, i));
            }
            return itemBuilder.build();
        }

        @Override
        public int getMetaCount()
        {
            return colorIndexes.length;
        }

    }
}
