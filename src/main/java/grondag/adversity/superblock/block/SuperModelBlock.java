package grondag.adversity.superblock.block;

import java.util.List;

import javax.annotation.Nullable;

import grondag.adversity.Configurator;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SuperModelBlock extends SuperBlock implements ITileEntityProvider 
{
    
    public SuperModelBlock(BaseMaterial material, String styleName)
    {
        super(material, styleName);
    }
        
    
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SuperTileEntity();        
    }
    
    /**
     * Defer destruction of block until after drops when harvesting so can gather NBT from tile entity.
     */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    /**
     * Need to destroy block here because did not do it during removedByPlayer.
     */
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) 
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }
    
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ItemStack stack = super.getStackFromBlock(state, world, pos);
        
        if(stack != null)
        {
            SuperItemBlock.setModelState(stack, this.getModelState(state, world, pos));
        }

        return stack;
    }
    
    //for tile-entity blocks, almost never use meta on items
    @Override
    public int damageDropped(IBlockState state)
    {
        return 0;
    }
    
    @Override
    public boolean hasAppearanceGui()
    {
        return true;
    }

    @Override
    public ModelState getModelState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        SuperTileEntity myTE = (SuperTileEntity) world.getTileEntity(pos);
        if(myTE != null) 
        {
          return myTE.getModelState(state, world, pos);
        }
        else
        {
            return this.getDefaultModelState();
        }
    }
    
    public void updateTileEntityOnPlacedBlockFromStack(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState newState, SuperTileEntity niceTE)
    {
        // default is NOOP
    }
    
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        ModelState modelState = SuperItemBlock.getModelState(stack);
        
        ColorMap colorMap = modelState.getColorMap(0);
        if(colorMap != null)
        {
            tooltip.add("Color: " + colorMap.colorMapName);
        }
    }
    
    // BLOCK PROPERTIES
    

    @SuppressWarnings("deprecation")
    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn)
    {
        // hyperstone blocks can be configured to prevent mob spawning
        
        if(this.material.isHyperMaterial && !Configurator.HYPERSTONE.allowMobSpawning)
        {
            return false;
        }
        else
        {
            return super.canEntitySpawn(state, entityIn);
        }
    }

    @Override
    public  boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return ModelState.BENUMSET_RENDER_LAYER.isFlagSetForValue(layer, state.getValue(META));
    }
}

