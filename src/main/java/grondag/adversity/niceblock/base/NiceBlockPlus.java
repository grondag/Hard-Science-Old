//package grondag.adversity.niceblock.base;
//
//import java.util.List;
//import javax.annotation.Nullable;
//
//import grondag.adversity.Configurator;
//import grondag.adversity.niceblock.base.NiceTileEntity.ModelRefreshMode;
//import grondag.adversity.niceblock.color.ColorMap;
//import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
//import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
//import grondag.adversity.niceblock.support.BlockSubstance;
//import net.minecraft.block.ITileEntityProvider;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.Explosion;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.World;
//
//@SuppressWarnings("unused")
//public class NiceBlockPlus extends NiceBlock implements ITileEntityProvider 
//{
//    
//	public NiceBlockPlus(ModelDispatcher dispatcher, BlockSubstance material, String styleName)
//	{
//		super(dispatcher, material, styleName);
//	}
//		
//	
//	@Override
//	public TileEntity createNewTileEntity(World worldIn, int meta) {
//		return new NiceTileEntity();		
//	}
//	
//	public ModelRefreshMode getModelRefreshMode()
//	{
//	    return ModelRefreshMode.CACHE;
//	}
//
//	
//	/**
//	 * Defer destruction of block until after drops when harvesting so can gather NBT from tile entity.
//	 */
//	@Override
//	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
//	    if (willHarvest) {
//	        return true;
//	    }
//	    return super.removedByPlayer(state, world, pos, player, willHarvest);
//	}
//	
//	/**
//	 * Need to destroy block here because did not do it during removedByPlayer.
//	 */
//	@Override
//	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) 
//	{
//	    super.harvestBlock(worldIn, player, pos, state, te, stack);
//	    worldIn.setBlockToAir(pos);
//	}
//	
//	@Override
//    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
//    {
//        ItemStack stack = super.getStackFromBlock(state, world, pos);
//        
//        if(stack != null)
//        {
//            NiceItemBlock myItem = (NiceItemBlock) Item.getItemFromBlock(this);
//            ModelStateSetValue modelState = this.dispatcher.getStateSet().getSetValueFromKey( this.getModelStateKey(state, world, pos));
//            myItem.setColorMapID(stack, modelState.getValue(this.dispatcher.getStateSet().getFirstColorMapComponent()).ordinal);
//        }
//
//        return stack;
//    }
//	
//	//for tile-entity blocks, almost never use meta on items
//    @Override
//    public int damageDropped(IBlockState state)
//    {
//        return 0;
//    }
//    
//    @Override
//    public boolean hasAppearanceGui()
//    {
//        return true;
//    }
//
//    @Override
//    public long getModelStateKey(IBlockState state, IBlockAccess world, BlockPos pos)
//    {
//        long oldKey = 0;
//        NiceTileEntity myTE = (NiceTileEntity) world.getTileEntity(pos);
//        if(myTE != null) 
//        {
//            oldKey = myTE.getModelKey();
//            boolean needsRefresh = myTE.isModelKeyCacheDirty;
//            myTE.isModelKeyCacheDirty = false;
//            long newKey = dispatcher.getStateSet().getRefreshedKeyFromWorld(oldKey, needsRefresh, this, state, world, pos);
//            if(newKey != oldKey) 
//            {
//                myTE.setModelKey(newKey);
//            }
//            return newKey;
//        }
//        else
//        {
//            return dispatcher.getStateSet().getRefreshedKeyFromWorld(oldKey, true, this, state, world, pos);
//        }
//    }
//    
//    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState, NiceTileEntity niceTE)
//    {
//        // default is NOOP
//    }
//    
//    @Override
//    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
//    {
//        super.addInformation(stack, playerIn, tooltip, advanced);
//        ModelColorMapComponent colorComponent = this.dispatcher.getStateSet().getFirstColorMapComponent();
//        if(colorComponent != null && colorComponent.getValueCount() > 1)
//        {
//            long modelKey = NiceItemBlock.getModelStateKey(stack);
//            if(modelKey != 0L)
//            {
//                ColorMap colorMap = dispatcher.getStateSet().getSetValueFromKey(modelKey).getValue(colorComponent);
//                if(colorMap != null)
//                {
//                    tooltip.add("Color: " + colorMap.colorMapName);
//                }
//            }
//        }
//    }
//    
//    // BLOCK PROPERTIES
//    
//
//    @Override
//    public boolean canEntitySpawn(IBlockState state, Entity entityIn)
//    {
//        // hyperstone blocks can be configured to prevent mob spawning
//        
//        if(this.material.isHyperMaterial && !Configurator.HYPERSTONE.allowMobSpawning)
//        {
//            return false;
//        }
//        else
//        {
//            return super.canEntitySpawn(state, entityIn);
//        }
//    }
//}
