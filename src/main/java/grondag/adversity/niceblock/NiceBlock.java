package grondag.adversity.niceblock;

import grondag.adversity.Adversity;
import grondag.adversity.library.Alternator;
import grondag.adversity.library.IAlternator;
import grondag.adversity.library.IBlockTest;
import grondag.adversity.niceblock.model.ModelRenderProperty;
import grondag.adversity.niceblock.model.ModelRenderState;
import grondag.adversity.niceblock.support.ICollisionHandler;
import grondag.adversity.niceblock.support.NicePlacement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
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
 * Note that NiceBlock metadata is ONLY used to define meta variants. For
 * blocks that require facing or other configuration that cannot be derived from
 * neighboring blocks, a different block instance is used. (Or a tile entity, if
 * needed to avoid an impractical number of instances.) In contrast, vanilla and
 * most mods use metadata for facing data on stairs, slabs, etc.
 *
 * NiceBlocks does it this way beause Adversity has MANY building blocks with
 * many variants and I wanted to avoid creating tile entities in most cases and
 * to be fully efficient in usage of metadata bits. If each NiceBlock instance
 * has 16 meta variants then no metadata bits are wasted. Slabs, stairs,
 * etc. do not necessarily consume four metadata bits and this also system means
 * all niceblocks can be fully consistent in the way they use metadata.
 */
public class NiceBlock extends Block {

	/** 
	 * Used for multiple purposes depending on block style.
	 * Thus the generic name.
	 */
	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

	/**
	 * Contains render state passed from getExtendedState to handleBlockState.
	 * Using a custom unlisted property because we need large int values and the
	 * vanilla implementation enumerates all allowed values into a hashmap...
	 * Plus this hides the implementation of render state from the block.
	 */
	public static final ModelRenderProperty MODEL_RENDER_STATE = new ModelRenderProperty();
	
	/**
	 * Controls material-dependent properties
	 */
	public final BaseMaterial material;

	/**
	 * Identifies the visual appearance of the block and handles aspects related
	 * to models, collision, extended state, placement, etc.
	 */
	public final NiceStyle style;

	/**
	 * Item for this block. Will have same meta variants as this block.
	 * Instantiated and retained here for convenience and to enable consistent
	 * handling.
	 */
	public final ItemMultiTexture item;

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

	/** number of meta variants to create.  Max is 16. */
	public final int metaCount;
	
	/**
	 * Assumes first substance is representative of all the substances for
	 * purposes of setting material-dependent attributes.
	 */
	public NiceBlock(String name, NiceStyle style, NicePlacement placer, BaseMaterial material, int metaCount) {
		super(material.material);
		this.style = style;
		this.material = material;
		this.metaCount = metaCount;
		this.name = name;
		setUnlocalizedName(Adversity.MODID + ":" + name);
		setCreativeTab(Adversity.tabAdversity);
		this.setHarvestLevel(material.harvestTool, material.harvestLevel);
		setStepSound(material.stepSound);
		setHardness(material.hardness);
		setResistance(material.resistance);
		placementHandler = placer;
		placer.setOwner(this);
		collisionHandler = style.getModelController().getCollisionHandler();

		item = new ItemMultiTexture(this, this, new Function<ItemStack, String>() {
			@Override
			public String apply(ItemStack stack) {
				return String.valueOf(stack.getMetadata());
			}
		});

		// let registrar know to register us when appropriate
		NiceBlockRegistrar.allBlocks.add(this);
	}

//	@Override
//	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
//		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
//		if(worldIn.isRemote){
//			TileEntity target = worldIn.getTileEntity(pos);
//			if(target != null && target instanceof NiceTileEntity)
//			{
//				((NiceTileEntity)target).dirtyNeighbors();
//				Adversity.log.info("dirtying @" + pos.toString());
//			}
//
//		}
//		
//	}

	
	@Override
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[] { META }, new IUnlistedProperty[] {
				MODEL_RENDER_STATE});
	}

	// BASIC METADATA MECHANICS

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
		for (int i = 0; i < metaCount; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(META, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	// INTERACTION HANDLING

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(META);
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

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
		return style.getModelController().canRenderInLayer(layer);
	}

	/**
	 * Determines which model should be displayed via PROP_MODEL_RECIPE.
	 * Handling is delegated to the style cook book.
	 */
	
	private static long elapsedTime;
	private static int timerCount = 0;

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		
		long start = System.nanoTime();
		
		// should always be an IExtendedBlockState but avoid crash if somehow not
		if (state instanceof IExtendedBlockState) {
			ModelRenderState renderState = style.getModelController().getRenderState((IExtendedBlockState) state, world, pos);
			state = ((IExtendedBlockState)state).withProperty( NiceBlock.MODEL_RENDER_STATE, renderState);
   		}
		
		long end = System.nanoTime();
		timerCount++;

		elapsedTime += (end - start);
		if((timerCount & 0x800) == 0x800){
			Adversity.log.info("average getExtendedState =" +  elapsedTime / (timerCount) );
			timerCount = 0;
			elapsedTime = 0;
		}
		return state;
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
		private final int meta;

		/**
		 * Blocks match if they have are the same block and same substance. Also
		 * implies the same style. Pass in the state of the block you want to
		 * match with.
		 */
		public TestForCompleteMatch(IBlockState ibs) {
			block = ibs.getBlock();
			meta = ibs.getValue(META);
		}

		@Override
		public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
			TileEntity te = world.getTileEntity(pos);
			if(te != null && te instanceof NiceTileEntity && ((NiceTileEntity)te).isDeleted){
				Adversity.log.info("caught deleted at" + pos.toString());
				return false;
			}
			return ibs.getBlock() == block && ibs.getValue(META) == meta;
		}
	}

	/**
	 * Blocks match if they are of the same substance. Can be different styles
	 * or blocks. Substance taken from the blockstate parameter.
	 */
	public static class TestForSubstance implements IBlockTest {

		private final int meta;

		public TestForSubstance(IBlockState ibs) {
			if (ibs.getBlock() instanceof NiceBlock) {
				meta = ibs.getValue(META);
			} else {
				meta = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
			return ibs.getBlock() instanceof NiceBlock && ibs.getValue(META) == meta;
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
		public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
			return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style;
		}
	}

	/**
	 * Blocks match if they have have the same style and subtance. Can be
	 * different blocks.
	 */
	public static class TestForStyleAndSubstance implements IBlockTest {

		private final NiceStyle style;
		private final int meta;

		/**
		 * Blocks match if they have have the same style and subtance. Can be
		 * different blocks. Style and subtance taken from blockstate parameter.
		 */
		public TestForStyleAndSubstance(IBlockState ibs) {
			if (ibs.getBlock() instanceof NiceBlock) {
				style = ((NiceBlock) ibs.getBlock()).style;
				meta = ibs.getValue(META);
			} else {
				style = null;
				meta = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
			return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style
					&& ibs.getValue(META) == meta;
		}
	}

	/**
	 * Just like TestForStyleAndSubstance but matches on any one of a group of
	 * styles passed in at instantiation.
	 */
	public static class TestForStyleGroupAndSubstance implements IBlockTest {

		private final HashSet<NiceStyle> styles;
		private final int meta;

		/**
		 * Just like TestForStyleAndSubstance but matches on any one of a group
		 * of styles passed in at instantiation. Substance taken from block
		 * state passed in.
		 */
		public TestForStyleGroupAndSubstance(IBlockAccess world, IBlockState ibs, BlockPos pos, NiceStyle... styles) {
			this.styles = new HashSet(Arrays.asList(styles));
			if (ibs.getBlock() instanceof NiceBlock) {
				meta = ibs.getValue(META);
			} else {
				meta = -1;
			}
		}

		@Override
		public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
			return ibs.getBlock() instanceof NiceBlock && styles.contains(((NiceBlock) ibs.getBlock()).style)
					&& ibs.getValue(META) == meta;
		}
	}

}
