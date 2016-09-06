package grondag.adversity.niceblock.base;

import grondag.adversity.niceblock.support.BaseMaterial;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class NiceBlockPlus2 extends NiceBlock2 implements ITileEntityProvider {

	public NiceBlockPlus2(ModelDispatcher2 dispatcher, BaseMaterial material, String styleName)
	{
		super(dispatcher, material, styleName);
	}
		
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new NiceTileEntity2();		
	}
	
	@Override
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        return 0;
    }
	
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1, 0);
        long key = ((IExtendedBlockState)this.getExtendedState(state, world, pos)).getValue(MODEL_KEY);
        NiceItemBlock2.setModelStateKey(stack, key);
        return stack;
    }
	
    @Override
    public long getModelStateKey(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        long oldKey = 0;
        NiceTileEntity2 myTE = (NiceTileEntity2) world.getTileEntity(pos);
        if(myTE != null) oldKey = myTE.getModelKey();
        boolean needsRefresh = myTE.isModelKeyCacheDirty;
        myTE.isModelKeyCacheDirty = false;
        long newKey = dispatcher.getStateSet().getRefreshedKeyFromWorld(oldKey, needsRefresh, this, state, world, pos);
        if(newKey != oldKey) myTE.setModelKey(newKey);
        return newKey;
    }
    
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState, NiceTileEntity2 niceTE)
    {
        // default is NOOP
    }
}
