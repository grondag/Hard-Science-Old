package grondag.hard_science.superblock.block;

import javax.annotation.Nullable;

import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.model.state.MetaUsage;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** base class for tile entity blocks */
public abstract class SuperBlockPlus extends SuperBlock implements ITileEntityProvider
{
    public SuperBlockPlus(String blockName, Material defaultMaterial, ModelState defaultModelState)
    {
        super(blockName, defaultMaterial, defaultModelState);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SuperTileEntity();        
    }


    @Override
    public ModelState getModelStateAssumeStateIsStale(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        TileEntity myTE = world.getTileEntity(pos);
        if(myTE != null && myTE instanceof SuperTileEntity) 
        {
            IBlockState currentState = world.getBlockState(pos);
            ModelState result = ((SuperTileEntity)myTE).getModelState(currentState, world, pos, refreshFromWorldIfNeeded);
            
            // honor passed in species if different
            if(currentState.getValue(META) != state.getValue(META) && result.metaUsage() != MetaUsage.NONE)
            {
                result = result.clone();
                result.setMetaData(state.getValue(META));
            }
            return result;
        }
        else
        {
            return super.getModelStateAssumeStateIsStale(state, world, pos, refreshFromWorldIfNeeded);
        }
    }
    
    /** 
     * Use when absolutely certain given block state is current.
     */
    @Override
    public ModelState getModelStateAssumeStateIsCurrent(IBlockState state, IBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        TileEntity myTE = world.getTileEntity(pos);
        if(myTE != null && myTE instanceof SuperTileEntity) 
        {
            return ((SuperTileEntity)myTE).getModelState(state, world, pos, refreshFromWorldIfNeeded);
            
        }
        else
        {
            return super.getModelStateAssumeStateIsCurrent(state, world, pos, refreshFromWorldIfNeeded);
        }

    }
    
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ItemStack stack = super.getStackFromBlock(state, world, pos);
        
        if(stack != null)
        {
            SuperItemBlock.setModelState(stack, this.getModelStateAssumeStateIsStale(state, world, pos, true));
        }

        return stack;
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
    
    
    /**
     * {@inheritDoc} <br><br>
     * 
     * SuperModelBlock: Defer destruction of block until after drops when harvesting so can gather NBT from tile entity.
     */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) 
    {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    public void setModelState(World world, BlockPos pos, ModelState modelState)
    {
        TileEntity blockTE = world.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof SuperTileEntity) 
        {
            ((SuperTileEntity)blockTE).setModelState(modelState);
        }
    }
}
