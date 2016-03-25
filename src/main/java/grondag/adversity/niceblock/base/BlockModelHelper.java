package grondag.adversity.niceblock.base;

import java.util.List;

import com.google.common.collect.ImmutableList;

//import mcp.mobius.waila.api.IWailaConfigHandler;
//import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public abstract class BlockModelHelper
{
    public NiceBlock block;
    
    protected String baseDisplayName;
    
    public final ModelDispatcher dispatcher;
    
    protected BlockModelHelper(ModelDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }
    
    /** called by NiceBlock during its constructor */
    public void setBlock(NiceBlock block)
    {
        this.block = block;
        String styleName = I18n.translateToLocal(block.getStyleName());

        if(styleName == null || styleName == "")
        {
            styleName = block.getStyleName();
        }
        baseDisplayName = styleName + " " + I18n.translateToLocal(block.material.materialName); 
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
    
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
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
    
//    public List<String> getWailaBody(ItemStack itemStack, List<String> current tip, IWailaDataAccessor accessor, IWailaConfigHandler config)
//    {
//        List<String> retVal = new ArrayList<String>();
//        this.addInformation(itemStack, null, retVal, false);
//        return retVal;
//    }

    public boolean hasCustomBrightness()
    {
        return false;
    }
       
    /** won't be called unless hasCustomBrightness is true */
    public int getCustomBrightness(IBlockState state, IBlockAccess source, BlockPos pos) 
    {
        return 0xFFFFFFFF;
    }
    
}
