package grondag.adversity.superblock.terrain;

import grondag.adversity.feature.volcano.lava.simulator.LavaCell;
import grondag.adversity.feature.volcano.lava.simulator.LavaSimulator;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.support.BlockSubstance;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LavaBlock extends TerrainDynamicBlock
{

    public LavaBlock(String blockName, BlockSubstance substance, ModelState defaultModelState, boolean isFiller)
    {
        super(blockName, substance, defaultModelState, isFiller);
        this.setTickRandomly(true);
    }
    
    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return false;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
    {
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        handleFallingBlocks(worldIn, pos, state);
        Simulator.INSTANCE.getFluidTracker().notifyBlockChange(worldIn, fromPos);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        handleFallingBlocks(worldIn, pos, state);
        Simulator.INSTANCE.getFluidTracker().registerPlacedLava(worldIn, pos, state);
    }
    
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        if(this.isFlowHeight()) Simulator.INSTANCE.getFluidTracker().unregisterDestroyedLava(worldIn, pos, state);
    }
    
    private void handleFallingBlocks(World worldIn, BlockPos pos, IBlockState state)
    {
        if(worldIn.isRemote) return;
        
        final BlockPos upPos = pos.up();
        final IBlockState upState = worldIn.getBlockState(upPos);
        final Block upBlock = upState.getBlock();

        if(upBlock instanceof BlockFalling) 
        {
            worldIn.setBlockToAir(upPos);
        }
        else if(upBlock == Blocks.FLOWING_WATER || upBlock == Blocks.FLOWING_LAVA)
        {
            if(upBlock instanceof BlockDynamicLiquid)
            {
                int level = upState.getValue(BlockLiquid.LEVEL);
                if( level < 8)
                {
                    worldIn.setBlockToAir(upPos);
                }
            }
        }
    }
    

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
    {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        if(Simulator.INSTANCE.getFluidTracker() instanceof LavaSimulator)
        {
            LavaSimulator sim = (LavaSimulator)Simulator.INSTANCE.getFluidTracker();
            BlockPos pos = data.getPos();  
            LavaCell cell = sim.cells.getCellIfExists(pos.getX(), pos.getY(), pos.getZ());
            if(cell == null)
            {
                probeInfo.text("Cell not found.");
            }
            else
            {
                probeInfo.text("Cell ID = " + cell.id)
                    .text("FluidUnits=" + cell.fluidUnits() + "  FluidSurfaceLevel=" + cell.worldSurfaceLevel() + "  Fluid Levels=" + (cell.fluidLevels()))
                    .text("RawRetainedUnits=" + cell.getRawRetainedUnits() + "  RawRetained Depth=" + (cell.getRawRetainedUnits() / LavaSimulator.FLUID_UNITS_PER_LEVEL))
                    .text("SmoothRetainedUnits=" + cell.getSmoothedRetainedUnits() + "  SmoothRetained Depth=" + (cell.getSmoothedRetainedUnits() / LavaSimulator.FLUID_UNITS_PER_LEVEL))
                    .text("floor=" + cell.floorLevel() + "  ceiling=" + cell.ceilingLevel() + " isFlowFloor=" + cell.isBottomFlow() + " floorFlowHeight=" + cell.floorFlowHeight())
                    .text(" avgLevelWithPrecisionShifted=" + (cell.avgFluidSurfaceLevelWithPrecision >> 6))
                    .text("Visible Level = " + cell.getCurrentVisibleLevel() + "  Last Visible Level = " + cell.getLastVisibleLevel())
                    .text("Connection Count = " + cell.connections.size());
            }
        }
    }


}
