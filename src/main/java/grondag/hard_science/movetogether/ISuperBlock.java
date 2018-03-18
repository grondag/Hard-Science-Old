package grondag.hard_science.movetogether;

import java.util.List;
import java.util.Random;

import grondag.exotic_matter.world.IBlockTest;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public interface ISuperBlock
{

    /**
     * Used for multiple purposes depending on the type of block. Thus the generic name.
     * Didn't find the block state property abstraction layer particularly useful for my purposes.
     */
    PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    /**
     * Contains state passed from getExtendedState to handleBlockState. Using a custom unlisted property because we need large int values and the vanilla implementation enumerates
     * all allowed values into a hashmap... Plus this hides the implementation from the block.
     */
    ModelStateProperty MODEL_STATE = new ModelStateProperty();

    /**
     * Factory for block test that should be used for border/shape joins
     * for this block.  Used in model state refresh from world.
     */
    IBlockTest<ISuperModelState> blockJoinTest(IBlockAccess worldIn, IBlockState state, BlockPos pos, ISuperModelState modelState);

    void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn,
            boolean p_185477_7_);

    boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager);

    boolean addHitEffects(IBlockState blockState, World world, RayTraceResult target, ParticleManager manager);

    void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced);

    boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles);

    /**
     * {@inheritDoc} <br><br>
     * 
     * Added by forge to allow better control over fence/wall/pane connections.
     * SuperBlocks determine connectivity with each other through other means
     * and are not going to be compatible with regular fences, panes, etc.
     * Always false for that reason.
     */
    boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing);

    boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos);

    boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side);

    /** 
     * {@inheritDoc} <br><br>
     * Mobs can't spawn on hypermatter.
     */
    boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type);

    /** 
     * {@inheritDoc} <br><br>
     * Hypermatter is indestructable by normal explosions.  
     * If it does blow up somehow it shouldn't drop as a block.
     */
    boolean canDropFromExplosion(Explosion explosionIn);

    boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity);

    /**
     * Accessed via state implementation.
     * Used to determine if an entity can spawn on this block.
     * Has no actual or extended state properties when referenced.
     * 
     * All superblocks allow spawning unless made of hypermatter.
     */
    boolean canEntitySpawn(IBlockState state, Entity entityIn);

    boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos);

    /**
     * This is queried before getActualState, which means it cannot be determined from world.
     * 
     * We could report that we render in all layers but return no quads.  However, this means RenderChunk
     * does quite a bit of work asking us for stuff that isn't there. 
     * 
     * Instead we persist it in the block instance and set block states that point to the
     * appropriate block instance for the model they represent.  This could force some block state changes in 
     * the world however if model state changes - but those changes are not likely.
     * Main drawback of this approach is that it consumes more block ids.
     * 
     * If any rendering is done by TESR, then don't render in any layer because too hard to
     * get render depth perfectly aligned that way.  TESR will also render normal block layers.
     */
    boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer);

    boolean canSilkHarvest();

    boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player);

    RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end);

    int damageDropped(IBlockState state);

    boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face);

    PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos);

    float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos);

    /** Only meaningful use is for itemRenderer which 
     * checks this to know if it should do depth checking on item renders.
     * Get no state here, so always report that we should.
     */
    BlockRenderLayer getBlockLayer();

    /**
     * Used in many places and seems to provide min/max bounds for rendering purposes.
     * For example, seems to determine at what height rain falls.
     * In most cases is same as collision bounding box.
     */
    AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos);

    AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos);

    /** 
     * Returns an instance of the default model state for this block.
     * Because model states are mutable, every call returns a new instance.
     */
    ISuperModelState getDefaultModelState();

    /**
     * Main reason for override is that we have to add NBT to stack for ItemBlock drops.
     * Also don't use fortune for our drops.
     */
    List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune);

    /**
     * Determines which model should be displayed via MODEL_STATE. 
     */
    IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos);

    /**
     * Would always return 0 anyway because we aren't in the list of encouragements that the Fire block maintains.
     */
    int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face);

    /** lowest-tier wood has a small chance of burning */
    int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face);

    ItemStack getItem(World worldIn, BlockPos pos, IBlockState state);

    /**
     * {@inheritDoc} <br><br>
     * 
     * DO NOT USE THIS FOR SUPERBLOCKS!
     * Use {@link #getStackFromBlock(IBlockState, IBlockAccess, BlockPos)} instead.
     * 
     * Also, yes, I overrode this method just to add this warning.
     */
    Item getItemDropped(IBlockState state, Random rand, int fortune);

    String getItemStackDisplayName(ItemStack stack);

    /**
     * Used by chunk for world lighting and to determine height map.
     * Blocks with 0 opacity are apparently ignored for height map generation.
     * 
     * 0 means fully transparent
     * values 1-15 are various degrees of opacity
     * 255 means fully opaque
     * values 16-254 have no meaning
     * 
     * Chunk uses location-dependent version if the chunk is loaded.
     * 
     * We return a non-zero estimate here which forces this block to be considered in sky/height maps.
     * Actual light value will generally be obtained via the location-dependent method.
     */
    int getLightOpacity(IBlockState state);

    /**
     * Location-dependent version of {@link #getLightOpacity(IBlockState)}
     * Gives more granular transparency information when chunk is loaded.
     * 
     * Any value over 0 prevents a block from seeing the sky.
     */
    int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos);

    /**
     * Number of supported meta values for this block.
     */
    int getMetaCount();

    /**
     * Usage of meta is overloaded and dependent on other aspects of state, so just storing the raw value.
     */
    int getMetaFromState(IBlockState state);

    /** 
     * If last parameter is false, does not perform a refresh from world for world-dependent state attributes.
     * Use this option to prevent infinite recursion when need to reference some static state )
     * information in order to determine dynamic world state. Block tests are main use case for false.
     * 
     * 
     */
    ISuperModelState getModelState(IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    /**
     * At least one vanilla routine passes in a block state that does not match world.
     * (After block updates, passes in previous state to detect collision box changes.) <br><br>
     * 
     * We don't want to update our current state based on stale block state, so for TE
     * blocks the refresh must be coded so we don't inject bad (stale) modelState into TE. <br><br>
     * 
     * However, we do want to honor the given world state if species is different than current.
     * We do this by directly changing species, because that is only thing that can changed
     * in model state based on block state, and also affects collision box. <br><br>
     * 
     * NOTE: there is probably still a bug here, because collision box can change based
     * on other components of model state (orthogonalAxis, for example) and those changes may not be detected
     * by path finding.
     */
    ISuperModelState getModelStateAssumeStateIsStale(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    /** 
     * Use when absolutely certain given block state is current.
     */
    ISuperModelState getModelStateAssumeStateIsCurrent(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    int getOcclusionKey(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side);

    ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player);

    /**
     * Used by NiceBlockHighligher to know if custom hit box rendering is needed. Actual event handling is in that class. 
     * Won't be called unless custom collision handler is available.
     */
    List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state);

    ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos);

    IBlockState getStateFromMeta(int meta);

    /**
     * {@inheritDoc} <br><br>
     * 
     * Confusingly named because is really the back end for Item.getSubItems.
     * Used by Creative and JEI to show a list of blocks.
     */
    void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items);

    List<ItemStack> getSubItems();

    /**
     * Controls material-dependent properties
     */
    BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos);

    BlockSubstance getSubstance(IBlockAccess world, BlockPos pos);

    /**
     * {@inheritDoc} <br><br>
     * 
     * Should be true if shape is not a full cube or fully transparent.
     */
    boolean getUseNeighborBrightness(IBlockState state);

    //overridden to allow for world-sensitive drops
    void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack);

    /** 
     * This is an egregious hack to avoid performance hit of instanceof.
     * (Based on performance profile results.) <br> <br>
     * 
     * Default value of {@link #associatedBlock} is set to this instance in constructor.
     * If not changed, will have same behavior as vanilla.
     * Change it to a reference value to have this block be recognized as part of a group.
     * Initially used for flow blocks so that they can be detected quickly.
     */
    boolean isAssociatedBlock(Block other);

    /**
     * {@inheritDoc} <br><br>
     * 
     * In a rare and ironic nod to vanilla magical thinking, Hypermatter acts as a beacon base.
     * I will almost certainly regret this.
     */
    boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon);

    /**
     * {@inheritDoc} <br><br>
     * 
     * Determines result of {@link #getAmbientOcclusionLightValue(IBlockState)}
     */
    boolean isBlockNormalCube(IBlockState state);

    BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face);

    boolean isBurning(IBlockAccess world, BlockPos pos);

    boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face);

    /** 
     * True if this is an instance of an IFlowBlock and also a filler block.
     * Avoids performance hit of casting to the IFlowBlock Interface.
     * (Based on performance profile results.)
     */
    boolean isFlowFiller();

    /** 
     * True if this is an instance of an IFlowBlock and also a height block.
     * Avoids performance hit of casting to the IFlowBlock Interface.
     * (Based on performance profile results.)
     */
    boolean isFlowHeight();

    /**
     * {@inheritDoc}
     * 
     * Accessed via state implementation.
     * Used in AI pathfinding, explosions and occlusion culling.
     * Has no actual or extended state properties when referenced. <br><br>
     * 
     * Vanilla implementation uses fullBlock instance variable,
     * which is derived at construction from {@link #isOpaqueCube(IBlockState)}
     */
    boolean isFullBlock(IBlockState state);

    /**
     * Used many places in rendering and AI. 
     * Must be true for block to cause suffocation.
     * Input state provided has no extended properties.
     * Result appears to be based on geometry - if block is a 
     * full 1.0 cube return true, false otherwise.
     * 
     * Is also used in derivation of {@link #isFullyOpaque(IBlockState)}
     * and {@link #isNormalCube(IBlockState)}
     */
    boolean isFullCube(IBlockState state);

    /**
     * With {@link #isSubstanceTranslucent(IBlockState)} makes all the block
     * test methods work when full location information not available.
     * 
     * Only addresses geometry - does this block fully occupy a 1x1x1 cube?
     * True if so. False otherwise.
     */
    boolean isGeometryFullCube(IBlockState state);

    boolean isHypermatter();

    /**
     * Only true for virtual blocks.  Prevents "instanceof" checking.
     */
    boolean isVirtual();

    /**
     * {@inheritDoc} <br><br>
     * 
     * Vanilla version will return false if material is not fully opaque.
     * Ours is based solely on geometry && solidity.
     * 
     */
    boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos);

    /**
     * {@inheritDoc} <br><br>
     * 
     * Value given for the default state is also used 
     * in Block constructor to determine value of fullBlock 
     * which in turn is used to determine initial value of lightOpacity.
     */
    boolean isOpaqueCube(IBlockState state);

    boolean isReplaceable(IBlockAccess worldIn, BlockPos pos);

    boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side);

    boolean isTranslucent(IBlockState state);

    void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn);

    /**
     * World-aware version called from getDrops because logic may need more than metadata.
     * Other versions (not overriden) should not be called.
     */
    int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state);

    /** should never be used */
    int quantityDropped(IBlockState state, int fortune, Random random);

    /** should never be used */
    int quantityDropped(Random random);

    /** should never be used */
    int quantityDroppedWithBonus(int fortune, Random random);

    boolean rotateBlock(World world, BlockPos pos, EnumFacing axis);

    ISuperBlock setAllowSilkHarvest(boolean allow);

    /**
     * Sets a drop other than this block if desired.
     */
    ISuperBlock setDropItem(Item dropItem);

    boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side);

}