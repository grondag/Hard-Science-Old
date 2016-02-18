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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

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
    public abstract IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos);
    
    public abstract List<ItemStack> getSubItems();
    
    public abstract int getSubItemCount();
    
    public abstract String getItemStackDisplayName(ItemStack stack);
    
    public String getBlockRegistryName()
    {
        return this.block.material.materialName + "." + this.dispatcher.controller.styleName;
    }
    
    public static class ColorMeta extends BlockModelHelper
    {
        protected final int colorIndexes[];
        protected final String subsetName;
        
        public ColorMeta(ModelDispatcher dispatcher, ColorSubset colorSubset)
        {
            super(dispatcher);
            this.colorIndexes = dispatcher.controller.getColorProvider().getSubset(colorSubset);
            this.subsetName = colorSubset.name();
        }

        @Override
        public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            return new ModelState(dispatcher.controller.getBlockShapeIndex(block, state, world, pos),
                    colorIndexes[state.getValue(NiceBlock.META)]);
        }

        @Override
        public ModelState getModelStateForItem(ItemStack stack)
        {
            return new ModelState(0, colorIndexes[Math.max(0, Math.min(stack.getMetadata(), colorIndexes.length - 1))]);
        }

        @Override
        public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            ModelState modelState = getModelStateForBlock(state, world, pos);
            return ((IExtendedBlockState) state).withProperty(NiceBlock.MODEL_STATE, modelState);
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
        public int getSubItemCount()
        {
            return colorIndexes.length;
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack)
        {
            return 
                    LanguageRegistry.instance().getStringLocalization(dispatcher.controller.styleName) + " "
                    + LanguageRegistry.instance().getStringLocalization(block.material.materialName) 
                    + ", " 
                    + dispatcher.controller.getColorProvider().getColor(getModelStateForItem(stack).getColorIndex()).vectorName;
        }

        @Override
        public String getBlockRegistryName()
        {
            return super.getBlockRegistryName() + "." + subsetName;
        }
    }
    
    public static class ColorPlus extends ColorMeta
    {
        public ColorPlus(ModelDispatcher dispatcher, ColorSubset colorSubset)
        {
            super(dispatcher, colorSubset);
        }

        @Override
        public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            ModelState retVal;
            NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
            if (niceTE != null) 
            {
                retVal = niceTE.modelState;

                if(niceTE.isShapeIndexDirty)
                {
                    int newShapeIndex = dispatcher.controller.getBlockShapeIndex(block, state, world, pos);
                    if(newShapeIndex != retVal.getShapeIndex())
                    {
                        retVal.setShapeIndex(dispatcher.controller.getBlockShapeIndex(block, state, world, pos));
                        niceTE.markDirty();
                    }
                    niceTE.isShapeIndexDirty = false;
                }
            }
            else
            {
                retVal = new ModelState(0, 0);
            }
            return retVal;
        }
        
        @Override
        public ModelState getModelStateForItem(ItemStack stack)
        {
            return new ModelState(0, stack.getMetadata());
        }
        
        @Override
        public List<ItemStack> getSubItems()
        {
            ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
            for(int i = 0; i < colorIndexes.length; i++)
            {
                NBTTagCompound tag = new NBTTagCompound();
                ModelState modelState = new ModelState(0, this.colorIndexes[i]);
                modelState.writeToNBT(tag);
                ItemStack stack = new ItemStack(block.item, 1, 0);
                stack.setTagCompound(tag);
                itemBuilder.add(stack);
            }
            return itemBuilder.build();
        }
    }
}
