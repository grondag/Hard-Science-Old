package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.support.ICollisionHandler;
import grondag.adversity.niceblock.support.NicePlacement;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.LanguageRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;

/**
 * Base class for Adversity building blocks. Should be instantiated and set up in NiceBlockRegistrar.
 *
 * Note that NiceBlock metadata is ONLY used to define meta variants. For blocks that require facing or other configuration that cannot be derived from neighboring blocks, a
 * different block instance is used. (Or a tile entity, if needed to avoid an impractical number of instances.) In contrast, vanilla and most mods use metadata for facing data on
 * stairs, slabs, etc.
 *
 * NiceBlocks does it this way beause Adversity has MANY building blocks with many variants and I wanted to avoid creating tile entities in most cases and to be fully efficient in
 * usage of metadata bits. If each NiceBlock instance has 16 meta variants then no metadata bits are wasted. Slabs, stairs, etc. do not necessarily consume four metadata bits and
 * this also system means all niceblocks can be fully consistent in the way they use metadata.
 */
public class NiceBlock extends Block
{

    /**
     * Used for multiple purposes depending on block style. Thus the generic name.
     */
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

    /**
     * Contains state passed from getExtendedState to handleBlockState. Using a custom unlisted property because we need large int values and the vanilla implementation enumerates
     * all allowed values into a hashmap... Plus this hides the implementation from the block.
     */
    public static final ModelStateProperty MODEL_STATE = new ModelStateProperty();

    /**
     * Controls material-dependent properties
     */
    public final BaseMaterial material;
    
    /** 
     * Combined with material name for localization and registration.
     */
    protected final String styleName;

    /**
     * Item for this block. Will have same meta variants as this block. Instantiated and retained here for convenience and to enable consistent handling.
     */
    public final NiceItemBlock item;

    /**
     * CAN BE NULL! If non-null, blocks requires special collision handling, typically because it is not a standard cube shape. Retrieved from model cook book at instantiation and
     * reference saved for simpler coding.
     */
    public final ICollisionHandler collisionHandler;

    public final BlockModelHelper blockModelHelper;

    public NiceBlock(BlockModelHelper blockModelHelper, BaseMaterial material, String styleName)
    {
        super(material.material);
        this.material = material;
        this.styleName = styleName;
        setCreativeTab(Adversity.tabAdversity);
        this.setHarvestLevel(material.harvestTool, material.harvestLevel);
        setStepSound(material.stepSound);
        setHardness(material.hardness);
        setResistance(material.resistance);
        this.blockModelHelper = blockModelHelper;
        blockModelHelper.setBlock(this);
        this.setRegistryName(material.materialName + "." + styleName);
        setUnlocalizedName(this.getRegistryName());
        collisionHandler = blockModelHelper.dispatcher.getCollisionHandler();

        item = new NiceItemBlock(this);

        // let registrar know to register us when appropriate
        NiceBlockRegistrar.allBlocks.add(this);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META }, new IUnlistedProperty[] { MODEL_STATE });
    }

    // BASIC METADATA MECHANICS

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
    {
        list.addAll(blockModelHelper.getSubItems());
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(META, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(META);
    }

    // LOCALIZATION
    @Override
    public String getLocalizedName()
    {
        return super.getLocalizedName();
    }

    // INTERACTION HANDLING

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(META);
    }

    // RENDERING-RELATED THINGS AND STUFF
    // Note that some of the methods here are called server-side.
    // (Ray tracing and collisions, mainly.)

    @Override
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.SOLID;
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return blockModelHelper.dispatcher.canRenderInLayer(layer);
    }

    private long elapsedTime;
    private int timerCount = 0;

    /**
     * Determines which model should be displayed via MODEL_STATE. Handling is delegated to the block model helper.
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return blockModelHelper.getExtendedState(state, world, pos);
    }

    /**
     * Used by NiceBlockHighligher to know if custom hit box rendering is needed. Actual event handling is in that class. Override for blocks that need it.
     */
    public boolean needsCustomHighlight()
    {
        return false;
    }

    /**
     * Used by NiceBlockHighligher get custom hit boxes for rendering. Only used if needsCustomHighlight returns true.
     */
    public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state)
    {
        if (collisionHandler == null)
        {
            return new ImmutableList.Builder().add(
                    new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ))
                    .build();
        }
        else
        {
            return collisionHandler.getSelectionBoundingBoxes(worldIn, pos, state);
        }
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        if (collisionHandler == null)
        {
            return super.collisionRayTrace(worldIn, pos, start, end);
        }
        else
        {
            return collisionHandler.collisionRayTrace(worldIn, pos, start, end);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        if (collisionHandler == null)
        {
            super.setBlockBoundsBasedOnState(worldIn, pos);
        }
        else
        {
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
        if (collisionHandler == null)
        {
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
        else
        {
            collisionHandler.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        if (collisionHandler == null)
        {
            return super.getCollisionBoundingBox(worldIn, pos, state);
        }
        else
        {
            return collisionHandler.getCollisionBoundingBox(worldIn, pos, state);
        }
    }


    /**
     * Implemented only for particle support.
     * Block model quads are pre-colored with tintIndex = 0,
     * so this should not be called for block rendering.
     */
    @Override
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        ModelState modelState = this.blockModelHelper.getModelStateForBlock(worldIn.getBlockState(pos), worldIn, pos, false);
        return this.blockModelHelper.dispatcher.getColorProvider().getColor(modelState.getColorIndex()).getColorMap(EnumColorMap.BASE);
    }

    // BLOCK TESTS

    // /**
    // * Blocks match if they have are the same block and same substance. Also
    // * implies the same style.
    // */
    // public static class TestForCompleteMatch implements IBlockTest {
    //
    // private final Block block;
    // private final int meta;
    //
    // /**
    // * Blocks match if they have are the same block and same substance. Also
    // * implies the same style. Pass in the state of the block you want to
    // * match with.
    // */
    // public TestForCompleteMatch(IBlockState ibs) {
    // block = ibs.getBlock();
    // meta = ibs.getValue(META);
    // }
    //
    // @Override
    // public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
    // TileEntity te = world.getTileEntity(pos);
    // if(te != null && te instanceof NiceTileEntity && ((NiceTileEntity)te).isDeleted){
    // Adversity.log.info("caught deleted at" + pos.toString());
    // return false;
    // }
    // return ibs.getBlock() == block && ibs.getValue(META) == meta;
    // }
    // }
    //
    // /**
    // * Blocks match if they are of the same substance. Can be different styles
    // * or blocks. Substance taken from the blockstate parameter.
    // */
    // public static class TestForSubstance implements IBlockTest {
    //
    // private final int meta;
    //
    // public TestForSubstance(IBlockState ibs) {
    // if (ibs.getBlock() instanceof NiceBlock) {
    // meta = ibs.getValue(META);
    // } else {
    // meta = -1;
    // }
    // }
    //
    // @Override
    // public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
    // return ibs.getBlock() instanceof NiceBlock && ibs.getValue(META) == meta;
    // }
    // }
    //
    // /**
    // * Blocks match if they have have the same style. Can be different
    // * substances or blocks.
    // */
    // public static class TestForStyle implements IBlockTest {
    //
    // private final NiceStyle style;
    //
    // /**
    // * Blocks match if they have have the same style. Can be different
    // * substances or blocks. Style taken from blockstate parameter.
    // */
    // public TestForStyle(IBlockState ibs) {
    // if (ibs.getBlock() instanceof NiceBlock) {
    // style = ((NiceBlock) ibs.getBlock()).style;
    // } else {
    // style = null;
    // }
    // }
    //
    // @Override
    // public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
    // return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style;
    // }
    // }
    //
    // /**
    // * Blocks match if they have have the same style and subtance. Can be
    // * different blocks.
    // */
    // public static class TestForStyleAndSubstance implements IBlockTest {
    //
    // private final NiceStyle style;
    // private final int meta;
    //
    // /**
    // * Blocks match if they have have the same style and subtance. Can be
    // * different blocks. Style and subtance taken from blockstate parameter.
    // */
    // public TestForStyleAndSubstance(IBlockState ibs) {
    // if (ibs.getBlock() instanceof NiceBlock) {
    // style = ((NiceBlock) ibs.getBlock()).style;
    // meta = ibs.getValue(META);
    // } else {
    // style = null;
    // meta = -1;
    // }
    // }
    //
    // @Override
    // public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
    // return ibs.getBlock() instanceof NiceBlock && ((NiceBlock) ibs.getBlock()).style == style
    // && ibs.getValue(META) == meta;
    // }
    // }
    //
    // /**
    // * Just like TestForStyleAndSubstance but matches on any one of a group of
    // * styles passed in at instantiation.
    // */
    // public static class TestForStyleGroupAndSubstance implements IBlockTest {
    //
    // private final HashSet<NiceStyle> styles;
    // private final int meta;
    //
    // /**
    // * Just like TestForStyleAndSubstance but matches on any one of a group
    // * of styles passed in at instantiation. Substance taken from block
    // * state passed in.
    // */
    // public TestForStyleGroupAndSubstance(IBlockAccess world, IBlockState ibs, BlockPos pos, NiceStyle... styles) {
    // this.styles = new HashSet(Arrays.asList(styles));
    // if (ibs.getBlock() instanceof NiceBlock) {
    // meta = ibs.getValue(META);
    // } else {
    // meta = -1;
    // }
    // }
    //
    // @Override
    // public boolean testBlock(IBlockAccess world, IBlockState ibs, BlockPos pos) {
    // return ibs.getBlock() instanceof NiceBlock && styles.contains(((NiceBlock) ibs.getBlock()).style)
    // && ibs.getValue(META) == meta;
    // }
    // }
}
