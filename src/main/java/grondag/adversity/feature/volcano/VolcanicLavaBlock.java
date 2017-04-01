package grondag.adversity.feature.volcano;

import java.util.ArrayList;
import java.util.List;

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
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
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
        Simulator.instance.getFluidTracker().unregisterDestroyedLava(worldIn, pos, state);
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
    

// TODO: this will not work with column-based model, would need a different purpose-specific method to confirm existing lava
//    @Override
//    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
//    {
//        Simulator.instance.getFluidTracker().registerPlacedLava(worldIn, pos, state);
//    }

    //TODO: make this work or remove it
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        ArrayList<String> lines = new ArrayList<String>(2);
        lines.add("Testing!");
        return lines;
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
                    .text("FluidUnits=" + cell.getFluidUnits() + "  Fluid Levels=" + (cell.getFluidUnits() / LavaSimulator.FLUID_UNITS_PER_LEVEL))
                    .text("RawRetainedLevel=" + cell.getRawRetainedLevel() + "  RawRetained Depth=" + (cell.getRawRetainedLevel() - cell.getFloor()))
                    .text("floor=" + cell.getFloor() + "  ceiling=" + cell.getCeiling() + " isFlowFloor=" + cell.isBottomFlow())
                    .text(" avgLevelWithPrecisionShifted=" + (cell.avgFluidSurfaceUnitsWithPrecision >> 6))
                    .text("Visible Level = " + cell.getCurrentVisibleLevel() + "  Last Visible Level = " + cell.getLastVisibleLevel())
                    .text("Connection Count = " + cell.connections.size());
                
//                for(LavaConnection conn : cell.connections.values())
//                {
//                    LavaCell other = conn.getOther(cell);
//                    probeInfo.text("Conn ID=" + conn.id + "  x=" + other.x() + "  z=" + other.z() + "  bottomY=" + other.bottomY() + "  fluidUnits=" + other.getFluidUnits() 
//                    + " isActive=" + conn.isActive() + "  isDeleted=" +  conn.isDeleted() + "  sortBucket=" + conn.getSortBucket() + "  cellID=" + other.id);
//                }
            }
        }
    }

}
