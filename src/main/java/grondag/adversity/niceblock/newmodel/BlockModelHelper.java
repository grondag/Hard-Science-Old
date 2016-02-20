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

public abstract class BlockModelHelper
{
    protected NiceBlock block;
    
    protected String baseDisplayName;
    
    public final ModelDispatcherBase dispatcher;

    protected BlockModelHelper(ModelDispatcherBase dispatcher)
    {
        this.dispatcher = dispatcher;

    }
    
    /** called by NiceBlock during its constructor */
    public void setBlock(NiceBlock block)
    {
        this.block = block;
        baseDisplayName = LanguageRegistry.instance().getStringLocalization(block.styleName) + " "
        + LanguageRegistry.instance().getStringLocalization(block.material.materialName); 
    }
    
    public abstract ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos);

    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ModelState modelState = this.getModelStateForBlock(state, world, pos);
        return ((IExtendedBlockState) state).withProperty(NiceBlock.MODEL_STATE, modelState);
    }
    
    public abstract int getSubItemCount();

    public List<ItemStack> getSubItems()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < this.getSubItemCount(); i++)
        {
            itemBuilder.add(new ItemStack(block.item, 1, i));
        }
        return itemBuilder.build();
    }    
    
    public abstract ModelState getModelStateForItem(int itemIndex);
    
    public abstract String getItemStackDisplayName(ItemStack stack);
        
    public static class ColorMeta extends BlockModelHelper
    {
        public ColorMeta(ModelDispatcherBase dispatcher)
        {
            super(dispatcher);
        }

        @Override
        public ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos)
        {
            ModelState retVal = new ModelState(0, state.getValue(NiceBlock.META));
            dispatcher.refreshClientShapeIndex(block, state, world, pos, retVal);
            return retVal;
        }

        @Override
        public int getSubItemCount()
        {
            return dispatcher.getColorProvider().getColorCount();
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack)
        {
            String colorName = dispatcher.getColorProvider().getColor(stack.getMetadata()).vectorName;
            return baseDisplayName + (colorName == "" ? "" : ", " + colorName);
        }

        @Override
        public ModelState getModelStateForItem(int itemIndex)
        {
            return new ModelState(0, itemIndex);
        }
    }
    
    public static class ColorPlus extends ColorMeta
    {
        public ColorPlus(ModelDispatcherBase dispatcher)
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
                if(niceTE.isClientShapeIndexDirty)
                {
                    if(dispatcher.refreshClientShapeIndex(block, state, world, pos, niceTE.modelState))
                    {
                        niceTE.markDirty();
                    }
                    niceTE.isClientShapeIndexDirty = false;
                }
                retVal = niceTE.modelState;
            }
            else
            {
                retVal = new ModelState();
            }
            return retVal;
        }
    }
}
