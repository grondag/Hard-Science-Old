package grondag.adversity.feature.volcano;

import grondag.adversity.feature.volcano.lava.simulator.LavaCell;
import grondag.adversity.feature.volcano.lava.simulator.LavaSimulator;
import grondag.adversity.niceblock.base.ModelDispatcher;
import grondag.adversity.niceblock.block.FlowDynamicBlock;
import grondag.adversity.niceblock.support.BaseMaterial;
import grondag.adversity.simulator.Simulator;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
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

public class VolcanicLavaBlock extends FlowDynamicBlock implements IProbeInfoAccessor
{
    public VolcanicLavaBlock(ModelDispatcher dispatcher, BaseMaterial material, String styleName, boolean isFiller)
    {
        super(dispatcher, material, styleName, isFiller);
        
        //TODO: provide config to turn this on - default off, can affect framerate
        //        this.setLightLevel(4F/15F);
        
        //TODO: add configs for these also
//        this.setBlockUnbreakable();
//        this.setResistance(2000F);
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

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
        super.neighborChanged(state, worldIn, pos, blockIn);
        handleFallingBlocks(worldIn, pos, state);
        Simulator.instance.getFluidTracker().notifyLavaNeighborChange(worldIn, pos, state);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        handleFallingBlocks(worldIn, pos, state);
        Simulator.instance.getFluidTracker().registerPlacedLava(worldIn, pos, state);
    }
    
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        if(this.isFlowHeight()) Simulator.instance.getFluidTracker().unregisterDestroyedLava(worldIn, pos, state);
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
        if(Simulator.instance.getFluidTracker() instanceof LavaSimulator)
        {
            LavaSimulator sim = (LavaSimulator)Simulator.instance.getFluidTracker();
            BlockPos pos = data.getPos();  
            LavaCell cell = sim.cells.getCellIfExists(pos.getX(), pos.getY(), pos.getZ());
            if(cell == null)
            {
                probeInfo.text("Cell not found.");
            }
            else
            {
                probeInfo.text("Cell ID = " + cell.id)
                    .text("FluidUnits=" + cell.getFluidUnits() + "  FluidSurfaceLevel=" + cell.fluidSurfaceLevel() + "  Fluid Levels=" + (cell.getFluidUnits() / LavaSimulator.FLUID_UNITS_PER_LEVEL))
                    .text("RawRetainedUnits=" + cell.getRawRetainedUnits() + "  RawRetained Depth=" + ((cell.getRawRetainedUnits() - cell.getFloorUnits()) / LavaSimulator.FLUID_UNITS_PER_LEVEL))
                    .text("SmoothRetainedUnits=" + cell.getSmoothedRetainedUnits() + "  SmoothRetained Depth=" + ((cell.getSmoothedRetainedUnits() - cell.getFloorUnits()) / LavaSimulator.FLUID_UNITS_PER_LEVEL))
                    .text("floor=" + cell.getFloor() + "  ceiling=" + cell.getCeiling() + " isFlowFloor=" + cell.isBottomFlow() + " floorFlowHeight=" + cell.floorFlowHeight())
                    .text(" avgLevelWithPrecisionShifted=" + (cell.avgFluidSurfaceUnitsWithPrecision >> 6))
                    .text("Visible Level = " + cell.getCurrentVisibleLevel() + "  Last Visible Level = " + cell.getLastVisibleLevel())
                    .text("Connection Count = " + cell.connections.size());
            }
        }
    }

}
