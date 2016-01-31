package grondag.adversity.niceblock.newmodel;

import java.util.List;

import grondag.adversity.Adversity;
import net.minecraft.block.state.IBlockState;
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
    protected final NiceBlock block;
    public final ModelDispatcher dispatcher;

    protected BlockModelHelper(NiceBlock block, ModelDispatcher dispatcher)
    {
        this.block = block;
        this.dispatcher = dispatcher;
    }
    
    public abstract ModelState getModelStateForBlock(NiceBlock block, IBlockState state, IBlockAccess world, BlockPos pos);
    public abstract ModelState getModelStateForItem(ItemStack stack);
    
    public abstract List<ItemStack> getSubItems();
    
    public abstract int getMetaCount();

}
