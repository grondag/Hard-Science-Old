package grondag.adversity.superblock.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import grondag.adversity.Adversity;
import grondag.adversity.Configurator;
import grondag.adversity.Output;
import grondag.adversity.external.IWailaProvider;
import grondag.adversity.init.ModModels;
import grondag.adversity.library.Color;
import grondag.adversity.library.Color.EnumHCLFailureMode;
import grondag.adversity.superblock.color.ColorMap;
import grondag.adversity.superblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.items.SuperItemBlock;
import grondag.adversity.superblock.model.shape.ICollisionHandler;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.support.BlockSubstance;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import grondag.adversity.superblock.model.state.ModelStateProperty;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.Translucency;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
     * Contains state passed from getExtendedState to handleBlockState. Using a custom unlisted property because we need large int values and the vanilla implementation enumerates
     * all allowed values into a hashmap... Plus this hides the implementation from the block.
     */
    public static final ModelStateProperty MODEL_STATE = new ModelStateProperty();

     /** non-null if this drops something other than itself */
    private Item dropItem;

    /** Allow silk harvest. Defaults true. Use setAllowSilk to change */
    private boolean allowSilkHarvest = true;

    /** change in constructor to have different appearance */
    protected int[] defaultModelStateBits;

    /** will be set based on default model state first time is needed */
    protected int renderLayerEnabledFlags = -1;
    
    /** 
     * Used by dispatcher to known if shading should be enabled for the given render layer.
     * Will be set based on default model state first time is needed unless
     * changed to value other than -1 in constructor.
     */
    /** will be set based on default model state first time is needed */
    protected int renderLayerShadedFlags = -1;
    
    /** change in constructor to have fewer variants */
    protected int metaCount = 16;
    
    /** see {@link #isAssociatedBlock(Block)} */
    protected Block associatedBlock;
    
    public SuperBlock(String blockName, Material defaultMaterial, ModelState defaultModelState)
    {
        super(defaultMaterial);
        setCreativeTab(Adversity.tabAdversity);
        
        // these values are fail-safes - should never be used normally
        this.setHarvestLevel("pickaxe", 1);
        setSoundType(SoundType.STONE);
        setHardness(2);
        setResistance(50);
        
        this.setRegistryName(blockName);
        this.setUnlocalizedName(blockName);
        this.associatedBlock = this;
        
        this.lightOpacity = 0;

        this.defaultModelStateBits = defaultModelState.getBitsIntArray();
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
            Entity entityIn, boolean p_185477_7_)
    {
        ModelState modelState = this.getModelState(worldIn, pos, true);
        ICollisionHandler collisionHandler = modelState.getShape().meshFactory().collisionHandler();

        if (collisionHandler == null)
        {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
        else
        {
            AxisAlignedBB localMask = entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ());

            List<AxisAlignedBB> bounds = collisionHandler.getCollisionBoxes(modelState);

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
        
        ModelState modelState = this.getModelStateAssumeStateIsCurrent(world.getBlockState(pos), world, pos, false);

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
        ModelState modelState = this.getModelState(world, pos, false);
        
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
        
        if(modelState != null)
        {
            tooltip.add(I18n.translateToLocal("label.shape") + ": " + modelState.getShape().localizedName());
            tooltip.add(I18n.translateToLocal("label.base_color") + ": " + modelState.getColorMap(PaintLayer.BASE).localizedName());
            tooltip.add(I18n.translateToLocal("label.base_texture") + ": " + modelState.getTexture(PaintLayer.BASE).localizedName());
            if(modelState.isOverlayLayerEnabled())
            {
                tooltip.add(I18n.translateToLocal("label.overlay_color") + ": " + modelState.getColorMap(PaintLayer.OVERLAY).localizedName());
                tooltip.add(I18n.translateToLocal("label.overlay_texture") + ": " + modelState.getTexture(PaintLayer.OVERLAY).localizedName());
            }
            if(modelState.isDetailLayerEnabled())
            {
                tooltip.add(I18n.translateToLocal("label.detail_color") + ": " + modelState.getColorMap(PaintLayer.DETAIL).localizedName());
                tooltip.add(I18n.translateToLocal("label.detail_texture") + ": " + modelState.getTexture(PaintLayer.DETAIL).localizedName());
            }
            if(modelState.hasSpecies()) 
            {
                tooltip.add(I18n.translateToLocal("label.species") + ": " + modelState.getSpecies());
            }
        }
        tooltip.add(I18n.translateToLocal("label.material") + ": " + SuperItemBlock.getStackSubstance(stack).localizedName());
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
        ModelState modelState = this.getModelStateAssumeStateIsStale(blockState, world, data.getPos(), true);
        
        if(modelState != null)
        {
            probeInfo.text(I18n.translateToLocal("label.shape") + ": " + modelState.getShape().localizedName());
            probeInfo.text(I18n.translateToLocal("label.base_color") + ": " + modelState.getColorMap(PaintLayer.BASE).localizedName());
            probeInfo.text(I18n.translateToLocal("label.base_texture") + ": " + modelState.getTexture(PaintLayer.BASE).localizedName());
            if(modelState.isOverlayLayerEnabled())
            {
                probeInfo.text(I18n.translateToLocal("label.overlay_color") + ": " + modelState.getColorMap(PaintLayer.OVERLAY).localizedName());
                probeInfo.text(I18n.translateToLocal("label.overlay_texture") + ": " + modelState.getTexture(PaintLayer.OVERLAY).localizedName());
            }
            if(modelState.isDetailLayerEnabled())
            {
                probeInfo.text(I18n.translateToLocal("label.detail_color") + ": " + modelState.getColorMap(PaintLayer.DETAIL).localizedName());
                probeInfo.text(I18n.translateToLocal("label.detail_texture") + ": " + modelState.getTexture(PaintLayer.DETAIL).localizedName());
            }
            if(modelState.hasSpecies()) 
            {
                probeInfo.text(I18n.translateToLocal("label.species") + ": " + modelState.getSpecies());
            }
        }
        probeInfo.text(I18n.translateToLocal("label.material") + ": " + this.getSubstance(blockState, world, data.getPos()).localizedName());
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
        return !this.isHypermatter() && super.canCreatureSpawn(state, world, pos, type);
    }

    /** 
     * {@inheritDoc} <br><br>
     * Hypermatter is indestructable by normal explosions.  
     * If it does blow up somehow it shouldn't drop as a block.
     */
    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return !this.isHypermatter();
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
        return super.canEntitySpawn(state, entityIn) && Configurator.HYPERSTONE.allowMobSpawning || !this.isHypermatter() ;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.getModelStateAssumeStateIsStale(state, world, pos, true).sideShape(EnumFacing.UP).holdsTorch;
    }

    /**
     * This is queried before getActualState, which means it cannot be determined from world.
     * 
     * We only have a couple ways to get around this that don't involve ASM or switching to a TESR.
     * 
     * 1) We can persist it in the block instance somehow and set block states that have the
     * right value for the model they represent.  This could force some block state changes in 
     * the world however if model state changes - but those changes are not likely.
     * Main drawback of this approach is that it consumes a lot of block ids.
     * 
     * 2) We can report that we render in all layers but return no quads.  However, this means RenderChunk
     * does quite a bit of work asking us for stuff that isn't there. 
     * 
     * Default implementation derives it from default model state first time it is needed.
     */
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        int layerFlags = this.renderLayerEnabledFlags;
        if(layerFlags == -1)
        {
            layerFlags = this.getDefaultModelState().getCanRenderInLayerFlags();
            this.renderLayerEnabledFlags = layerFlags;
        }
        return ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(layer, layerFlags);
    }
    
    /** used by dispatcher to control AO shading by render layer */
    public int renderLayerShadedFlags()
    {
        int layerFlags = this.renderLayerShadedFlags;
        if(layerFlags == -1)
        {
            layerFlags = this.getDefaultModelState().getRenderLayerShadedFlags();
            this.renderLayerShadedFlags = layerFlags;
        }
        return layerFlags;
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
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        if (this.getModelStateAssumeStateIsStale(blockState, worldIn, pos, true).getShape().meshFactory().collisionHandler() == null)
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
        return new ExtendedBlockState(this, new IProperty[] { META }, new IUnlistedProperty[] { MODEL_STATE });
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(META);
    }
   
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        ModelState modelState = this.getModelStateAssumeStateIsCurrent(state, world, pos, true);
        return modelState.getRenderLayer(PaintLayer.BASE) == BlockRenderLayer.SOLID
                && modelState.sideShape(face).occludesOpposite;
    }
    
    @Override
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(this.getSubstance(state, world, pos) == BlockSubstance.VOLCANIC_LAVA) 
            return PathNodeType.LAVA;
        
        if(this.isBurning(world, pos))
            return PathNodeType.DAMAGE_FIRE;
        
        return PathNodeType.BLOCKED;
    }

    @Override
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos)
    {
        if(this.getSubstance(state, world, pos).isTranslucent)
        {
            ModelState modelState = this.getModelStateAssumeStateIsStale(state, world, pos, false);
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
        ModelState modelState = this.getModelStateAssumeStateIsStale(state, worldIn, pos, true);
        ICollisionHandler handler = modelState.getShape().meshFactory().collisionHandler();
        if (handler == null)
        {
            return super.getBoundingBox(state, worldIn, pos);
        }
        else
        {
            return handler.getCollisionBoundingBox(modelState);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        ModelState modelState = this.getModelStateAssumeStateIsStale(state, worldIn, pos, true);
        ICollisionHandler handler = modelState.getShape().meshFactory().collisionHandler();
        if (handler == null)
        {
            return super.getCollisionBoundingBox(state, worldIn, pos);
        }
        else
        {
            return handler.getCollisionBoundingBox(modelState);
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
     * Determines which model should be displayed via MODEL_STATE. 
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return ((IExtendedBlockState)state)
                .withProperty(MODEL_STATE, getModelStateAssumeStateIsCurrent(state, world, pos, true));
//                .withProperty(SHADE_FLAGS, (int)this.getModelState(state, world, pos, false).getRenderLayerShadedFlags());;
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
        return this.getSubstance(world, pos) == BlockSubstance.FLEXWOOD ? 1 : 0;
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

    public String getItemStackDisplayName(ItemStack stack)
    {
        return this.getLocalizedName(); 
    }
    
    
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
    @Override
    public int getLightOpacity(IBlockState state)
    {
        return  this.worldLightOpacity(state).opacity;
    }

    /**
     * Location-dependent version of {@link #getLightOpacity(IBlockState)}
     * Gives more granular transparency information when chunk is loaded.
     * 
     * Any value over 0 prevents a block from seeing the sky.
     */
    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if(this.getSubstance(state, world, pos).isTranslucent)
        {
            return this.getModelStateAssumeStateIsCurrent(state, world, pos, false).getTranslucency().blockLightOpacity;
        }
        else
        {
            return this.getModelStateAssumeStateIsCurrent(state, world, pos, false).geometricSkyOcclusion();
        }
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
     * 
     * 
     */
    public ModelState getModelState(IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        return getModelStateAssumeStateIsCurrent(world.getBlockState(pos), world, pos, refreshFromWorldIfNeeded);
    }
    
    /**
     * At least one vanilla routine passes in a block state that does not match world.
     * (After block updates, passes in previous state to detect collision box changes.)
     * 
     * We don't want to update our current state based on stale block state, so the TE
     * refresh is coded to always use current world state.
     * 
     * However, we do want to honor the given world state if species is different than current.
     * We do this by directly changing species, because that is only thing that can changed
     * in model state based on block state, and also affects collision box.
     * 
     * TODO: there is probably still a bug here, because collision box can change based
     * on other components of model state (axis, for example) and those changes may not be detected
     * by path finding.
     */
    public ModelState getModelStateAssumeStateIsStale(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        // for mundane (non-TE) blocks don't need to worry about state being persisted, logic is same for old and current states
        return refreshFromWorldIfNeeded ? this.getDefaultModelState().refreshFromWorld(state, world, pos) : this.getDefaultModelState();
    }
    
    /** 
     * Use when absolutely certain given block state is current.
     */
    public ModelState getModelStateAssumeStateIsCurrent(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        return getModelStateAssumeStateIsStale(state, world, pos, refreshFromWorldIfNeeded);
    }
 
    public int getOcclusionKey(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return ModModels.MODEL_DISPATCH.getOcclusionKey(this.getModelStateAssumeStateIsCurrent(state, world, pos, true), side);
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
        ModelState modelState = this.getModelStateAssumeStateIsStale(state, worldIn, pos, true);
        ICollisionHandler handler = modelState.getShape().meshFactory().collisionHandler();
        if (handler == null)
        {
            return ImmutableList.of(this.getBoundingBox(state, worldIn, pos));
        }
        else
        {
            Builder<AxisAlignedBB> builder = new ImmutableList.Builder<AxisAlignedBB>();

            for (AxisAlignedBB aabb : handler.getCollisionBoxes(modelState))
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

    /**
     * {@inheritDoc} <br><br>
     * 
     * Confusingly named because is really the back end for Item.getSubItems.
     * Used by Creative and JEI to show a list of blocks.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list)
    {
        list.addAll(getSubItems());
    }

    public List<ItemStack> getSubItems()
    {
        return this.getSubItemsBasic();
    }
    
    protected List<ItemStack> getSubItemsBasic()
    {
        ImmutableList.Builder<ItemStack> itemBuilder = new ImmutableList.Builder<ItemStack>();
        for(int i = 0; i < this.metaCount; i++)
        {
            ItemStack stack = new ItemStack(this, 1, i);
            ModelState modelState = this.getDefaultModelState();
            if(modelState.hasSpecies())
            {
                modelState.setSpecies(i);
            }
            SuperItemBlock.setModelState(stack, modelState);
            itemBuilder.add(stack);
        }
        return itemBuilder.build();
    }
    
    /**
     * Controls material-dependent properties
     */
    public abstract BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos);

    public BlockSubstance getSubstance(IBlockAccess world, BlockPos pos)
    {
        return this.getSubstance(world.getBlockState(pos), world, pos);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Should be true if shape is not a full cube or fully transparent.
     */
    @Override
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return this.worldLightOpacity(state) == WorldLightOpacity.TRANSPARENT
                || !this.isGeometryFullCube(state);
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
     * This is an egregious hack to avoid performance hit of instanceof.
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
        return this.isHypermatter() || super.isBeaconBase(worldObj, pos, beacon);
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Determines result of {@link #getAmbientOcclusionLightValue(IBlockState)}
     */
    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        return this.isGeometryFullCube(state) && this.worldLightOpacity(state) == WorldLightOpacity.SOLID;
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Has nothing to do with block geometry but instead is based on material.
     * Is used by fluid rendering to know if fluid should appear to flow into this block.
     */
    @Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return this.getSubstance(worldIn, pos).material.isSolid();
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return this.getSubstance(world, pos) == BlockSubstance.VOLCANIC_LAVA;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return this.getSubstance(world, pos) == BlockSubstance.FLEXWOOD;
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
     * {@inheritDoc}
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
        return this.isGeometryFullCube(state);
    }

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
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return this.isGeometryFullCube(state);
    }

    /**
     * With {@link #isSubstanceTranslucent(IBlockState)} makes all the block
     * test methods work when full location information not available.
     * 
     * Only addresses geometry - does this block fully occupy a 1x1x1 cube?
     * True if so. False otherwise.
     */
    public abstract boolean isGeometryFullCube(IBlockState state);
    
    public abstract boolean isHypermatter();
    
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
        return this.getModelStateAssumeStateIsStale(state, world, pos, true).isCube() && this.getSubstance(state, world, pos).material.isSolid();
    }

    /**
     * {@inheritDoc} <br><br>
     * 
     * Value given for the default state is also used 
     * in Block constructor to determine value of fullBlock 
     * which in turn is used to determine initial value of lightOpacity.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return this.isGeometryFullCube(state) && this.worldLightOpacity(state) == WorldLightOpacity.SOLID;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return this.getSubstance(worldIn, pos).material.isReplaceable();
    }
    
    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return this.getModelStateAssumeStateIsStale(base_state, world, pos, true).sideShape(side).holdsTorch;
    }
 
    @Override
    public boolean isTranslucent(IBlockState state)
    {
        return this.worldLightOpacity(state) != WorldLightOpacity.SOLID;
    }

    
    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        BlockSubstance substance = this.getSubstance(worldIn, pos);
        if (!entityIn.isSneaking() && substance.walkSpeedFactor != 0.0)
        {
            entityIn.motionX *= substance.walkSpeedFactor;
            entityIn.motionZ *= substance.walkSpeedFactor;
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
        if(Output.DEBUG_MODE) Output.warn("Unsupported call to SuperBlock.quantityDropped(IBlockState state, int fortune, Random random)");
        return 0;
    }

    /** should never be used */
    @Override
    public int quantityDropped(Random random)
    {
        if(Output.DEBUG_MODE) Output.warn("Unsupported call to SuperBlock.quantityDropped(Random random)");
        return 0;
    }

    /** should never be used */
    @Override
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        if(Output.DEBUG_MODE) Output.warn("Unsupported call to SuperBlock.quantityDroppedWithBonus");
        return 0;
    }

    /** should never be used - all handled in {@link #collisionRayTrace(IBlockState, World, BlockPos, Vec3d, Vec3d)} */
    @Override
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox)
    {
        if(Output.DEBUG_MODE) Output.warn("Unsupported call to SuperBlock.rayTrace on block with custom collision handler");
        return super.rayTrace(pos, start, end, boundingBox);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        IBlockState blockState = world.getBlockState(pos);
        return this.getModelStateAssumeStateIsCurrent(blockState, world, pos, true).rotateBlock(blockState, world, pos, axis, this);
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
        if(this.getSubstance(blockState, blockAccess, pos).isTranslucent)
        {
            BlockPos otherPos = pos.offset(side);
            IBlockState otherBlockState = blockAccess.getBlockState(otherPos);
            Block block = otherBlockState.getBlock();
            if(block instanceof SuperBlock)
            {
                SuperBlock sBlock = (SuperBlock)block;
                if(((SuperBlock) block).getSubstance(otherBlockState, blockAccess, otherPos).isTranslucent)
                {
                    ModelState myModelState = this.getModelStateAssumeStateIsCurrent(blockState, blockAccess, pos, false);
                    ModelState otherModelState = sBlock.getModelStateAssumeStateIsCurrent(otherBlockState, blockAccess, otherPos, false);
                    //TODO: need to check for texture/occlusion match
                    return myModelState.getSpecies() != otherModelState.getSpecies()
                            || myModelState.getRenderLayer(PaintLayer.BASE) != otherModelState.getRenderLayer(PaintLayer.BASE)
                            || myModelState.getTranslucency() != otherModelState.getTranslucency()
                            || myModelState.getColorMap(PaintLayer.BASE) != otherModelState.getColorMap(PaintLayer.BASE);
                }
            }
        }

        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
   
    /**
     * Used in conjunction with {@link #isGeometryFullCube(IBlockState)} to
     * make all the other full/normal/opaque/translucent methods work
     * when they don't have full location information.
     * NB: default vanilla implementation is simply this.translucent
     * 
     * 
     * Should return true if the substance is not fully opaque.
     * Has nothing to do with block geometry.
     */
    protected abstract WorldLightOpacity worldLightOpacity(IBlockState state);

}
