package grondag.adversity.niceblock.base;

import java.util.List;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.base.NiceTileEntity.ModelRefreshMode;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NiceBlockPlus extends NiceBlock implements ITileEntityProvider 
{
    
	public NiceBlockPlus(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
	{
		super(dispatcher, material, styleName);
	}
		
    //only display one item meta variant for item search
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(getSubItems().get(0));
    }
    
    public List<ItemStack> getSubItems()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < 16; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new NiceTileEntity();		
	}
	
	public ModelRefreshMode getModelRefreshMode()
	{
	    return ModelRefreshMode.CACHE;
	}

	
	/**
	 * Defer destruction of block until after drops when harvesting so can gather NBT from tile entity.
	 */
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
	    if (willHarvest) {
	        return true;
	    }
	    return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	/**
	 * Need to destroy block here because did not do it during removedByPlayer.
	 */
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) 
	{
	    super.harvestBlock(worldIn, player, pos, state, te, stack);
	    worldIn.setBlockToAir(pos);
	}
	
	@Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ItemStack stack = super.getStackFromBlock(state, world, pos);
        
        if(stack != null)
        {
            NiceItemBlock myItem = (NiceItemBlock) Item.getItemFromBlock(this);
            ModelStateSetValue modelState = this.dispatcher.getStateSet().getSetValueFromBits( this.getModelStateKey(state, world, pos));
            myItem.setColorMapID(stack, modelState.getValue(this.dispatcher.getStateSet().getFirstColorMapComponent()).ordinal);
        }

        return stack;
    }

    @Override
    public long getModelStateKey(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        long oldKey = 0;
        NiceTileEntity myTE = (NiceTileEntity) world.getTileEntity(pos);
        if(myTE != null) oldKey = myTE.getModelKey();
        boolean needsRefresh = myTE.isModelKeyCacheDirty;
        myTE.isModelKeyCacheDirty = false;
        long newKey = dispatcher.getStateSet().getRefreshedKeyFromWorld(oldKey, needsRefresh, this, state, world, pos);
        if(newKey != oldKey) 
        {
//            Adversity.log.info("calling setModelKey from NiceBlockPlus.getModelStateKey");
            myTE.setModelKey(newKey);
        }
        return newKey;
    }
    
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState, NiceTileEntity niceTE)
    {
        // default is NOOP
    }
    
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        ModelColorMapComponent colorComponent = this.dispatcher.getStateSet().getFirstColorMapComponent();
        if(colorComponent != null && colorComponent.getValueCount() > 1)
        {
            long modelKey = NiceItemBlock.getModelStateKey(stack);
            if(modelKey != 0L)
            {
                ColorMap colorMap = dispatcher.getStateSet().getSetValueFromBits(modelKey).getValue(colorComponent);
                if(colorMap != null)
                {
                    tooltip.add("Color: " + colorMap.colorMapName);
                }
            }
        }
    }
}
