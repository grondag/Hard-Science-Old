package grondag.adversity.superblock.block;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.adversity.Configurator;
import grondag.adversity.library.render.LightingMode;
import grondag.adversity.superblock.color.BlockColorMapProvider;
import grondag.adversity.superblock.color.ColorMap;
import grondag.adversity.superblock.items.SuperItemBlock;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.BlockRenderLayerSet;
import grondag.adversity.superblock.model.state.ModelStateProperty;
import grondag.adversity.superblock.model.state.PaintLayer;
import grondag.adversity.superblock.model.state.WorldLightOpacity;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.placement.SpeciesGenerator;
import grondag.adversity.superblock.texture.Textures;
import grondag.adversity.superblock.varia.BlockSubstance;
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
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * User-configurable Adversity building blocks.<br><br>
 * 
 * While most attributes are stored in stack/tile entity NBT
 * some important methods are called without reference to a location or stack.
 * For these, we have multiple instances of this block and use a different instance
 * depending on the combination of attributes needed.<br><br>
 * 
 * The choice of which block to deploy is made by the item/creative stack that places the block
 * by calling {@link grondag.adversity.init.ModSuperModelBlocks#findAppropriateSuperModelBlock(BlockSubstance substance, ModelState modelState)} <br><br>
 * 
 * The specific dimensions by which the block instances vary are: {@link #renderLayerSet}, {@link #worldLightOpacity}, Block.fullBlock and {@link #isHypermatter()}.
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
        //all superblocks have same display name
        this.setUnlocalizedName("super_model_block");
        this.isHyperMatter = isHyperMatter;
        this.fullBlock = isGeometryFullCube;
        this.worldLightOpacity = worldLightOpacity;
        this.renderLayerSet = renderLayerSet;
        this.renderLayerEnabledFlags = renderLayerSet.blockRenderLayerFlags;
        this.lightOpacity = worldLightOpacity.opacity;
        
        // dispatcher reports always reports shading enabled for supermodel blocks
        // light level is used for fullbright rendering instead
        this.renderLayerShadedFlags = ModelState.BENUMSET_RENDER_LAYER.getFlagsForIncludedValues(BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED, BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
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
    
    /**
     * {@inheritDoc}
     * 
     * Model dispatcher always returns isAmbientOcclusion=true for SuperModelBlocks.
     * We want getLightValue() to return a non-zero value for fullbright layers to force disable of AO.
     * When getLightValue() is called it passes in an extended state, so we can check for modeLstate 
     * populated in getExtendedState and if true for the current layer return 1 for the light value.
     * Means that all glowing blocks emit at least a tiny amount of light, except that actual 
     * light calculations are done via the location-aware version of getLightValue(), so should be fine.
     */

    @SuppressWarnings("deprecation")
    @Override
    public int getLightValue(IBlockState state)
    {
        int min = 0;
        
        if(state instanceof IExtendedBlockState)
        {
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            if(layer != null)
            {
                ModelState modelState = ((IExtendedBlockState)state).getValue(MODEL_STATE);
                if(modelState != null)
                {
                    if(!modelState.isLayerShaded(layer)) min = 1;
                }
            }
        }
        return Math.max(min, super.getLightValue(state));
    }
    
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        SuperModelTileEntity myTE = (SuperModelTileEntity) world.getTileEntity(pos);
        if(myTE != null)
        {
            SuperItemBlock.setStackLightValue(stack, myTE.getLightValue());
            SuperItemBlock.setStackSubstance(stack, myTE.getSubstance());
        }
        return stack;
    }
  
    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list)
    {
        // We only want to show one item for supermodelblocks
        // Otherwise will spam creative search / JEI
        // All do the same thing in the end.
        if(this.worldLightOpacity == WorldLightOpacity.SOLID 
                && this.renderLayerSet == BlockRenderLayerSet.ALL 
                && this.fullBlock 
                && !this.isHyperMatter)
        {
            list.add(this.getSubItems().get(0));
        }
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

