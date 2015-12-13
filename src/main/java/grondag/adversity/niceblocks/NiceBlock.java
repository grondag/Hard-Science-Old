package grondag.adversity.niceblocks;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Function;

/**
 * Base class for Adversity building blocks. Should be instantiated and set up
 * in NiceBlockRegistrar.
 */
public class NiceBlock extends Block {

	/** Index to the substances[] array. */
	public static final PropertyInteger		PROP_SUBSTANCE_INDEX	= PropertyInteger.create("substance_index", 0, 15);
	/**
	 * Used by NiceModel to select correct in-game variant. 385 is the max. Most
	 * blocks have fewer variants.
	 */
	public static final IUnlistedProperty	PROP_RECIPE				= Properties.toUnlisted(PropertyInteger.create(
																			"recipe", 0, 385));
	/**
	 * Used by NiceModel to select an alternate in-game appearance when more
	 * than one is available. We don't use the MineCraft alternate functionality
	 * because it is non-deterministic for different block states. This causes
	 * undesirable changes to texture selection when neighbor blocks change.
	 */
	public static final IUnlistedProperty	PROP_ALTERNATE			= Properties.toUnlisted(PropertyInteger.create(
																			"alternate", 0, 15));

	public final NiceSubstance[]			substances;
	public final NiceBlockStyle				style;
	public final ItemMultiTexture			item;

	// see NiceBlockStyle renderLayerFlags and NiceBlock.canRenderInLayer()
	public static final int					LAYER_SOLID				= 1;
	public static final int					LAYER_CUTOUT_MIPPED		= 2;
	public static final int					LAYER_CUTOUT			= 4;
	public static final int					LAYER_TRANSLUCENT		= 8;

	private final IAlternator				alternator;
	public final String						name;
	private final NicePlacement				placementHandler;
	
	/**
	 * Assumes first substance is representative of all the substances for
	 * purposes of setting material-dependent attributes.
	 */
	public NiceBlock(String name, NiceBlockStyle style, NicePlacement placer, NiceSubstance... substances) {
		super(substances[0].baseMaterial.material);
		this.style = style;
		this.substances = substances;
		this.name = name;
		setUnlocalizedName(Adversity.MODID + ":" + name);
		setCreativeTab(Adversity.tabAdversity);
		this.setHarvestLevel(substances[0].baseMaterial.harvestTool, substances[0].baseMaterial.harvestLevel);
		setStepSound(substances[0].baseMaterial.stepSound);
		setHardness(substances[0].baseMaterial.hardness);
		setResistance(substances[0].baseMaterial.resistance);
		alternator = Alternator.getAlternator((byte) (style.alternateCount * (style.useRotationsAsAlternates ? 4 : 1)));
		placementHandler = placer;
		placer.setOwner(this);

		item = new ItemMultiTexture(this, this, new Function<ItemStack, String>() {
			@Override
			public String apply(ItemStack stack) {
				return String.valueOf(stack.getMetadata());
			}
		});

		// let registrar know to register us when appropriate
		NiceBlockRegistrar.allBlocks.add(this);
	}

	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(PROP_SUBSTANCE_INDEX);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
		for (int i = 0; i < substances.length; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(PROP_SUBSTANCE_INDEX, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PROP_SUBSTANCE_INDEX);
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.SOLID;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		// should always be an IExtendedBlockState but avoid crash if not

		if (state instanceof IExtendedBlockState) {
			return ((IExtendedBlockState) state).withProperty(PROP_RECIPE,
					style.cookbook.getModelIndex((IExtendedBlockState) state, world, pos)).withProperty(PROP_ALTERNATE,
									alternator.getAlternate(pos));
		} else {
			return state;
		}
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		switch (layer) {
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
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[] { PROP_SUBSTANCE_INDEX }, new IUnlistedProperty[] {
				PROP_RECIPE, PROP_ALTERNATE });
	}
	
	/**
	 * Blocks match if they have are the same block and same substance. Also
	 * implies the same style.
	 */
	public static class TestForCompleteMatch implements IBlockTest {

		private final Block	block;
		private final int	substanceIndex;

		/**
		 * Blocks match if they have are the same block and same substance. Also
		 * implies the same style. Pass in the state of the block you want to
		 * match with.
		 */
		public TestForCompleteMatch(IBlockState ibs) {
			block = ibs.getBlock();
			substanceIndex = ibs.getValue(PROP_SUBSTANCE_INDEX);
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() == block && ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}
	}

	/**
	 * Blocks match if they are of the same substance. Can be different styles
	 * or blocks. Substance taken from the blockstate parameter.
	 */
	public static class TestForSubstance implements IBlockTest {

		private final int	substanceIndex;

		public TestForSubstance(IBlockState ibs) {
			if (ibs.getBlock() instanceof NiceBlock) {
				substanceIndex = ibs.getValue(PROP_SUBSTANCE_INDEX);
			} else {
				substanceIndex = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}
	}

	/**
	 * Blocks match if they have have the same style. Can be different
	 * substances or blocks.
	 */
	public static class TestForStyle implements IBlockTest {

		private final NiceBlockStyle	style;

		/**
		 * Blocks match if they have have the same style. Can be different
		 * substances or blocks. Style taken from blockstate parameter.
		 */
		public TestForStyle(IBlockState ibs) {
			if (ibs.getBlock() instanceof NiceBlock) {
				style = ((NiceBlock) ibs.getBlock()).style;
			} else {
				style = null;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style;
		}
	}

	/**
	 * Blocks match if they have have the same style and subtance. Can be
	 * different blocks.
	 */
	public static class TestForStyleAndSubstance implements IBlockTest {

		private final NiceBlockStyle	style;
		private final int				substanceIndex;

		/**
		 * Blocks match if they have have the same style and subtance. Can be
		 * different blocks. Style and subtance taken from blockstate parameter.
		 */
		public TestForStyleAndSubstance(IBlockState ibs) {
			if (ibs.getBlock() instanceof NiceBlock) {
				style = ((NiceBlock) ibs.getBlock()).style;
				substanceIndex = ibs.getValue(PROP_SUBSTANCE_INDEX);
			} else {
				style = null;
				substanceIndex = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style
					&& ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}
	}

	/**
	 * Just like TestForStyleAndSubstance but matches on any one of a group of
	 * styles passed in at instantiation.
	 */
	public static class TestForStyleGroupAndSubstance implements IBlockTest {

		private final HashSet<NiceBlockStyle>	styles;
		private final int						substanceIndex;

		/**
		 * Just like TestForStyleAndSubstance but matches on any one of a group
		 * of styles passed in at instantiation. Substance taken from block
		 * state passed in.
		 */
		public TestForStyleGroupAndSubstance(IBlockState ibs, NiceBlockStyle... styles) {
			this.styles = new HashSet(Arrays.asList(styles));
			if (ibs.getBlock() instanceof NiceBlock) {
				substanceIndex = ibs.getValue(PROP_SUBSTANCE_INDEX);
			} else {
				substanceIndex = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockState ibs) {
			return ibs.getBlock() instanceof NiceBlock && styles.contains(((NiceBlock) ibs.getBlock()).style)
					&& ibs.getValue(PROP_SUBSTANCE_INDEX) == substanceIndex;
		}
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {

		return placementHandler.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);

	}
}
