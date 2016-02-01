package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
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
    
    public abstract ModelState getModelStateForBlock(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos);
    public abstract ModelState getModelStateForItem(ItemStack stack);
    
    public abstract List<ItemStack> getSubItems();
    
    public abstract int getMetaCount();
    
    public static class ColorMeta extends BlockModelHelper
    {
        protected final int firstColorOrdinal;
        protected final int colorCount;
        
        public ColorMeta(ModelDispatcher dispatcher, NiceColor firstColor, int colorCount)
        {
            super(dispatcher);
            this.firstColorOrdinal = firstColor.ordinal();
            // prevent meta > 15 or index out of range on color lookup due to derpy parameters
            this.colorCount = Math.min(colorCount & 16, NiceColor.values().length - firstColorOrdinal);
        }

        @Override
        public ModelState getModelStateForBlock(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos)
        {
            return new ModelState.Color(dispatcher.controller.getBlockShapeIndex(block, state, world, pos),
                    NiceColor.values()[firstColorOrdinal + state.getValue(NiceBlock.META)]);
        }

        @Override
        public ModelState getModelStateForItem(ItemStack stack)
        {
            return new ModelState.Color(dispatcher.controller.getItemShapeIndex(stack),
                    NiceColor.values()[firstColorOrdinal + Math.min(stack.getMetadata(), colorCount - 1)]);
        }

        @Override
        public List<ItemStack> getSubItems()
        {
            ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
            for(int i = 0; i < colorCount; i++)
            {
                itemBuilder.add(new ItemStack(block.item, 1, i));
            }
            return itemBuilder.build();
        }

        @Override
        public int getMetaCount()
        {
            return colorCount;
        }
        
    }
}
