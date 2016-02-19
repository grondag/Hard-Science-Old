package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
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
//    public abstract ModelState getModelStateForItem(ItemStack stack);
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
        public ColorMeta(ModelDispatcher dispatcher)
        {
            super(dispatcher);
        }

        @Override
        public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            return new ModelState(dispatcher.controller.getBlockShapeIndex(block, state, world, pos),state.getValue(NiceBlock.META));
        }

//        @Override
//        public ModelState getModelStateForItem(ItemStack stack)
//        {
//            return new ModelState(0, stack.getMetadata());
//        }

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
            for(int i = 0; i < dispatcher.controller.getColorProvider().getColorCount(); i++)
            {
                itemBuilder.add(new ItemStack(block.item, 1, i));
            }
            return itemBuilder.build();
        }

        @Override
        public int getSubItemCount()
        {
            return dispatcher.controller.getColorProvider().getColorCount();
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack)
        {
            return 
                    LanguageRegistry.instance().getStringLocalization(dispatcher.controller.styleName) + " "
                    + LanguageRegistry.instance().getStringLocalization(block.material.materialName) 
                    + ", " 
                    + dispatcher.controller.getColorProvider().getColor(stack.getMetadata()).vectorName;
        }

        @Override
        public String getBlockRegistryName()
        {
            return super.getBlockRegistryName();
        }
    }
    
    public static class ColorPlus extends ColorMeta
    {
        public ColorPlus(ModelDispatcher dispatcher)
        {
            super(dispatcher);
        }

        @Override
        public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            ModelState retVal;
            NiceTileEntity niceTE = (NiceTileEntity)world.getTileEntity(pos);
            if (niceTE != null) 
            {
                Adversity.log.info("ColorPlus getModelStateForBlock found TE with loaded = " + niceTE.isLoaded);

                if(niceTE.isShapeIndexDirty)
                {
                    int newShapeIndex = dispatcher.controller.getBlockShapeIndex(block, state, world, pos);
                    if(newShapeIndex != niceTE.modelState.getShapeIndex())
                    {
                        niceTE.modelState.setShapeIndex(dispatcher.controller.getBlockShapeIndex(block, state, world, pos));
                        niceTE.markDirty();
                    }
                    niceTE.isShapeIndexDirty = false;
                }
                retVal = niceTE.modelState;
            }
            else
            {
                Adversity.log.info("ColorPlus getModelStateForBlock modelState found NULL tile entity");
                retVal = new ModelState(0, 0);
            }
            Adversity.log.info("colorIndex = " + retVal.getColorIndex() + ", shapeIndex = " + retVal.getShapeIndex());
            return retVal;
        }
        
//        @Override
//        public ModelState getModelStateForItem(ItemStack stack)
//        {
//            return new ModelState(stack.getTagCompound());
//        }
        
//        @Override
//        public List<ItemStack> getSubItems()
//        {
//            ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
//            for(int i = 0; i < dispatcher.controller.getColorProvider().getColorCount(); i++)
//            {
//  //              ModelState modelState = new ModelState(0, i);
// //               ItemStack stack = new ItemStack(block.item, 1, 0);
// //               stack.setTagCompound(modelState.getNBT());
//                itemBuilder.add(new ItemStack(block.item, 1, i));
//            }
//            return itemBuilder.build();
//        }
    }
}
