package grondag.adversity.feature.volcano;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import grondag.adversity.feature.volcano.lava.AbstractLavaSimulator;
import grondag.adversity.feature.volcano.lava.blockmodel.LavaCell;
import grondag.adversity.feature.volcano.lava.blockmodel.LavaSimulator;
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
    


    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        Simulator.instance.getFluidTracker().registerPlacedLava(worldIn, pos, state);
    }

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
            LavaCell cell = sim.getCell(data.getPos(), false);
            if(cell == null)
            {
                probeInfo.text("Cell not found.");
            }
            else
            {
                LavaCell up = cell.getUpEfficiently(sim, false);
                LavaCell down = cell.getDownEfficiently(sim, false);
                LavaCell east = cell.getEastEfficiently(sim);
                LavaCell west = cell.getWestEfficiently(sim);
                LavaCell north = cell.getNorthEfficiently(sim);
                LavaCell south =cell.getSouthEfficiently(sim);
                
                
                int netDrop = Math.max(
                        Math.max(east.getDistanceToFlowFloor(), west.getDistanceToFlowFloor()), 
                        Math.max(north.getDistanceToFlowFloor(), south.getDistanceToFlowFloor())
                    ) - cell.getDistanceToFlowFloor();
                
                int netRetention = (cell.getRawRetainedLevel(sim) + down.getRawRetainedLevel(sim)) / AbstractLavaSimulator.FLUID_UNITS_PER_LEVEL - cell.getDistanceToFlowFloor();
                
                probeInfo.text("Cell ID = " + cell.hashCode())
                    .text("CurrentLevel=" + cell.getFluidAmount() + "    RetainedLevel=" + cell.getRawRetainedLevel(sim) + "   MaxLevel=" + cell.maxLevel)
                    .text("interiorFloor=" + cell.getInteriorFloor() + "  distanceToFlowFloor=" + cell.getDistanceToFlowFloor() + " isFlowFloor=" + cell.flowFloorIsFlowBlock())
                    .text("LastFlowTickl = " + cell.getLastFlowTick() + "  currentSimTick=" + sim.getTickIndex() + "  avgLevelWithPrecision=" + cell.avgFluidAmountWithPrecision)
                    .text("Visible Level = " + cell.getCurrentVisibleLevel() + "  Last Visible Level = " + cell.getLastVisibleLevel() 
                            + "  Net Drop = " + netDrop  + "  Net Retention = " + netRetention)
                    .text("Up: " + (up == null ? "null" : "id=" + up.hashCode() + " barrier:" + up.isBarrier() + " drop:" + up.getDistanceToFlowFloor() + " retention:" + up.getRawRetainedLevel(sim)))
                    .text("Down: " + (down == null ? "null" : "id=" + down.hashCode()  + " barrier:" + down.isBarrier() + " drop:" + down.getDistanceToFlowFloor() + " retention:" + down.getRawRetainedLevel(sim)))
                    .text("East: " + (east == null ? "null" : "id=" + east.hashCode() + " barrier:" + east.isBarrier() + " drop:" + east.getDistanceToFlowFloor() + " retention:" + east.getRawRetainedLevel(sim)))
                    .text("West: " + (west == null ? "null" : "id=" + west.hashCode() + " barrier:" + west.isBarrier() + " drop:" + west.getDistanceToFlowFloor() + " retention:" + west.getRawRetainedLevel(sim)))
                    .text("North: " + (north == null ? "null" : "id=" + north.hashCode() + " barrier:" + north.isBarrier() + " drop:" + north.getDistanceToFlowFloor() + " retention:" + north.getRawRetainedLevel(sim)))
                    .text("South: " + (south == null ? "null" : "id=" + south.hashCode() + " barrier:" + south.isBarrier() + " drop:" + south.getDistanceToFlowFloor() + " retention:" + south.getRawRetainedLevel(sim)));
            }
        }
    }

}
