package grondag.adversity.superblock.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import grondag.adversity.Adversity;
import grondag.adversity.Output;
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
import grondag.adversity.niceblock.support.BlockSubstance;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
 * Base class for Adversity building blocks.
 * 
 * 
 * TODO LIST
 * 
 * instance-level state info
 *  2 opacity  
 *  1 is full block
 *  4 render shaded flags
 *  4 render layer flags
 *  4 materials
 *  15 bits total = too damn many bits
 */
@SuppressWarnings("deprecation")
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
    public final BlockSubstance substance;
    
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
    
    /** see {@link #isAssociatedBlock(Block)} */
    protected Block associatedBlock;
    
    
    public SuperBlock(String styleName, BlockSubstance material)
    {
        super(material.material);
        this.substance = material;
        this.styleName = styleName;
        setCreativeTab(Adversity.tabAdversity);
        this.setHarvestLevel(material.harvestTool, material.harvestLevel);
        setSoundType(material.stepSound);
        setHardness(material.hardness);
        setResistance(material.resistance);
        this.setRegistryName(styleName + "_" + material.materialName);
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.associatedBlock = this;
        
        // see getLightOpacity(IBlockState)
        this.lightOpacity = material.isTranslucent ? 0 : 255;

        ModelState defaultState = new ModelState();
        defaultState.setShape(ModelShape.CUBE);
        BlockRenderLayer baseRenderLayer = this.substance.isTranslucent
                ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
        defaultState.setRenderLayer(PaintLayer.BASE, baseRenderLayer);
        defaultState.setRenderLayer(PaintLayer.CUT, baseRenderLayer);
        defaultState.setRenderLayer(PaintLayer.LAMP, baseRenderLayer);
        this.defaultModelStateBits = defaultState.getBitsIntArray();
    }

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
        IBlockState blockState = world.getBlockState(pos);
        if(blockState.getBlock() != this)
        {
            // somehow got called for a different block
            if(blockState.getBlock() instanceof SuperBlock)
            {
                // if the block at given position is somehow also a SuperBlock, call particle handler for it
                ((SuperBlock)(blockState.getBlock())).addDestroyEffects(world, pos, manager);
            }
            else
            {
                // handle as a vanilla block
                return false;
            }
        }
        
        ModelState modelState = this.getModelState(world.getBlockState(pos), world, pos, false);

        for (int j = 0; j < 4; ++j)
        {
            for (int k = 0; k < 4; ++k)
            {
                for (int l = 0; l < 4; ++l)
                {
                    double d0 = ((double)j + 0.5D) / 4.0D;
                    double d1 = ((double)k + 0.5D) / 4.0D;
                    double d2 = ((double)l + 0.5D) / 4.0D;
                    manager.addEffect((new ParticleDiggingSuperBlock(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D,
                            blockState, modelState)).setBlockPos(pos));
                }
            }
        }

        return true;
    }

    @Override
    public boolean addHitEffects(IBlockState blockState, World world, RayTraceResult target, ParticleManager manager)
    {
        if(blockState.getBlock() != this)
        {
            // somehow got called for a different block
            if(blockState.getBlock() instanceof SuperBlock)
            {
                // if the block at given position is somehow also a SuperBlock, call particle handler for it
                ((SuperBlock)(blockState.getBlock())).addHitEffects(blockState, world, target, manager);
            }
            else
            {
                // handle as a vanilla block
                return false;
            }
        }
        
        BlockPos pos = target.getBlockPos();
        ModelState modelState = this.getModelState(world.getBlockState(pos), world, pos, false);
        
        EnumFacing side = target.sideHit;

        if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE)
        {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
 
            AxisAlignedBB axisalignedbb = blockState.getBoundingBox(world, pos);
            double d0 = (double)i + ThreadLocalRandom.current().nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double)j + ThreadLocalRandom.current().nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double)k + ThreadLocalRandom.current().nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

            if (side == EnumFacing.DOWN)
            {
                d1 = (double)j + axisalignedbb.minY - 0.10000000149011612D;
            }

            if (side == EnumFacing.UP)
            {
                d1 = (double)j + axisalignedbb.maxY + 0.10000000149011612D;
            }

            if (side == EnumFacing.NORTH)
            {
                d2 = (double)k + axisalignedbb.minZ - 0.10000000149011612D;
            }

            if (side == EnumFacing.SOUTH)
            {
                d2 = (double)k + axisalignedbb.maxZ + 0.10000000149011612D;
            }

            if (side == EnumFacing.WEST)
            {
                d0 = (double)i + axisalignedbb.minX - 0.10000000149011612D;
            }

            if (side == EnumFacing.EAST)
            {
                d0 = (double)i + axisalignedbb.maxX + 0.10000000149011612D;
            }

            manager.addEffect((new ParticleDiggingSuperBlock(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockState, modelState)).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }

        return true;
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
        // This is server-side, so to get matching particle color/texture I'd have to create a custom particle type similar to ParticleBlockDust,
        // register it and add a handler for it.  Light-colored quartz particles are fine IMO - is just kicking up dust, not breaking anything.
        
        worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] {Block.getStateId(Blocks.QUARTZ_BLOCK.getDefaultState())});
        return true;
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
        return !this.substance.isHyperMaterial && super.canCreatureSpawn(state, world, pos, type);
    }

    /** 
     * {@inheritDoc} <br><br>
     * Hypermatter is indestructable by normal explosions.  
     * If it does blow up somehow it shouldn't drop as a block.
     */
    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return !this.substance.isHyperMaterial;
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
    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn)
    {
        return super.canEntitySpawn(state, entityIn) && !this.substance.isHyperMaterial;
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

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        if (this.getModelState(blockState, worldIn, pos, true).getShape().meshFactory().collisionHandler() == null)
        {
            // same as vanilla logic here, but avoiding call to rayTrace so can detected unsupported calls to it
            Vec3d vec3d = start.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            Vec3d vec3d1 = end.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            RayTraceResult raytraceresult = this.getBoundingBox(blockState, worldIn, pos).calculateIntercept(vec3d, vec3d1);
            return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), raytraceresult.sideHit, pos);
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
        ModelState modelState = this.getModelState(state, world, pos, true);
        return modelState.getRenderLayer(PaintLayer.BASE) == BlockRenderLayer.SOLID
                && modelState.isSideSolid(face);
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        // Would save a call to getModelState in getExtendedState if we could simply
        // add model state to the state instance returned here.
        
        // However this won't work because model lookup is done via an identity hash map
        // and the state instance would not match the pre-defined, immutable state instances
        // added by our block state mapper at load.
        
        // Experimented with thread-local cache - but overhead of that approach made it unworthwhile.
        // Another approach would be to have multiple block instances with different shaded flags
        // so that it isn't necessary to look up modelstate here.
        
        // TODO: state location for rendershadedflags
        
        
        
        
        // BUG:
        
        
        
        return super.getActualState(state, worldIn, pos)
                .withProperty(SHADE_FLAGS, (int)this.getModelState(state, worldIn, pos, true).getRenderLayerShadedFlags());
    }

    @Override
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(this.substance == BlockSubstance.VOLCANIC_LAVA) 
            return PathNodeType.LAVA;
        
        if(this.isBurning(world, pos))
            return PathNodeType.DAMAGE_FIRE;
        
        return PathNodeType.BLOCKED;
    }

    @Override
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos)
    {
        if(this.substance.isTranslucent)
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
        return this.substance == BlockSubstance.FLEXWOOD ? 1 : 0;
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
    public String getItemStackDisplayName(ItemStack stack)
    {
        return I18n.translateToLocal(this.substance.materialName + "." + this.styleName); 
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
        return this.substance.isTranslucent 
                ? this.getModelState(state, world, pos, false).getTranslucency().blockLightOpacity 
                : this.getLightOpacity(state);

    }

     /** 
     * For SuperBlock (without persisted state), meta stores species data.  
     * For SuperModelBlock, meta indicates supported render layers.
     */
    public int getMetaFromModelState(ModelState modelState)
    {
        return modelState.getSpecies();
    }

    /**
     * Usage of meta is overloaded and dependent on other aspects of state, so just storing the raw value.
     */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(META);
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

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        //Do not trust the state passed in, because WAILA passes in a default state.
        //Doing so causes us to pass in bad meta value which determines a bad model key 
        //which is then cached, leading to strange render problems for blocks just placed up updated.
        IBlockState goodState = world.getBlockState(pos);

        return getStackFromBlock(goodState, world, pos);
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

    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getSubItems().get(this.damageDropped(state));
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(META, meta);
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
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        // TODO state location - should be true if shape is not a full cube or fully transparent
        return super.getUseNeighborBrightness(state);
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return Collections.emptyList();
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

    /** 
     * This is an egregious hack to avoids performance hit of instanceof.
     * (Based on performance profile results.) <br> <br>
     * 
     * Default value of {@link #associatedBlock} is set to this instance in constructor.
     * If not changed, will have same behavior as vanilla.
     * Change it to a reference value to have this block be recognized as part of a group.
     * Initially used for flow blocks so that they can be detected quickly.
     */
    @Override
    public boolean isAssociatedBlock(Block other)
    {
        return other == this.associatedBlock;
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * In a rare and ironic nod to vanilla magical thinking, Hypermatter acts as a beacon base.
     * I will almost certainly regret this.
     */
    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon)
    {
        return this.substance.isHyperMaterial || super.isBeaconBase(worldObj, pos, beacon);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Determines result of {@link #getAmbientOcclusionLightValue(IBlockState)}
     */
    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        // TODO State location
        return super.isBlockNormalCube(state);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Same behavior as vanilla but faster implementation.
     * Has nothing to do with block geometry but instead is based on material.
     * Is used by fluid rendering to know if fluid should appear to flow into this block.
     */
    @Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return this.substance.material.isSolid();
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return this.substance == BlockSubstance.VOLCANIC_LAVA;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return this.substance == BlockSubstance.FLEXWOOD;
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

    /**
     * {@inheritDoc} <br><br>
     * 
     * Accessed via state implementation.
     * Used in AI pathfinding, explosions and occlusion culling.
     * Has no actual or extended state properties when referenced. <br><br>
     * 
     * Vanilla implementation uses fullBlock instance variable,
     * which is derived at construction from {@link #isOpaqueCube(IBlockState)}
     */
    @Override
    public boolean isFullBlock(IBlockState state)
    {
        //TODO state location
        // could handle by setting value of fullBlock in constructor 
        return super.isFullBlock(state);
    }

    /**
     * Used many places in rendering and AI. 
     * Input state provided has no extended properties.
     * Result appears to be based on geometry - if block is a 
     * full 1.0 cube return true, false otherwise.
     * Also returns false if material is translucent. <br><br>
     * 
     * Is also used in derivation of {@link #isFullyOpaque(IBlockState)}
     * and {@link #isNormalCube(IBlockState)}
     */
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return !this.substance.isTranslucent && this.isFullBlock(state);
    }

    /** To be overridden by stackable plates or blocks with similar behavior. */
    public boolean isItemUsageAdditive(World worldIn, BlockPos pos, ItemStack stack)
    {
        return false;
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Vanilla version will return false if material is not fully opaque.
     * Ours is based solely on geometry && solidity.
     * 
     */
    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getModelState(state, world, pos, true).isCube() && this.substance.material.isSolid();
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
        return this.substance == null || !this.substance.isTranslucent;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return this.getModelState(base_state, world, pos, true).isSideSolid(side);
    }

    @Override
    public boolean isTranslucent(IBlockState state)
    {
        return this.substance.isTranslucent;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        if (!entityIn.isSneaking() && this.substance.walkSpeedFactor != 0.0)
        {
            entityIn.motionX *= this.substance.walkSpeedFactor;
            entityIn.motionZ *= this.substance.walkSpeedFactor;
        }
    }

    /**
     * World-aware version called from getDrops because logic may need more than metadata.
     * Other versions (not overriden) should not be called.
     */
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        return 1;
    }

    /** should never be used */
    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported call to SuperBlock.quantityDropped(IBlockState state, int fortune, Random random)");
        return 0;
    }

    /** should never be used */
    @Override
    public int quantityDropped(Random random)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported call to SuperBlock.quantityDropped(Random random)");
        return 0;
    }

    /** should never be used */
    @Override
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported call to SuperBlock.quantityDroppedWithBonus");
        return 0;
    }

    /** should never be used - all handled in {@link #collisionRayTrace(IBlockState, World, BlockPos, Vec3d, Vec3d)} */
    @Override
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox)
    {
        if(Output.DEBUG_MODE) Output.getLog().warn("Unsupported call to SuperBlock.rayTrace on block with custom collision handler");
        return super.rayTrace(pos, start, end, boundingBox);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        IBlockState blockState = world.getBlockState(pos);
        return this.getModelState(world.getBlockState(pos), world, pos, true).rotateBlock(blockState, world, pos, axis, this);
    }

    public SuperBlock setAllowSilkHarvest(boolean allow)
    {
        this.allowSilkHarvest = allow;
        return this;
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
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        if(this.substance.isTranslucent)
        {
            BlockPos otherPos = pos.offset(side);
            IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
            Block block = otherBlockState.getBlock();
            if(block instanceof SuperBlock)
            {
                SuperBlock sBlock = (SuperBlock)block;
                if(((SuperBlock) block).substance.isTranslucent)
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
}
