package grondag.adversity.niceblock;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.support.ICollisionHandler;
import grondag.adversity.niceblock.support.NicePlacement;

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
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/**
 * Base class for Adversity building blocks. Should be instantiated and set up
 * in NiceBlockRegistrar.
 *
 * Note that NiceBlock metadata is ONLY used to define substance variants. For
 * blocks that require facing or other configuration that cannot be derived from
 * neighboring blocks, a different block instance is used. (Or a tile entity, if
 * needed to avoid an impractical number of instances.) In contrast, vanilla and
 * most mods use metadata for facing data on stairs, slabs, etc.
 *
 * NiceBlocks does it this way beause Adversity has MANY building blocks with
 * many variants and I wanted to avoid creating tile entities in most cases and
 * to be fully efficient in usage of metadata bits. If each NiceBlock instance
 * has 16 substance variants then no metadata bits are wasted. Slabs, stairs,
 * etc. do not necessarily consume four metadata bits and this also system means
 * all niceblocks can be fully consistent in the way they use metadata.
 */
public class NiceBlock extends Block {

	/** Index to the substances[] array. */
	public static final PropertyInteger PROP_SUBSTANCE_INDEX = PropertyInteger.create("substance_index", 0, 15);
	/**
	 * Used by NiceModel to select correct in-game variant. 385 is the max. Most
	 * blocks have fewer variants.
	 */
	public static final IUnlistedProperty PROP_MODEL_RECIPE = Properties.toUnlisted(PropertyInteger.create("model_recipe", 0, 385));
	/**
	 * Used by NiceModel to select an alternate in-game appearance when more
	 * than one is available. We don't use the MineCraft alternate functionality
	 * because it is non-deterministic for different block states. This causes
	 * undesirable changes to texture selection when neighbor blocks change.
	 */
	public static final IUnlistedProperty PROP_MODEL_ALTERNATE = Properties.toUnlisted(PropertyInteger.create("model_alternate", 0, 15));

	/**
	 * Maps metadata to specific Adversity substance. Metadata is the index to
	 * this array. Substance control texture and may be used to control some
	 * in-game properties and behaviors.
	 */
	public final NiceSubstance[] substances;

	/**
	 * Identifies the visual appearance of the block and handles aspects related
	 * to models, collision, extended state, placement, etc.
	 */
	public final NiceStyle style;

	/**
	 * Item for this block. Will have same substance variants as this block.
	 * Instantiated and retained here for convenience and to enable consistent
	 * handling.
	 */
	public final ItemMultiTexture item;

	/**
	 * Model randomizer for this block. These are cached in Alternator class, so
	 * no significant cost to keeping a reference in each block. Necessary
	 * because Vanilla alternate function apparently uses block state as input,
	 * causing textures to vary for the same position as extended state changes,
	 * which gives a jarring effect in some cases. Alternator only uses BlockPos
	 * as input.
	 */
	private final IAlternator alternator;

	/** Non-prefixed, unlocalized name of this block */
	public final String name;

	/**
	 * Handler for onBlockPlaced event. Given at instantiation. Allows player to
	 * build large multi-blocks with connected-texture blocks and handles
	 * appropriate placement of axis-oriented blocks, for example.
	 */
	private final NicePlacement placementHandler;

	/**
	 * CAN BE NULL! If non-null, blocks requires special collision handling,
	 * typically because it is not a standard cube shape. Retrieved from model
	 * cook book at instantiation and reference saved for simpler coding.
	 */
	public final ICollisionHandler collisionHandler;

	/**
	 * Assumes first substance is representative of all the substances for
	 * purposes of setting material-dependent attributes.
	 */
	public NiceBlock(String name, NiceStyle style, NicePlacement placer, NiceSubstance... substances) {
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
		collisionHandler = style.cookbook.getCollisionHandler();

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
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[] { PROP_SUBSTANCE_INDEX }, new IUnlistedProperty[] {
				PROP_MODEL_RECIPE, PROP_MODEL_ALTERNATE });
	}

	// BASIC METADATA MECHANICS

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

	// INTERACTION HANDLING

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(PROP_SUBSTANCE_INDEX);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos,
			EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {

		return placementHandler.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);

	}

	// RENDERING-RELATED THINGS AND STUFF
	// Note that some of the methods here are called server-side.
	// (Ray tracing and collisions, mainly.)

	@Override
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.SOLID;
	}

	// Used in NiceBlockStyle renderLayerFlags and NiceBlock.canRenderInLayer()
	// Would have expected that forge already defines these somewhere but
	// couldn't find them.
	public static final int LAYER_SOLID = 1;
	public static final int LAYER_CUTOUT_MIPPED = 2;
	public static final int LAYER_CUTOUT = 4;
	public static final int LAYER_TRANSLUCENT = 8;

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

	/**
	 * Determines which model should be displayed via PROP_MODEL_RECIPE.
	 * Handling is delegated to the style cook book.
	 */
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		// should always be an IExtendedBlockState but avoid crash if somehow
		// not
		if (state instanceof IExtendedBlockState) {
			return ((IExtendedBlockState) state).withProperty(PROP_MODEL_RECIPE,
					style.cookbook.getRecipeIndex((IExtendedBlockState) state, world, pos)).withProperty(PROP_MODEL_ALTERNATE,
							alternator.getAlternate(pos));
		} else {
			return state;
		}
	}

	/**
	 * Used by NiceBlockHighligher to know if custom hit box rendering is
	 * needed. Actual event handling is in that class. Override for blocks that
	 * need it.
	 */
	public boolean needsCustomHighlight() {
		return false;
	}

	/**
	 * Used by NiceBlockHighligher get custom hit boxes for rendering. Only used
	 * if needsCustomHighlight returns true.
	 */
	public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state) {
		if (collisionHandler == null) {
			return new ImmutableList.Builder()
					.add(new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ))
					.build();
		} else {
			return collisionHandler.getSelectionBoundingBoxes(worldIn, pos, state);
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
		if (collisionHandler == null) {
			return super.collisionRayTrace(worldIn, pos, start, end);
		} else {
			return collisionHandler.collisionRayTrace(worldIn, pos, start, end);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
		if (collisionHandler == null) {
			super.setBlockBoundsBasedOnState(worldIn, pos);
		} else {
			AxisAlignedBB aabb = collisionHandler.getCollisionBoundingBox(worldIn, pos, worldIn.getBlockState(pos));
			minX = aabb.minX;
			minY = aabb.minY;
			minZ = aabb.minZ;
			maxX = aabb.maxX;
			maxY = aabb.maxY;
			maxZ = aabb.maxZ;
		}
	}

	@Override
	public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
	{
		if (collisionHandler == null) {
			super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
		} else {
			collisionHandler.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
	{
		if (collisionHandler == null) {
			return super.getCollisionBoundingBox(worldIn, pos, state);
		} else {
			return collisionHandler.getCollisionBoundingBox(worldIn, pos, state);
		}
	}

	// BLOCK TESTS

	/**
	 * Blocks match if they have are the same block and same substance. Also
	 * implies the same style.
	 */
	public static class TestForCompleteMatch implements IBlockTest {

		private final Block block;
		private final int substanceIndex;

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

		private final int substanceIndex;

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

		private final NiceStyle style;

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

		private final NiceStyle style;
		private final int substanceIndex;

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

		private final HashSet<NiceStyle> styles;
		private final int substanceIndex;

		/**
		 * Just like TestForStyleAndSubstance but matches on any one of a group
		 * of styles passed in at instantiation. Substance taken from block
		 * state passed in.
		 */
		public TestForStyleGroupAndSubstance(IBlockState ibs, NiceStyle... styles) {
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

}
