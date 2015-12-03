package grondag.adversity.niceblocks;


import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.ShapeValidatorCubic;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
    public final NiceBlockStyle style;
    
    /** see NiceBlockStyle renderLayerFlags and NiceBlock.canRenderInLayer()*/
	public final static int LAYER_SOLID = 1;
	public final static int LAYER_CUTOUT_MIPPED = 2;
	public final static int LAYER_CUTOUT = 4;
	public final static int LAYER_TRANSLUCENT = 8;
    
    private final IAlternator alternator;
    public final String name;
    private final NicePlacement placementHandler;

    /**
     * Assumes first substance is representative of all the substances
     * for purposes of setting material-dependent attributes.
     */
	public NiceBlock(String name, NiceBlockStyle style, NicePlacement placer, NiceSubstance... substances ) {
		super(substances[0].baseMaterial.material);
		this.style = style;
		this.substances = substances;
		this.name = name;
		this.setUnlocalizedName(Adversity.MODID + ":" + name);
		this.setCreativeTab(Adversity.tabAdversity);
		this.setHarvestLevel(substances[0].baseMaterial.harvestTool, substances[0].baseMaterial.harvestLevel);
		this.setStepSound(substances[0].baseMaterial.stepSound);
		this.setHardness(substances[0].baseMaterial.hardness);
		this.setResistance(substances[0].baseMaterial.resistance);
		this.alternator = Alternator.getAlternator((byte)(style.alternateCount * (style.useRotationsAsAlternates ? 4 : 1)));
		this.placementHandler = placer;
		placer.setOwner(this);
		
		// let registrar know to register us when appropriate
		NiceBlockRegistrar.allBlocks.add(this);
	}	

	@Override
	public int damageDropped(IBlockState state)
	{
		return (Integer) state.getValue(PROP_SUBSTANCE_INDEX);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
	{
		for(int i = 0; i < substances.length; i++){
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
			return ((IExtendedBlockState)state)
					.withProperty(PROP_RECIPE, style.cookbook.getModelIndex((IExtendedBlockState)state, world, pos))
					.withProperty(PROP_ALTERNATE, alternator.getAlternate(pos));
		} else {
			return state;
		}
	}	

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		switch(layer){
		case SOLID:
			return (style.renderLayerFlags & LAYER_SOLID) == LAYER_SOLID;
		case CUTOUT_MIPPED:
			return (style.renderLayerFlags & LAYER_CUTOUT_MIPPED) == LAYER_CUTOUT_MIPPED;
		case CUTOUT:
			return (style.renderLayerFlags & LAYER_CUTOUT) == LAYER_CUTOUT;
		case TRANSLUCENT:
			return (style.renderLayerFlags & LAYER_TRANSLUCENT) == LAYER_TRANSLUCENT;
		default:
			return false;
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
	 */
	public static class TestForCompleteMatch implements IBlockTest{

		private final Block block;
		private final int substanceIndex;
		
		/**
		 * Blocks match if they have are the same block and same substance.
		 * Also implies the same style.
		 * Pass in the state of the block you want to match with.
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
	 * Substance taken from the blockstate parameter.
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
	 */
	public static class TestForStyle implements IBlockTest{

		private final NiceBlockStyle style;
		
		/**
		 * Blocks match if they have have the same style.
		 * Can be different substances or blocks.
		 * Style taken from blockstate parameter.
		 */
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
	
	/**
	 * Blocks match if they have have the same style and subtance.
	 * Can be different blocks.
	 */
	public static class TestForStyleAndSubstance implements IBlockTest{

		private final NiceBlockStyle style;
		private final int substanceIndex;
		
		/**
		 * Blocks match if they have have the same style and subtance.
		 * Can be different blocks.
		 * Style and subtance taken from blockstate parameter.
		 */	
		public TestForStyleAndSubstance(IBlockState ibs){
			if( ibs.getBlock() instanceof NiceBlock){
				this.style = ((NiceBlock) ibs.getBlock()).style;
				this.substanceIndex = (Integer) ibs.getValue(PROP_SUBSTANCE_INDEX);
			} else{
				this.style = null;
				substanceIndex = -1;
			}
		}
		
		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style
					 && (Integer)ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}	  
	}
		
	/**
	 * Just like TestForStyleAndSubstance but matches on any one of 
	 * a group of styles passed in at instantiation.
	 */
	public static class TestForStyleGroupAndSubstance implements IBlockTest{

		private final HashSet<NiceBlockStyle> styles;
		private final int substanceIndex;
		
		/**
		 * Just like TestForStyleAndSubstance but matches on any one of 
		 * a group of styles passed in at instantiation.
		 * Substance taken from block state passed in.
		 */
		public TestForStyleGroupAndSubstance(IBlockState ibs, NiceBlockStyle... styles){
			this.styles = new HashSet(Arrays.asList(styles));
			if( ibs.getBlock() instanceof NiceBlock){
				this.substanceIndex = (Integer) ibs.getValue(PROP_SUBSTANCE_INDEX);
			} else{
				substanceIndex = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && styles.contains(((NiceBlock) ibs.getBlock()).style) 
					 && (Integer)ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}	  
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
			EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
			EntityLivingBase placer) {
		
		return placementHandler.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);


	}
}
