package grondag.adversity.niceblocks;


import grondag.adversity.Adversity;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.ShapeValidatorCubic;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblocks.client.INiceCookbook;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NiceBlock extends Block {
	
	public static final PropertyInteger PROP_SUBSTANCE_INDEX = PropertyInteger.create("substance_index", 0, 15);
	public static final IUnlistedProperty PROP_RECIPE = Properties.toUnlisted(PropertyInteger.create("recipe", 0, 385));
	public static final IUnlistedProperty PROP_ALTERNATE = Properties.toUnlisted(PropertyInteger.create("alternate", 0, 15));
	
	public final NiceSubstance[] substances;
    public final int countMaterials;
    public final NiceBlockStyle style;

    /**
     * Assumes first substance is representative of all the substances
     * for purposes of setting material-dependent attributes.
     * 
     * @param unlocalizedName
     * @param style
     * @param substances
     */
	public NiceBlock(String unlocalizedName, NiceBlockStyle style, NiceSubstance... substances ) {
		super(substances[0].baseMaterial.material);
		this.style = style;
		this.substances = substances;
		this.setUnlocalizedName(unlocalizedName);
		this.setCreativeTab(Adversity.tabAdversity);
		this.setHarvestLevel(substances[0].baseMaterial.harvestTool, substances[0].baseMaterial.harvestLevel);
		this.setStepSound(substances[0].baseMaterial.stepSound);
		this.setHardness(substances[0].baseMaterial.hardness);
		this.setResistance(substances[0].baseMaterial.resistance);
		
		// just in case I'm stupid enough to send in more than metadata will support
		this.countMaterials = Math.min(substances.length + 1, 16);

	}	

	@Override
	public int damageDropped(IBlockState state)
	{
		return (Integer) state.getValue(PROP_SUBSTANCE_INDEX);
	}

	@Override
	public String getUnlocalizedName() {
		// TODO Auto-generated method stub
		return super.getUnlocalizedName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
	{
		for(int i = 0; i < countMaterials; i++){
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(PROP_SUBSTANCE_INDEX, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return (Integer) state.getValue(PROP_SUBSTANCE_INDEX);
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.SOLID;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		// should always be an IExtendedBlockState but avoid crash if not
		if (state instanceof IExtendedBlockState) {  
			return style.cookbook.getExtendedState((IExtendedBlockState)state, world, pos);
		} else {
			return state;
		}
	}	

	@Override
	protected BlockState createBlockState()
	{
		return new ExtendedBlockState(this, new IProperty[] {PROP_SUBSTANCE_INDEX}, new IUnlistedProperty[] {PROP_RECIPE, PROP_ALTERNATE});
	}


	/**
	 * Blocks match if they have are the same block and same substance.
	 * Also implies the same style.
	 * @author grondag
	 */
	public static class TestForCompleteMatch implements IBlockTest{

		private final Block block;
		private final int substanceIndex;
		
		/**
		 * Pass in the state of the block you want to match with.
		 * @param ibs
		 */
		public TestForCompleteMatch(IBlockState ibs){
			this.block = ibs.getBlock();
			this.substanceIndex = (Integer) ibs.getValue(PROP_SUBSTANCE_INDEX);
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() == block && (Integer)ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}	 
	}

	/**
	 * Blocks match if they are of the same substance.
	 * Can be different styles or blocks.
	 * @author grondag
	 */
	public static class TestForSubstance implements IBlockTest{

		private final int substanceIndex;

		public TestForSubstance(IBlockState ibs){
			if( ibs.getBlock() instanceof NiceBlock){
				this.substanceIndex = (Integer) ibs.getValue(PROP_SUBSTANCE_INDEX);
			} else{
				substanceIndex = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && (Integer)ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}	  
	}
	
	/**
	 * Blocks match if they have have the same style.
	 * Can be different substances or blocks.
	 * @author grondag
	 */
	public static class TestForStyle implements IBlockTest{

		private final NiceBlockStyle style;

		public TestForStyle(IBlockState ibs){
			if( ibs.getBlock() instanceof NiceBlock){
				this.style = ((NiceBlock) ibs.getBlock()).style;
			} else{
				this.style = null;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style;
		}	  
	}
	
}
