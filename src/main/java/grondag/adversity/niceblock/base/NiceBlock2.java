package grondag.adversity.niceblock.base;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlockRegistrar2;
import grondag.adversity.niceblock.modelstate.ModelKeyProperty;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.ICollisionHandler;

import java.util.ArrayList;
import java.util.List;

//import mcp.mobius.waila.api.IWailaConfigHandler;
//import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import gnu.trove.map.hash.TLongObjectHashMap;

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
 * 
 * NB: Vanilla Bug / Feature
 * If block getLightValue > 0 and lightOpacity > 0
 * then block stays lit when nearby light sources are removed.
 */
public class NiceBlock2 extends Block // implements IWailaProvider
{

    /**
     * Used for multiple purposes depending on block style. Thus the generic name.
     */
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

    /**
     * Contains state passed from getExtendedState to handleBlockState. Using a custom unlisted property because we need large int values and the vanilla implementation enumerates
     * all allowed values into a hashmap... Plus this hides the implementation from the block.
     */
    public static final ModelKeyProperty MODEL_KEY = new ModelKeyProperty();

    /**
     * Controls material-dependent properties
     */
    public final BaseMaterial material;
    
    /** 
     * Combined with material name for localization and registration.
     */
    private final String styleName;

    /**
     * Use in UI
     */
    private final String displayName;
    /**
     * Item for this block. Will have same meta variants as this block. Instantiated and retained here for convenience and to enable consistent handling.
     */
    public final NiceItemBlock2 item;

    /**
     * CAN BE NULL! If non-null, blocks requires special collision handling, typically because it is not a standard cube shape. Retrieved from model cook book at instantiation and
     * reference saved for simpler coding.
     */
    public final ICollisionHandler collisionHandler;
    
    //TODO: replace with cacheloader
    /**
     * Cache collision box lists.
     * Override getCollisionHandler and getModelBounds if need something 
     * other than standard cubes.  Has to be here and not in parent because
     * the number of models is specific to cookbook.
     * 
     * NULL if collisionHandler is NULL.
     */
    private final TLongObjectHashMap<List<AxisAlignedBB>> modelBounds;
    
    //TODO: replace with cacheloader
    /**
     * Cached union of model bounds.  NULL if collisionHandler is NULL.  
     */
    private final TLongObjectHashMap<AxisAlignedBB> combinedBounds;

    public final ModelDispatcher2 dispatcher;
    
    private boolean isGlowing;

    public NiceBlock2(ModelDispatcher2 dispatcher, BaseMaterial material, String styleName)
    {
        this(dispatcher, material, styleName, false);
    }
    
    public NiceBlock2(ModelDispatcher2 dispatcher, BaseMaterial material, String styleName, boolean isGlowing)
    {
        super(material.material);
        this.material = material;
        this.isGlowing = isGlowing;
        this.styleName = styleName;
        setCreativeTab(Adversity.tabAdversity);
        this.setHarvestLevel(material.harvestTool, material.harvestLevel);
        setStepSound(material.stepSound);
        setHardness(material.hardness);
        setResistance(material.resistance);
        this.dispatcher = dispatcher;
        this.setRegistryName(material.materialName + "." + styleName);
        this.setUnlocalizedName(this.getRegistryName().toString());
        
        String makeName = I18n.translateToLocal(getStyleName());
        if(makeName == null || makeName == "") makeName = getStyleName();
        displayName = makeName + " " + I18n.translateToLocal(material.materialName); 

        collisionHandler = dispatcher.getCollisionHandler();
        modelBounds = collisionHandler == null ? null : new TLongObjectHashMap<List<AxisAlignedBB>>();
        combinedBounds = collisionHandler == null ? null :  new TLongObjectHashMap<AxisAlignedBB>();

        item = new NiceItemBlockColor(this);

        // let registrar know to register us when appropriate
        NiceBlockRegistrar2.allBlocks.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META }, new IUnlistedProperty[] { MODEL_KEY });
    }

    // BASIC METADATA MECHANICS

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.addAll(item.getSubItems());
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

    public String getDisplayName()
    {
    	return displayName;
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

    /** Only meaningful use is for itemRenderer which 
    * checks this to know if it should do depth checking on item renders
    */
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return dispatcher.canRenderInLayer(BlockRenderLayer.TRANSLUCENT)
        		? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer)
    {
        return dispatcher.canRenderInLayer(layer);
    }

    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return this.isGlowing ? 15 << 20 | 15 << 4 : super.getPackedLightmapCoords(state, source, pos);
    }
  
//  private long elapsedTime;
//  private int timerCount = 0;

	/**
     * Determines which model should be displayed via MODEL_STATE. Handling is delegated to the block model helper.
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ((IExtendedBlockState)state).withProperty(MODEL_KEY, dispatcher.getRefreshedKeyFromWorld(0, this, state, world, pos));
    }

    /**
     * Used by NiceBlockHighligher to know if custom hit box rendering is needed. Actual event handling is in that class. Override for blocks that need it.
     */
    public boolean needsCustomHighlight()
    {
        return this.collisionHandler != null;
    }

    /**
     * Gets bounding box list for given state.
     * Should never be called unless collisionHandler is non-null.
     */
    private List<AxisAlignedBB> getCachedModelBounds(long collisionKey)
    {
        if(collisionHandler == null) return java.util.Collections.emptyList();
        
        List<AxisAlignedBB> retVal = modelBounds.get(collisionKey);
        
        if(retVal == null)
        {
            retVal = collisionHandler.getModelBounds(collisionKey);
            modelBounds.put(collisionKey, retVal);
        }

        return retVal;
    }
    
  
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
    {
        if (collisionHandler == null)
        {
        	super.addCollisionBoxToList(state, worldIn, pos, mask, list, collidingEntity);
        }
        else
        {
            AxisAlignedBB localMask = mask.offset(-pos.getX(), -pos.getY(), -pos.getZ());
            
            List<AxisAlignedBB> bounds = getCachedModelBounds(collisionHandler.getCollisionKey(worldIn, pos, state));
 
            for (AxisAlignedBB aabb : bounds) {
                if (localMask.intersectsWith(aabb)) 
                {
                    list.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
                }
            }        
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        if (collisionHandler == null)
        {
            return super.getCollisionBoundingBox(state, worldIn, pos);
        }
        else
        {
            long collisionKey = collisionHandler.getCollisionKey(worldIn, pos, state);
            AxisAlignedBB retVal = this.combinedBounds.get(collisionKey);
            
            if(retVal == null)
            {
                for (AxisAlignedBB aabb : this.getCachedModelBounds(collisionHandler.getCollisionKey(worldIn, pos, state))) 
                {
                  retVal = retVal == null ? aabb : retVal.union(aabb);
                }
                combinedBounds.put(collisionKey, retVal);
            }
            
            return retVal;
        }
    }
    
    /** won't be called unless getCollisionHandler is overriden */
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        if (collisionHandler == null)
        {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        else
        {
            ArrayList<AxisAlignedBB> bounds = new ArrayList<AxisAlignedBB>();
        
            addCollisionBoxToList(blockState, worldIn, pos, 
                    new AxisAlignedBB(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord),
                    bounds, null);
    
            RayTraceResult retval = null;
            double distance = 1;
    
            for (AxisAlignedBB aabb : bounds) {
                RayTraceResult candidate = aabb.calculateIntercept(start, end);
                if (candidate != null) {
                    double checkDist = candidate.hitVec.squareDistanceTo(start);
                    if (retval == null || checkDist < distance) {
                        retval = candidate;
                        distance = checkDist;
                    }
                }
            }
    
            return retval == null ? null : new RayTraceResult(retval.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), retval.sideHit, pos);
        }
    }

    /**
     * Used by NiceBlockHighligher to know if custom hit box rendering is needed. Actual event handling is in that class. 
     * Won't be called unless custom collision handler is available.
     */
    public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state) {

        if (collisionHandler == null)
        {
            return new ImmutableList.Builder<AxisAlignedBB>().add(this.getBoundingBox(state, worldIn, pos)).build();
        }
        else
        {
            Builder<AxisAlignedBB> builder = new ImmutableList.Builder<AxisAlignedBB>();
    
            for (AxisAlignedBB aabb : this.getCachedModelBounds(collisionHandler.getCollisionKey(worldIn, pos, state))) {
                builder.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
            }
            return builder.build();
        }
    }

	public String getStyleName() {
		return styleName;
	}



//    @Override
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
//    {
//        return this.blockModelHelper.getWailaBody(itemStack, currenttip, accessor, config);
//    }

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
