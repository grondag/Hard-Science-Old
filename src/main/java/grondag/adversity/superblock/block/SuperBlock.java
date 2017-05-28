package grondag.adversity.superblock.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import grondag.adversity.Adversity;
import grondag.adversity.external.IWailaProvider;
import grondag.adversity.init.ModModels;
import grondag.adversity.library.Color;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.library.Color.EnumHCLFailureMode;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.niceblock.support.BlockTests;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import grondag.adversity.superblock.model.state.ModelStateProperty;
import grondag.adversity.superblock.model.state.Translucency;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
public abstract class SuperBlock extends Block implements IWailaProvider, IProbeInfoAccessor
{

    /**
     * Used for multiple purposes depending on the type of block. Thus the generic name.
     * Didn't find the block state property abstraction layer particularly useful for my purposes.
     */
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

    /**
     * Used to find the appropriate dispatcher delegate so that full-brightness render layers aren't rendered with AO shading.
     * Needed after getBlockState and after getActualState but before getExtendedState, 
     * so must be a regular property but is not saved to meta.
     */
    public static final PropertyInteger SHADE_FLAGS = PropertyInteger.create("shade_flags", 0, ModelState.BENUMSET_RENDER_LAYER.combinationCount() - 1);

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
    private final String styleName;

    /** non-null if this drops something other than itself */
    private Item dropItem;

    /** Allow silk harvest. Defaults true. Use setAllowSilk to change */
    private boolean allowSilkHarvest = true;

    // set in constructor to have different appearance
    protected int[] defaultModelStateBits;

    // change in constructor to have fewer variants
    protected int metaCount = 16;

    @SuppressWarnings("deprecation")
    public SuperBlock(String styleName, BaseMaterial material)
    {
        super(material.material);
        this.material = material;
        this.styleName = styleName;
        setCreativeTab(Adversity.tabAdversity);
        this.setHarvestLevel(material.harvestTool, material.harvestLevel);
        setSoundType(material.stepSound);
        setHardness(material.hardness);
        setResistance(material.resistance);
        this.setRegistryName(styleName + "_" + material.materialName);
        this.setUnlocalizedName(this.getRegistryName().toString());
        
        // see getLightOpacity(IBlockState)
        this.lightOpacity = material.isTranslucent ? 0 : 255;

        ModelState defaultState = new ModelState();
        defaultState.setShape(ModelShape.CUBE);
        BlockRenderLayer baseRenderLayer = this.material.isTranslucent
                ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
        defaultState.setRenderLayer(PaintLayer.BASE, baseRenderLayer);
        defaultState.setRenderLayer(PaintLayer.CUT, baseRenderLayer);
        defaultState.setRenderLayer(PaintLayer.LAMP, baseRenderLayer);
        this.defaultModelStateBits = defaultState.getBitsIntArray();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
            Entity entityIn, boolean p_185477_7_)
    {
        ModelState modelState = this.getModelState(state, worldIn, pos, true);

        AbstractCollisionHandler collisionHandler = modelState.getShape().meshFactory().collisionHandler();

        if (collisionHandler == null)
        {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
        else
        {
            AxisAlignedBB localMask = entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ());

            List<AxisAlignedBB> bounds = collisionHandler.getCollisionBoxes(state, worldIn, pos, modelState);

            for (AxisAlignedBB aabb : bounds) {
                if (localMask.intersectsWith(aabb)) 
                {
                    collidingBoxes.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
                }
            }        
        }
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
    {
        // TODO have to override this to get correct particle color/texture
        return super.addDestroyEffects(world, pos, manager);
    }

    @Override
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager)
    {
        // TODO have to override this to get correct particle color/texture
        return super.addHitEffects(state, worldObj, target, manager);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        ModelState modelState = SuperItemBlock.getModelState(stack);
        
        ColorMap colorMap = modelState.getColorMap(PaintLayer.BASE);
        if(colorMap != null)
        {
            tooltip.add("Color: " + colorMap.colorMapName);
        }
        
        int placementShape = SuperItemBlock.getStackPlacementShape(stack);
        if(placementShape != 0)
        {
            tooltip.add(String.format("Block Size: %1$d x %2$d x %3$d", placementShape & 0xFF, (placementShape >> 8) & 0xFF, (placementShape >> 16) & 0xFF));
        }
        
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles)
    {
        // TODO have to override this to get correct particle color/texture
        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    {
        ModelState modelState = this.getModelState(blockState, world, data.getPos(), true);
        
        if(modelState != null)
        {
            probeInfo.text("Shape: " + modelState.getShape());
            probeInfo.text("Base Color: " + modelState.getColorMap(PaintLayer.BASE).colorMapName);
            if(modelState.hasSpecies()) 
            {
                probeInfo.text("Species: " + modelState.getSpecies());
                int placementShape = SuperItemBlock.getStackPlacementShape(this.getStackFromBlock(blockState, world, data.getPos()));
                if(placementShape != 0)
                {
                    probeInfo.text(String.format("Block Size: %1$d x %2$d x %3$d", placementShape & 0xFF, (placementShape >> 8) & 0xFF, (placementShape >> 16) & 0xFF));
                }
            }
        }
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Added by forge to allow better control over fence/wall/pane connections.
     * SuperBlocks determine connectivity with each other through other means
     * and are not going to be compatible with regular fences, panes, etc.
     * Always false for that reason.
     */
    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

//    /** Used by world ray tracing.  
//     * All superblocks are normally going to be collidable, so default implementation works.
//     */
//    @Override
//    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
//    {
//        return super.canCollideCheck(state, hitIfLiquid);
//    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    /** 
     * {@inheritDoc} <br><br>
     * Mobs can't spawn on hypermatter.
     */
    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type)
    {
        return !this.material.isHyperMaterial && super.canCreatureSpawn(state, world, pos, type);
    }

    /** 
     * {@inheritDoc} <br><br>
     * Hypermatter is indestructable by normal explosions.  
     * If it does blow up somehow it shouldn't drop as a block.
     */
    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return !this.material.isHyperMaterial;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
    {
        return super.canEntityDestroy(state, world, pos, entity);
    }

    /**
     * Accessed via state implementation.
     * Used to determine if an entity can spawn on this block.
     * Has no actual or extended state properties when referenced.
     * 
     * All superblocks allow spawning unless made of hypermatter.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn)
    {
        return super.canEntitySpawn(state, entityIn) && !this.material.isHyperMaterial;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getModelState(state, world, pos, true).canPlaceTorchOnTop();
    }

    /**
     * This is queried before getActualState, which means it cannot be determined from world.
     * There are three ways to handle that don't involve ASM: 
     *   1) persist it in the block instance
     *   2) hard-code it into the block subclass
     *   3) store it in block meta
     *   
     *  The best approach depend on the type of block. 
     *  SuperModelBlocks will use meta.
     *  World-gen blocks will use one of the first two methods.
     */
    @Override
    public abstract boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer);
    //   {
    //       return ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(layer, state.getValue(SHADE_FLAGS));
    //   }

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

      /** won't be called unless getCollisionHandler is overriden */
    @SuppressWarnings("deprecation")
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        if (this.getModelState(blockState, worldIn, pos, true).getShape().meshFactory().collisionHandler() == null)
        {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        else
        {
            ArrayList<AxisAlignedBB> bounds = new ArrayList<AxisAlignedBB>();

            this.addCollisionBoxToList(blockState, worldIn, pos, 
                    new AxisAlignedBB(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord),
                    bounds, null, false);

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

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META, SHADE_FLAGS }, new IUnlistedProperty[] { MODEL_STATE });
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(META);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        //TODO: need shape-dependent logic here also
        return this.getModelState(state, world, pos, true).getRenderLayer(PaintLayer.BASE) == BlockRenderLayer.SOLID;
    }

     @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        // TODO look at profile results and consider caching modelstate in thread-local variable for getExtendedState
        return super.getActualState(state, worldIn, pos)
                .withProperty(SHADE_FLAGS, (int)this.getModelState(state, worldIn, pos, true).getRenderLayerShadedFlags());
    }

    @Override
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(this.material == BaseMaterial.VOLCANIC_LAVA) 
            return PathNodeType.LAVA;
        
        if(this.isBurning(world, pos))
            return PathNodeType.DAMAGE_FIRE;
        
        return PathNodeType.BLOCKED;
    }

    @Override
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos)
    {
        if(this.material.isTranslucent)
        {
            ModelState modelState = this.getModelState(state, world, pos, false);
            if(modelState != null)
            {
                ColorMap colorMap = modelState.getColorMap(PaintLayer.BASE);
                if(colorMap != null)
                {
                    Color lamp = Color.fromRGB(colorMap.getColor(EnumColorMap.LAMP));
                    double desaturation = lamp.HCL_C * (Translucency.STAINED.alpha - modelState.getTranslucency().alpha);
                    Color beaconLamp = Color.fromHCL(lamp.HCL_H, lamp.HCL_C - desaturation, Color.HCL_MAX, EnumHCLFailureMode.REDUCE_CHROMA);
                    int color = beaconLamp.RGB_int;
                    
                    float[] result = new float[3];
                    result[0] = ((color >> 16) & 0xFF) / 255f;
                    result[1] = ((color >> 8) & 0xFF) / 255f;
                    result[2] = (color & 0xFF) / 255f;
                    return result;
                }
            }
        }
        return null;
    }

     /** Only meaningful use is for itemRenderer which 
     * checks this to know if it should do depth checking on item renders.
     * Get no state here, so always report that we should.
     */
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * Used in many places and seems to provide min/max bounds for rendering purposes.
     * For example, seems to determine at what height rain falls.
     * In most cases is same as collision bounding box.
     */
    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        AbstractCollisionHandler handler = this.getModelState(state, worldIn, pos, true).getShape().meshFactory().collisionHandler();
        if (handler == null)
        {
            return super.getBoundingBox(state, worldIn, pos);
        }
        else
        {
            return handler.getCollisionBoundingBox(state, worldIn, pos);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        AbstractCollisionHandler handler = this.getModelState(state, worldIn, pos, true).getShape().meshFactory().collisionHandler();
        if (handler == null)
        {
            return super.getCollisionBoundingBox(state, worldIn, pos);
        }
        else
        {
            return handler.getCollisionBoundingBox(state, worldIn, pos);
        }
    }

     /** 
     * Returns an instance of the default model state for this block.
     * Because model states are mutable, every call returns a new instance.
     */
    public ModelState getDefaultModelState()
    {
        return new ModelState(this.defaultModelStateBits);
    }

    /**
     * Main reason for override is that we have to add NBT to stack for ItemBlock drops.
     * Also don't use fortune for our drops.
     */
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
     * Determines which model should be displayed via MODEL_KEY. 
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ((IExtendedBlockState)state).withProperty(MODEL_STATE, getModelState(state, world, pos, true));
    }

    /**
     * Would always return 0 anyway because we aren't in the list of encouragements that the Fire block maintains.
     */
    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return 0;
    }

    /** lowest-tier wood has a small chance of burning */
    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return this.material == BaseMaterial.FLEXWOOD ? 1 : 0;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        //Do not trust the state passed in, because nobody should be calling this method anyway.
        IBlockState goodState = worldIn.getBlockState(pos);
        return getStackFromBlock(goodState, worldIn, pos);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * DO NOT USE THIS FOR SUPERBLOCKS!
     * Use {@link #getStackFromBlock(IBlockState, IBlockAccess, BlockPos)} instead.
     * 
     * Also, yes, I overrode this method just to add this warning.
     */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return super.getItemDropped(state, rand, fortune);
    }

    // TODO: Localize
    @SuppressWarnings("deprecation")
    public String getItemStackDisplayName(ItemStack stack)
    {
        return I18n.translateToLocal(this.material.materialName + "." + this.styleName); 
    }

    /**
     * Accessed via state information.
     * Used by chunk for world lighting and to determine height map.
     * Blocks with 0 opacity are apparently ignored for height map generation.
     * 
     * 0 means fully transparent
     * values 1-15 are various degrees of opacity
     * 255 means fully opaque
     * values 16-254 have no meaning
     * 
     * Chunk uses location-dependent version if the chunk is loaded.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getLightOpacity(IBlockState state)
    {
        // super implementation returns instance variable that we set based on material in constructor
        return super.getLightOpacity(state);
    }

    /**
     * Location-dependent version of {@link #getLightOpacity(IBlockState)}
     * Gives more granular transparency information when chunk is loaded.
     */
    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        //TODO: state location
        //Using model state but is used during lighting - may want to consider using blockID/meta depending on what else we do
        // also means shaded glass does not block sky light - which would be nices
        return this.material.isTranslucent 
                ? this.getModelState(state, world, pos, false).getTranslucency().blockLightOpacity 
                : this.getLightOpacity(state);

    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLightValue(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getLightValue(state);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getLightValue(state, world, pos);
    }

    // LOCALIZATION
    @SuppressWarnings("deprecation")
    @Override
    public MapColor getMapColor(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getMapColor(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Material getMaterial(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getMaterial(state);
    }

    /** 
     * For SuperBlock (without persisted state), species is stored in meta data.  
     * For SuperModelBlock, meta indicates supported render layers
     */
    public int getMetaFromModelState(ModelState modelState)
    {
        return modelState.getSpecies();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(META);
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getMobilityFlag(state);
    }

    /** 
     * If last parameter is false, does not perform a refresh from world for world-dependent state attributes.
     * Use this option to prevent infinite recursion when need to reference some static state )
     * information in order to determine dynamic world state. Block tests are main use case for false.
     */
    public ModelState getModelState(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        ModelState result = refreshFromWorldIfNeeded ? this.getDefaultModelState().refreshFromWorld(state, world, pos) : this.getDefaultModelState();
        result.setSpecies(state.getValue(META));
        return result;
    }

    public int getOcclusionKey(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return ModModels.MODEL_DISPATCH.getOcclusionKey(this.getModelState(state, world, pos, true), side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Vec3d getOffset(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getOffset(state, worldIn, pos);
    }

    @Override
    public EnumOffsetType getOffsetType()
    {
        // TODO Auto-generated method stub
        return super.getOffsetType();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getPackedLightmapCoords(state, source, pos);
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

    @SuppressWarnings("deprecation")
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getRenderType(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getSelectedBoundingBox(state, worldIn, pos);
    }

    /**
     * Used by NiceBlockHighligher to know if custom hit box rendering is needed. Actual event handling is in that class. 
     * Won't be called unless custom collision handler is available.
     */
    public List<AxisAlignedBB> getSelectionBoundingBoxes(World worldIn, BlockPos pos, IBlockState state)
    {
        AbstractCollisionHandler handler = this.getModelState(state, worldIn, pos, true).getShape().meshFactory().collisionHandler();
        if (handler == null)
        {
            return new ImmutableList.Builder<AxisAlignedBB>().add(this.getBoundingBox(state, worldIn, pos)).build();
        }
        else
        {
            Builder<AxisAlignedBB> builder = new ImmutableList.Builder<AxisAlignedBB>();

            for (AxisAlignedBB aabb : handler.getCollisionBoxes(state, worldIn, pos, this.getModelState(state, worldIn, pos, true)))
            {
                builder.add(aabb.offset(pos.getX(), pos.getY(), pos.getZ()));
            }
            return builder.build();
        }
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getSilkTouchDrop(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SoundType getSoundType()
    {
        // TODO Auto-generated method stub
        return super.getSoundType();
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity)
    {
        // TODO Auto-generated method stub
        return super.getSoundType(state, world, pos, entity);
    }

    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getSubItems().get(this.damageDropped(state));
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer)
    {
        // TODO Auto-generated method stub
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer,
            EnumHand hand)
    {
        // TODO Auto-generated method stub
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(META, meta);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        // TODO Auto-generated method stub
        return super.getStrongPower(blockState, blockAccess, pos, side);
    }

    public String getStyleName() {
        return styleName;
    }

    //In most cases only display one item meta variant for item search
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list)
    {
        list.add(getSubItems().get(0));
    }

    public List<ItemStack> getSubItems()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < this.metaCount; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            SuperItemBlock.setModelState(stack, this.getDefaultModelState());
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }

    @Override
    public boolean getTickRandomly()
    {
        // TODO Auto-generated method stub
        return super.getTickRandomly();
    }

    @Override
    public String getUnlocalizedName()
    {
        // TODO Auto-generated method stub
        return super.getUnlocalizedName();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.getUseNeighborBrightness(state);
    }

    @Override
    public EnumFacing[] getValidRotations(World world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getValidRotations(world, pos);
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean getWeakChanges(IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.getWeakChanges(world, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        // TODO Auto-generated method stub
        return super.getWeakPower(blockState, blockAccess, pos, side);
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

    /**
     * True if block has a configurable appearance.
     */
    public boolean hasAppearanceGui()
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.hasComparatorInputOverride(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasCustomBreakingProgress(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.hasCustomBreakingProgress(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasTileEntity()
    {
        // TODO Auto-generated method stub
        return super.hasTileEntity();
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.hasTileEntity(state);
    }

    @Override
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn)
    {
        // TODO Auto-generated method stub
        return super.isAABBInsideMaterial(world, pos, boundingBox, materialIn);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isAir(state, world, pos);
    }

    @Override
    public boolean isAssociatedBlock(Block other)
    {
        // TODO Auto-generated method stub
        return super.isAssociatedBlock(other);
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon)
    {
        // TODO Auto-generated method stub
        return super.isBeaconBase(worldObj, pos, beacon);
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, Entity player)
    {
        // TODO Auto-generated method stub
        return super.isBed(state, world, pos, player);
    }

    @Override
    public boolean isBedFoot(IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isBedFoot(world, pos);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Determines result of {@link #getAmbientOcclusionLightValue(IBlockState)}
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        // TODO State location
        return super.isBlockNormalCube(state);
    }

    @Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        // TODO Auto-generated method stub
        return super.isBlockSolid(worldIn, pos, side);
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isBurning(world, pos);
    }

    @Override
    public boolean isCollidable()
    {
        // TODO Auto-generated method stub
        return super.isCollidable();
    }

    @Override
    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn,
            boolean testingHead)
    {
        // TODO Auto-generated method stub
        return super.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead);
    }

    @Override
    public boolean isFertile(World world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isFertile(world, pos);
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side)
    {
        // TODO Auto-generated method stub
        return super.isFireSource(world, pos, side);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        // TODO Auto-generated method stub
        return super.isFlammable(world, pos, face);
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
    public boolean isFoliage(IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isFoliage(world, pos);
    }

    /**
     * Accessed via state implementation.
     * Used in AI pathfinding, explosions and occlusion culling.
     * Has no actual or extended state properties when referenced.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullBlock(IBlockState state)
    {
        //TODO - state location
        return super.isFullBlock(state);
    }

    /**
     * Value stored in instance variable may be incorrect
     * because we don't have material/shape info during constructor.
     */
    @Override
    public boolean isFullCube(IBlockState state)
    {
        //TODO: state location - need to also check shape somehow
        return !this.material.isTranslucent;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullyOpaque(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.isFullyOpaque(state);
    }

    public boolean isItemUsageAdditive(World worldIn, BlockPos pos, ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
    {
        // TODO Auto-generated method stub
        return super.isLadder(state, world, pos, entity);
    }

    @Override
    public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isLeaves(state, world, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isNormalCube(IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.isNormalCube(state);
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isNormalCube(state, world, pos);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Also used in Block constructor to determine if this is a full cube.
     * So this.material may be null when it is called.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        // TODO state location
        // needs to account for shape also
        return this.material == null || !this.material.isTranslucent;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isPassable(worldIn, pos);
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isReplaceable(worldIn, pos);
    }

    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, Predicate<IBlockState> target)
    {
        // TODO Auto-generated method stub
        return super.isReplaceableOreGen(state, world, pos, target);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        // TODO Auto-generated method stub
        return super.isSideSolid(base_state, world, pos, side);
    }

    @Override
    public boolean isToolEffective(String type, IBlockState state)
    {
        // TODO Auto-generated method stub
        return super.isToolEffective(type, state);
    }

    @Override
    public boolean isTranslucent(IBlockState state)
    {
        return this.material.isTranslucent;
    }

    // BASIC METADATA MECHANICS

    @Override
    public boolean isWood(IBlockAccess world, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return super.isWood(world, pos);
    }

    @Override
    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion)
    {
        // TODO Auto-generated method stub
        return super.modifyAcceleration(worldIn, pos, entityIn, motion);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        // TODO Auto-generated method stub
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos)
    {
        // TODO Auto-generated method stub
        super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ)
    {
        // TODO Auto-generated method stub
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        // TODO Auto-generated method stub
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        // TODO Auto-generated method stub
        super.onBlockClicked(worldIn, pos, playerIn);
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn)
    {
        // TODO Auto-generated method stub
        super.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state)
    {
        // TODO Auto-generated method stub
        super.onBlockDestroyedByPlayer(worldIn, pos, state);
    }

    // INTERACTION HANDLING

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        // TODO Auto-generated method stub
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        // TODO Auto-generated method stub
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        // TODO Auto-generated method stub
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        // TODO Auto-generated method stub
        super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        // TODO Auto-generated method stub
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
    {
        // TODO Auto-generated method stub
        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    @Override
    public void onLanded(World worldIn, Entity entityIn)
    {
        // TODO Auto-generated method stub
        super.onLanded(worldIn, entityIn);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        // TODO Auto-generated method stub
        super.onNeighborChange(world, pos, neighbor);
    }

    @Override
    public void onPlantGrow(IBlockState state, World world, BlockPos pos, BlockPos source)
    {
        // TODO Auto-generated method stub
        super.onPlantGrow(state, world, pos, source);
    }

    /**
     * Need a world-aware version because may need more than metadata
     */
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        return 1;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random)
    {
        // TODO Auto-generated method stub
        return super.quantityDropped(state, fortune, random);
    }


    // RENDERING-RELATED THINGS AND STUFF
    // Note that some of the methods here are called server-side.
    // (Ray tracing and collisions, mainly.)


    @Override
    public int quantityDropped(Random random)
    {
        // TODO Auto-generated method stub
        return super.quantityDropped(random);
    }



    @Override
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        // TODO Auto-generated method stub
        return super.quantityDroppedWithBonus(fortune, random);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        // TODO Auto-generated method stub
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        // TODO Auto-generated method stub
        super.randomTick(worldIn, pos, state, random);
    }

    @Override
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox)
    {
        // TODO Auto-generated method stub
        return super.rayTrace(pos, start, end, boundingBox);
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color)
    {
        // TODO Auto-generated method stub
        return super.recolorBlock(world, pos, side, color);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        // TODO Auto-generated method stub
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public boolean requiresUpdates()
    {
        // TODO Auto-generated method stub
        return super.requiresUpdates();
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        // TODO Auto-generated method stub
        return super.rotateBlock(world, pos, axis);
    }

    public SuperBlock setAllowSilkHarvest(boolean allow)
    {
        this.allowSilkHarvest = allow;
        return this;
    }

    @Override
    public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied)
    {
        // TODO Auto-generated method stub
        super.setBedOccupied(world, pos, player, occupied);
    }

    /**
     * Sets a drop other than this block if desired.
     */
    public SuperBlock setDropItem(Item dropItem)
    {
        this.dropItem = dropItem;
        return this;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        // TODO Auto-generated method stub
        return super.shouldCheckWeakPower(state, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        if(this.material.isTranslucent)
        {
            BlockPos otherPos = pos.offset(side);
            IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
            Block block = otherBlockState.getBlock();
            if(block instanceof SuperBlock)
            {
                SuperBlock sBlock = (SuperBlock)block;
                if(((SuperBlock) block).material.isTranslucent)
                {
                    ModelState myModelState = this.getModelState(blockState, blockAccess, pos, false);
                    ModelState otherModelState = sBlock.getModelState(otherBlockState, blockAccess, otherPos, false);
                    //TODO: need to check for color/texture/occlusion match
                    return myModelState.getSpecies() != otherModelState.getSpecies()
                            || myModelState.getTranslucency() != otherModelState.getTranslucency()
                            || myModelState.getColorMap(PaintLayer.BASE) != otherModelState.getColorMap(PaintLayer.BASE);
                }
            }
        }

        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public int tickRate(World worldIn)
    {
        // TODO Auto-generated method stub
        return super.tickRate(worldIn);
    }

    @Override
    public String toString()
    {
        // TODO Auto-generated method stub
        return super.toString();
    }

    /**
     * Does not modify input stack. If the stack is modified, returns a copy.
     */
    public ItemStack updatedStackForPlacement(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        ModelState modelState = SuperItemBlock.getModelState(stack);
        int species = updatedStackForPlacementGetSpecies(modelState, worldIn, posPlaced, posOn, facing, stack, player);
        if(species == modelState.getSpecies())
        {
            return stack;
        }
        else
        {
            ItemStack result = stack.copy();
            modelState.setSpecies(species);
            SuperItemBlock.setModelState(result, modelState);
            return result;
        }
    }

    private int updatedStackForPlacementGetSpecies(ModelState modelState, World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {
        if(!modelState.hasSpecies()) return 0;

        // If player is sneaking and placing on same block, force matching species.
        // Or, if player is sneaking and places on a block that cannot mate, force non-matching species
        if(player.isSneaking())
        {
            IBlockState placedOn = worldIn.getBlockState(posOn);
            if(placedOn.getBlock() == this)
            {
                // Force match the metadata of the block on which we are placed
                return ((SuperBlock)placedOn.getBlock()).getModelState(placedOn, worldIn, posOn, true).getSpecies();
            }
            else
            {
                // Force non-match of metadata for any neighboring blocks
                int speciesInUseFlags = 0;
                SuperBlock myBlock = (SuperBlock) (((SuperItemBlock)stack.getItem()).block);

                NeighborBlocks neighbors = new NeighborBlocks(worldIn, posPlaced, false);
                NeighborTestResults results = neighbors.getNeighborTestResults(new BlockTests.SuperBlockBorderCandidateMatch(myBlock));
                
                for(EnumFacing face : EnumFacing.VALUES)            
                {
                    if (results.result(face)) 
                    {
                        speciesInUseFlags |= (1 << neighbors.getModelState(face).getSpecies());
                    }
                }

                // try to avoid corners also if picking a species that won't connect
                for(BlockCorner corner : BlockCorner.values())
                {
                    if (results.result(corner)) 
                    {
                        speciesInUseFlags |= (1 << neighbors.getModelState(corner).getSpecies());
                    }
                }

                // now randomly choose a species 
                //that will not connect to what is surrounding
                int salt = ThreadLocalRandom.current().nextInt(16);
                for(int i = 0; i < 16; i++)
                {
                    int candidate = (i + salt) % 16;
                    if((speciesInUseFlags & (1 << candidate)) == 0)
                    {
                        return candidate;
                    }
                }
                
                // give up
                return 0;
            }
        }
        // player not sneaking, so choose species based on placement shape
        else
        {
            int shape = SuperPlacement.PLACEMENT_3x3x3;

            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null && tag.hasKey(NiceTileEntity.PLACEMENT_SHAPE_TAG))
            {
                shape = tag.getInteger(NiceTileEntity.PLACEMENT_SHAPE_TAG);
            }

            SuperPlacement placer = new SuperPlacement.PlacementBigBlock(
                    new PlacementValidatorCubic(shape & 0xFF, (shape >> 8) & 0xFF, (shape >> 16) & 0xFF));

            return placer.getSpeciesForPlacedStack(worldIn, posPlaced, facing, stack, this);

        }
    }
    
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        // TODO Auto-generated method stub
        super.updateTick(worldIn, pos, state, rand);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        // TODO Auto-generated method stub
        return super.withMirror(state, mirrorIn);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        // TODO Auto-generated method stub
        return super.withRotation(state, rot);
    }
}
