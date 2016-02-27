package grondag.adversity.niceblock.newmodel;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.color.NiceColor;
import grondag.adversity.niceblock.support.NicePlacement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

public abstract class BlockModelHelper
{
    public NiceBlock block;
    
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
        String styleName = LanguageRegistry.instance().getStringLocalization(block.styleName);
        if(styleName == null || styleName == "")
        {
            styleName = block.styleName;
        }
        baseDisplayName = styleName + " " + LanguageRegistry.instance().getStringLocalization(block.material.materialName); 
    }
    
    /** set doClientStateRefresh = false to avoid inifinte recursion when getting colorinfo 
     * or other state potentially from within clientStateRefresh */
    public abstract ModelState getModelStateForBlock(IBlockState state, IBlockAccess world, BlockPos pos, boolean doClientStateRefresh);

    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ModelState modelState = this.getModelStateForBlock(state, world, pos, true);
        return ((IExtendedBlockState) state).withProperty(NiceBlock.MODEL_STATE, modelState);
    }
    
    public abstract int getItemModelCount();

    public List<ItemStack> getSubItems()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < this.getItemModelCount(); i++)
        {
            itemBuilder.add(new ItemStack(block.item, 1, i));
        }
        return itemBuilder.build();
    }    
    
    public abstract ModelState getModelStateForItemModel(int itemIndex);
    
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos pos, EnumFacing facing, ItemStack stack)
    {
        return stack.getMetadata();
    }
    
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState, NiceTileEntity niceTE)
    {
        // default is NOOP
    }
    
    public int getColorIndexFromItemStack(ItemStack stack)
    {
        return stack.getMetadata();
    }
    
    public void updateItemStackForPickBlock(ItemStack stack, IBlockState blockState, ModelState modelState, NiceTileEntity niceTE)
    {
        // default is NOOP
    }
    
    public String getItemStackDisplayName(ItemStack stack)
    {
        return baseDisplayName;
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        // NOOP is default implementation
    }
}
