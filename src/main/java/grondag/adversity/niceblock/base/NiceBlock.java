package grondag.adversity.niceblock.base;

import grondag.adversity.Adversity;
import grondag.adversity.external.IWailaProvider;
import grondag.adversity.library.cache.ManagedLoadingCache;
import grondag.adversity.library.cache.SimpleCacheLoader;
import grondag.adversity.niceblock.NiceBlockRegistrar;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import grondag.adversity.niceblock.modelstate.ModelKeyProperty;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.ICollisionHandler;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

//import mcp.mobius.waila.api.IWailaConfigHandler;
//import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;


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
public class NiceBlock extends Block implements IWailaProvider
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
     * CAN BE NULL! If non-null, blocks requires special collision handling, typically because it is not a standard cube shape. Retrieved from model cook book at instantiation and
     * reference saved for simpler coding.
     */
    public final ICollisionHandler collisionHandler;
    
    /**
     * Cache for collision box info.
     * NULL if collisionHandler is NULL.
     */
    private final ManagedLoadingCache<ModelBounds> modelBounds;

    public final ModelDispatcher dispatcher;
        
    /** non-null if this drops something other than itself */
    private Item dropItem;
    
    /** Allow silk harvest. Defaults true. Use setAllowSilk to change */
    private boolean allowSilkHarvest = true;
    
    public NiceBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName)
    {
        super(material.material);
        this.material = material;
        this.styleName = styleName;
        setCreativeTab(Adversity.tabAdversity);
        this.setHarvestLevel(material.harvestTool, material.harvestLevel);
        setSoundType(material.stepSound);
        setHardness(material.hardness);
        setResistance(material.resistance);
        this.dispatcher = dispatcher;
        this.setRegistryName(material.materialName + "." + styleName);
        this.setUnlocalizedName(this.getRegistryName().toString());

        
        String makeName = I18n.translateToLocal(getStyleName());
        if(makeName == null || makeName == "") makeName = getStyleName();
        displayName = makeName + " " + I18n.translateToLocal(material.materialName); 

        collisionHandler = dispatcher.getCollisionHandler();
        modelBounds = collisionHandler == null ? null : new ManagedLoadingCache<ModelBounds>(new BoundsLoader(), 0xF, 0xFFF);

        // let registrar know to register us when appropriate
        NiceBlockRegistrar.allBlocks.add(this);
    }
    
    private static class ModelBounds
    {
        private final List<AxisAlignedBB> boundsList;
        private AxisAlignedBB combinedBounds;
        
        private ModelBounds(List<AxisAlignedBB> boundsList)
        {
            this.boundsList = boundsList;
        }
        
        public List<AxisAlignedBB> getBounds() { return boundsList; }
        
        public AxisAlignedBB getCombinedBounds()
        {
            if(combinedBounds == null)
            {
                for (AxisAlignedBB aabb : this.boundsList) 
                {
                    combinedBounds = combinedBounds == null ? aabb : combinedBounds.union(aabb);
                }
            }
            return combinedBounds;
        }
    }
    
    private class BoundsLoader implements SimpleCacheLoader<ModelBounds>
    {

        @Override
        public ModelBounds load(long key)
        {
            return new ModelBounds(NiceBlock.this.collisionHandler.getModelBounds(key));
        }
        
    }
    
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META }, new IUnlistedProperty[] { MODEL_KEY });
    }

    // BASIC METADATA MECHANICS

    //only display one item meta variant for item search
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(getSubItems().get(0));
    }
    
    public List<ItemStack> getSubItems()
    {
        ModelColorMapComponent colorMap = dispatcher.getStateSet().getFirstColorMapComponent();
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < 16; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            long key = dispatcher.getStateSet().computeKey(colorMap.createValueFromBits(this.material.defaultColorMapID));
            NiceItemBlock.setModelStateKey(stack, key);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
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
    
    public int getMetaForPlacedBlockFromStack(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        return stack.getMetadata();
    }
    
    public boolean isItemUsageAdditive(World worldIn, BlockPos pos, ItemStack stack)
    {
        return false;
    }

    // LOCALIZATION
    @Override
    public String getLocalizedName()
    {
        return displayName;
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
    	return displayName;
    }
    
    // INTERACTION HANDLING

    
    /**
     * True if block has a configurable appearance.
     */
    public boolean hasAppearanceGui()
    {
        return false;
    }
    
    /**
     * Sets a drop other than this block if desired.
     */
    public NiceBlock setDropItem(Item dropItem)
    {
        this.dropItem = dropItem;
        return this;
    }
    
    public NiceBlock setAllowSilkHarvest(boolean allow)
    {
        this.allowSilkHarvest = allow;
        return this;
    }
    
    @Override
    public boolean canSilkHarvest()
    {
        return allowSilkHarvest;
    }
    
    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return this.canSilkHarvest();
    }
    
    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(META);
    }

    //overridden to allow for world-sensitive drops
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) 
    {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.025F);

        if (this.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
        {
            java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
            
            //this is the part that is different from Vanilla
            ItemStack itemstack = getStackFromBlock(state, worldIn, pos);

            if (itemstack != null)
            {
                items.add(itemstack);
            }

            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
            for (ItemStack item : items)
            {
                spawnAsEntity(worldIn, pos, item);
            }
        }
        else
        {
            harvesters.set(player);
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            this.dropBlockAsItem(worldIn, pos, state, i);
            harvesters.set(null);
        }
    }
    
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {

        List<ItemStack> ret = new java.util.ArrayList<ItemStack>();

        if(this.dropItem == null)
        {
            ItemStack stack = getStackFromBlock(state, world, pos);
            if(stack != null)
            {
                ret.add(stack);
            }
        }
        else
        {
            int count = quantityDropped(world, pos, state);
            ret.add(new ItemStack(this.dropItem, count, 0));
        }
        return ret;
    }
    
    /**
     * Need a world-aware version because may need more than metadata
     */
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        return 1;
    }
    
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getSubItems().get(this.damageDropped(state));
    }
    
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        //Do not trust the state passed in, because WAILA passes in a default state.
        //Doing so causes us to pass in bad meta value which determines a bad model key 
        //which is then cached, leading to strange render problems for blocks just placed up updated.
        IBlockState goodState = world.getBlockState(pos);

        return getStackFromBlock(goodState, world, pos);
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

//  private long elapsedTime;
//  private int timerCount = 0;

	/**
     * Determines which model should be displayed via MODEL_KEY. 
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
//        Adversity.log.info("getExtendedState meta=" + state.getValue(NiceBlock.META) + " pos=" + pos.toString());
        return ((IExtendedBlockState)state).withProperty(MODEL_KEY, getModelStateKey(state, world, pos));
    }

    public long getModelStateKey(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return dispatcher.getStateSet().getRefreshedKeyFromWorld(0, true, this, state, world, pos);
    }

    public ModelStateSetValue getModelState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return dispatcher.getStateSet().getSetValueFromBits(getModelStateKey(state, world, pos));
    }
    
    public int getOcclusionKey(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return dispatcher.getOcclusionKey(this.getModelStateKey(state, world, pos), side);
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
    protected List<AxisAlignedBB> getCachedModelBounds(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        if(collisionHandler == null) return java.util.Collections.emptyList();
        
        long key = collisionHandler.getCollisionKey(state, worldIn, pos);
        
        return modelBounds.get(key).boundsList;
    
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
            
            List<AxisAlignedBB> bounds = getCachedModelBounds(state, worldIn, pos);
 
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
            //TODO: waaaaay too slow for some blocks to handle it this way
            long collisionKey = collisionHandler.getCollisionKey(state, worldIn, pos);
            return this.modelBounds.get(collisionKey).getCombinedBounds();
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
    
            for (AxisAlignedBB aabb : this.getCachedModelBounds(state, worldIn, pos)) {
                builder.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
            }
            return builder.build();
        }
    }

	public String getStyleName() {
		return styleName;
	}

    /** 
     * True if this is an instance of an IFlowBlock and also a filler block.
     * Avoids performance hit of casting to the IFlowBlock Interface.
     * (Based on performance profile results.)
     */
    public boolean isFlowFiller()
    {
        return false;
    }

    /** 
     * True if this is an instance of an IFlowBlock and also a height block.
     * Avoids performance hit of casting to the IFlowBlock Interface.
     * (Based on performance profile results.)
     */
    public boolean isFlowHeight()
    {
        return false;
    }
	
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return Collections.emptyList();
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
