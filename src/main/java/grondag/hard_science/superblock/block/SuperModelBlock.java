package grondag.hard_science.superblock.block;

import javax.annotation.Nullable;

import grondag.hard_science.superblock.model.state.WorldLightOpacity;
import grondag.hard_science.superblock.model.state.BlockRenderMode;
import grondag.hard_science.superblock.model.state.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * User-configurable HardScience building blocks.<br><br>
 * 
 * While most attributes are stored in stack/tile entity NBT
 * some important methods are called without reference to a location or stack.
 * For these, we have multiple instances of this block and use a different instance
 * depending on the combination of attributes needed.<br><br>
 * 
 * The choice of which block to deploy is made by the item/creative stack that places the block
 * by calling {@link grondag.hard_science.init.ModSuperModelBlocks#findAppropriateSuperModelBlock(BlockSubstance substance, ModelState modelState)} <br><br>
 * 
 * The specific dimensions by which the block instances vary are:  {@link #getRenderModeSet()}, {@link #worldLightOpacity}, Block.fullBlock and {@link #isHypermatter()}.
 * 
 *
 */
public class SuperModelBlock extends SuperBlockPlus  
{
    /**
     * Ordinal of the substance for this block. Set during getActualState
     * so that harvest/tool methods can have access to location-dependent substance information.
     */
    public static final PropertyInteger SUBSTANCE = PropertyInteger.create("substance", 0, BlockSubstance.values().length - 1);
    
    protected final WorldLightOpacity worldLightOpacity;
    
    protected final boolean isHyperMatter;
    
    /**
     * 
     * @param blockName
     * @param defaultMaterial
     * @param defaultModelState  Controls render layer visibility for this instance.
     * @param worldLightOpacity
     * @param isHyperMatter
     * @param isGeometryFullCube        If true, blocks with this instance are expected to have a full block geometry
     */
    public SuperModelBlock(String blockName, Material defaultMaterial, BlockRenderMode blockRenderMode, WorldLightOpacity worldLightOpacity, 
                boolean isHyperMatter, boolean isGeometryFullCube)
    {
        super(blockName, defaultMaterial, new ModelState(), blockRenderMode);
        this.isHyperMatter = isHyperMatter;
        this.fullBlock = isGeometryFullCube;
        this.worldLightOpacity = worldLightOpacity;
        this.lightOpacity = worldLightOpacity.opacity;
     }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return this.blockRenderMode == BlockRenderMode.TESR 
                ? new SuperModelTileEntityTESR()
                : new SuperModelTileEntity();
    }
    
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META, SUBSTANCE }, new IUnlistedProperty[] { MODEL_STATE });
    }
    
    @Override
    public int damageDropped(IBlockState state)
    {
        // don't want species to "stick" with SuperModelblocks - so they can restack
        // species will be set again on placement anyway
        return 0;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        // Add substance for tool methods
        return super.getActualState(state, worldIn, pos)
                .withProperty(SUBSTANCE, this.getSubstance(state, worldIn, pos).ordinal());
    }
    
    /** 
     * {@inheritDoc}  <br><br>
     * relying on {@link #getActualState(IBlockState, IBlockAccess, BlockPos)} 
     * to set {@link #SUBSTANCE} property
     */
    @Override
    public int getHarvestLevel(IBlockState state)
    {
        return BlockSubstance.values()[state.getValue(SUBSTANCE)].harvestLevel;
    }
    
    /** 
     * {@inheritDoc}  <br><br>
     * relying on {@link #getActualState(IBlockState, IBlockAccess, BlockPos)} 
     * to set {@link #SUBSTANCE} property
     */
    @Override
    @Nullable public String getHarvestTool(IBlockState state)
    {
        return BlockSubstance.values()[state.getValue(SUBSTANCE)].harvestTool;
    }
    
   
    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return this.getSubstance(blockState, worldIn, pos).hardness;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
    {
        return this.getSubstance(world, pos).resistance;
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity)
    {
        return this.getSubstance(state, world, pos).soundType;
    }

    /**
     * SuperModel blocks light emission level is stored in tile entity.
     * Is not part of model state because does not affect rendering.
     * However, {@link #getLightValue(IBlockState)} will return 0.
     * That version is not used in vanilla forge except to determine if flat
     * render pipeline should be used for emissive blocks.
     * Should not be a problem because render logic also checks
     * isAmbientOcclusion() on the baked model itself.
     * 
     * 
     * FIXME: in latest Forge, block renderer now checks the location-aware version
     * of getLightValue which means it will use flat lighter even when we don't want it to.
     * So we'll need to force this to zero depending on render layer.
     * OTOH - if the block actually does emit light, maybe flat lighter is OK.
     * 
     */
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity myTE = world.getTileEntity(pos);
        return myTE == null || !(myTE instanceof SuperModelTileEntity)
                ? 0
                : ((SuperModelTileEntity)myTE).getLightValue();
    }
    
  
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        // We only want to show one item for supermodelblocks
        // Otherwise will spam creative search / JEI
        // All do the same thing in the end.
        if(this.worldLightOpacity == WorldLightOpacity.SOLID 
                && this.fullBlock 
                && !this.isHyperMatter
                && this.blockRenderMode == BlockRenderMode.SOLID_SHADED)
        {
            list.add(this.getSubItems().get(0));
        }
    }
    
    @Override
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity myTE = world.getTileEntity(pos);
        return myTE == null || !(myTE instanceof SuperModelTileEntity)
                ? BlockSubstance.FLEXSTONE
                : ((SuperModelTileEntity)myTE).getSubstance();
    }
    
    @Override
    public boolean isGeometryFullCube(IBlockState state)
    {
        return this.fullBlock;
    }

    @Override
    public boolean isHypermatter()
    {
        return this.isHyperMatter;
    }
    
    /**
     * Set light level emitted by block.
     * Inputs are masked to 0-15
     */
    public void setLightValue(IBlockState state, IBlockAccess world, BlockPos pos, int lightValue)
    {
        TileEntity myTE = world.getTileEntity(pos);
        if(myTE != null && myTE instanceof SuperModelTileEntity) ((SuperModelTileEntity)myTE).setLightValue((byte)(lightValue & 0xF));
    }

    public void setSubstance(IBlockState state, IBlockAccess world, BlockPos pos, BlockSubstance substance)
    {
        TileEntity myTE = world.getTileEntity(pos);
        if(myTE != null && myTE instanceof SuperModelTileEntity) ((SuperModelTileEntity)myTE).setSubstance(substance);
    }
 
    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state)
    {
        return this.worldLightOpacity;
    }
}

