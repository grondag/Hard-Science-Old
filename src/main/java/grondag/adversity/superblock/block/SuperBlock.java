package grondag.adversity.superblock.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import grondag.adversity.Adversity;
import grondag.adversity.external.IWailaProvider;
import grondag.adversity.init.ModModels;
import grondag.adversity.library.NeighborBlocks;
import grondag.adversity.library.PlacementValidatorCubic;
import grondag.adversity.library.NeighborBlocks.BlockCorner;
import grondag.adversity.library.NeighborBlocks.NeighborTestResults;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.color.ColorMap;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
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

    /**
     * Use in UI
     */
    private final String displayName;

    /** non-null if this drops something other than itself */
    private Item dropItem;

    /** Allow silk harvest. Defaults true. Use setAllowSilk to change */
    private boolean allowSilkHarvest = true;

    // set in constructor to have different appearance
    protected int[] defaultModelStateBits;

    // change in constructor to have fewer variants
    protected int metaCount = 16;

    @SuppressWarnings("deprecation")
    public SuperBlock(BaseMaterial material, String styleName)
    {
        super(material.material);
        this.material = material;
        this.styleName = styleName;
        setCreativeTab(Adversity.tabAdversity);
        this.setHarvestLevel(material.harvestTool, material.harvestLevel);
        setSoundType(material.stepSound);
        setHardness(material.hardness);
        setResistance(material.resistance);
        this.setRegistryName(material.materialName + "." + styleName);
        this.setUnlocalizedName(this.getRegistryName().toString());

        ModelState defaultState = new ModelState();
        defaultState.setShape(ModelShape.CUBE);
        this.setDefaultModelState(defaultState);


        String makeName = I18n.translateToLocal(getStyleName());
        if(makeName == null || makeName.equals("")) makeName = getStyleName();

        displayName = makeName + " " + I18n.translateToLocal(material.materialName); 
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META, SHADE_FLAGS }, new IUnlistedProperty[] { MODEL_STATE });
    }

    // BASIC METADATA MECHANICS

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
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(META, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(META);
    }

    /** 
     * For SuperBlock (without persisted state), species is stored in meta data.  
     * For SuperModelBlock, meta indicates supported render layers
     */
    public int getMetaFromModelState(ModelState modelState)
    {
        return modelState.getSpecies();
    }

    /**
     * Does not modify input stack. If the stack is modified, returns a copy.
     */
    public ItemStack updatedStackForPlacement(World worldIn, BlockPos posPlaced, BlockPos posOn, EnumFacing facing, ItemStack stack, EntityPlayer player)
    {

        int species = 0;

        // If player is sneaking and placing on same block, force matching species.
        // Or, if player is sneaking and places on a block that cannot mate, force non-matching species
        if(player.isSneaking())
        {
            IBlockState placedOn = worldIn.getBlockState(posOn);
            if(placedOn.getBlock() == this)
            {
                // Force match the metadata of the block on which we are placed
                species = ((SuperBlock)placedOn.getBlock()).getModelState(placedOn, worldIn, posOn, true).getSpecies();
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
                        species = candidate;
                        break;
                    }
                }
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

            species = placer.getSpeciesForPlacedStack(worldIn, posPlaced, facing, stack, this);

        }
        
        ModelState modelState = SuperItemBlock.getModelState(stack);
        
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
    public SuperBlock setDropItem(Item dropItem)
    {
        this.dropItem = dropItem;
        return this;
    }

    public SuperBlock setAllowSilkHarvest(boolean allow)
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

    public final void setDefaultModelState(ModelState modelState)
    {
        this.defaultModelStateBits = modelState.getBitsIntArray();
    }

    /** 
     * Returns an instance of the default model state for this block.
     * Because model states are mutable, every call returns a new instance.
     */
    public ModelState getDefaultModelState()
    {
        return new ModelState(this.defaultModelStateBits);
    }



    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        // TODO look at profile results and consider caching modelstate in thread-local variable for getExtendedState
        return super.getActualState(state, worldIn, pos)
                .withProperty(SHADE_FLAGS, (int)this.getModelState(state, worldIn, pos, true).getRenderLayerShadedFlags());
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
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
            int numberOfParticles)
    {
        // TODO have to override this to get correct particle color/texture
        return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
    }

    @Override
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager)
    {
        // TODO have to override this to get correct particle color/texture
        return super.addHitEffects(state, worldObj, target, manager);
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
    {
        // TODO have to override this to get correct particle color/texture
        return super.addDestroyEffects(world, pos, manager);
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

    /**
     * Used by NiceBlockHighligher to know if custom hit box rendering is needed. Actual event handling is in that class. 
     * Won't be called unless custom collision handler is available.
     */
    @SuppressWarnings("deprecation")
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
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return Collections.emptyList();
    }
    
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    {
        ModelState modelState = this.getModelState(blockState, world, data.getPos(), true);
        
        if(modelState != null)
        {
            probeInfo.text("Species = " + modelState.getSpecies())
                .text("Shape = " + modelState.getShape());
            
            int placementShape = SuperItemBlock.getStackPlacementShape(this.getStackFromBlock(blockState, world, data.getPos()));
            if(placementShape != 0)
            {
                probeInfo.text(String.format("Block Size: %1$d x %2$d x %3$d", placementShape & 0xFF, (placementShape >> 8) & 0xFF, (placementShape >> 16) & 0xFF));
            }
        }
    }
}
