package grondag.adversity.superblock.block;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.adversity.Configurator;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.base.NiceTileEntity;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateProperty;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.placement.SpeciesGenerator;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * User-configurable Adversity building blocks.
 */

@SuppressWarnings("unused")
public class SuperModelBlock extends SuperBlockPlus  
{
    /**
     * Ordinal of the substance for this block. Set during getActualState
     * so that harvest/tool methods can have access to location-dependent substance information.
     */
    public static final PropertyInteger SUBSTANCE = PropertyInteger.create("substance", 0, BlockSubstance.values().length - 1);
    
    protected final WorldLightOpacity worldLightOpacity;
    
    protected final boolean isHyperMatter;
    
    public final BlockRenderLayerSet renderLayerSet;
    
    /**
     * 
     * @param blockName
     * @param defaultMaterial
     * @param defaultModelState  Controls render layer visibility for this instance.
     * @param worldLightOpacity
     * @param isHyperMatter
     * @param isGeometryFullCube        If true, blocks with this instance are expected to have a full block geometry
     */
    public SuperModelBlock(String blockName, Material defaultMaterial, BlockRenderLayerSet renderLayerSet, WorldLightOpacity worldLightOpacity, 
                boolean isHyperMatter, boolean isGeometryFullCube)
    {
        super(blockName, defaultMaterial, new ModelState());
        this.isHyperMatter = isHyperMatter;
        this.fullBlock = isGeometryFullCube;
        this.worldLightOpacity = worldLightOpacity;
        this.renderLayerSet = renderLayerSet;
        this.renderLayerFlags = renderLayerSet.blockRenderLayerFlags;
        this.lightOpacity = worldLightOpacity.opacity;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SuperModelTileEntity();        
    }
    
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { META, SUBSTANCE }, new IUnlistedProperty[] { MODEL_STATE });
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
    
    /**
     * SuperModel blocks light emission level is stored in tile entity.
     * Is not part of model state because does not affect rendering.
     * However, {@link #getLightValue(IBlockState)} will return 0.
     * That version is not used in vanilla forge except to determine if flat
     * render pipeline should be used for emissive blocks.
     * Should not be a problem because render logic also checks
     * isAmbientOcclusion() on the baked model itself.
     * 
     */
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        SuperModelTileEntity myTE = (SuperModelTileEntity) world.getTileEntity(pos);
        return myTE == null
                ? 0
                : myTE.getLightValue();
    }
    
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        SuperModelTileEntity myTE = (SuperModelTileEntity) world.getTileEntity(pos);
        if(myTE != null)
        {
            int placementShape = myTE.getPlacementShape() != 0 ? myTE.getPlacementShape() : SpeciesGenerator.PLACEMENT_3x3x3;
            SuperItemBlock.setStackPlacementShape(stack, placementShape);
            SuperItemBlock.setStackLightValue(stack, myTE.getLightValue());
            SuperItemBlock.setStackSubstance(stack, myTE.getSubstance());
        }
        return stack;
    }
  
    
    @Override
    public BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        SuperModelTileEntity myTE = (SuperModelTileEntity) world.getTileEntity(pos);
        return myTE == null
                ? BlockSubstance.FLEXSTONE
                : myTE.getSubstance();
    }
    
    @Override
    public boolean hasAppearanceGui()
    {
        return true;
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
        SuperModelTileEntity myTE = (SuperModelTileEntity) world.getTileEntity(pos);
        if(myTE != null) myTE.setLightValue((byte)(lightValue & 0xF));
    }

    public void setSubstance(IBlockState state, IBlockAccess world, BlockPos pos, BlockSubstance substance)
    {
        SuperModelTileEntity myTE = (SuperModelTileEntity) world.getTileEntity(pos);
        if(myTE != null) myTE.setSubstance(substance);
    }
 
    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state)
    {
        return this.worldLightOpacity;
    }
}

