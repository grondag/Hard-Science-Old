package grondag.adversity.feature.volcano;

import java.util.ArrayList;
import java.util.List;

import grondag.adversity.feature.volcano.lava.LavaCell;
import grondag.adversity.feature.volcano.lava.LavaCellConnection;
import grondag.adversity.feature.volcano.lava.LavaSimulator;
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
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
        this.setLightLevel(4F/15F);
//        this.setBlockUnbreakable();
//        this.setResistance(2000F);
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
        
        if(worldIn.getBlockState(pos.up()).getBlock() instanceof BlockFalling)
        {
            worldIn.setBlockToAir(pos.up());
        }
    }
    
    @Override
    public int tickRate(World worldIn)
    {
        return 2;
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
        LavaSimulator sim = Simulator.instance.getFluidTracker();
        LavaCell cell = sim.getCell(data.getPos());
        if(cell == null)
        {
            probeInfo.text("Cell not found.");
        }
        else
        {
            LavaCellConnection up = sim.getConnection(data.getPos(), data.getPos().up());
            LavaCellConnection down = sim.getConnection(data.getPos(), data.getPos().down());
            LavaCellConnection east = sim.getConnection(data.getPos(), data.getPos().east());
            LavaCellConnection west = sim.getConnection(data.getPos(), data.getPos().west());
            LavaCellConnection north = sim.getConnection(data.getPos(), data.getPos().north());
            LavaCellConnection south = sim.getConnection(data.getPos(), data.getPos().south());
            
            probeInfo.text("Cell ID = " + cell.hashCode())
                .text("Current Fluid Level = " + Float.toString(cell.getFluidAmount()))
                .text("Floor Level = " + Float.toString(cell.getFloor()))
                .text("Delta = " + Float.toString(cell.getDelta()))
                .text("Retained Level = " + Float.toString(cell.getRetainedLevel()))
                .text("Visible Level = " + cell.getVisibleLevel())
                .text("Up: " + (up == null ? "null" : "sortKey=" + up.getSortKey() + "  curFlow=" + up.getCurrentFlowRate() + "  drop=" + up.getDrop()))
                .text("Down: " + (down == null ? "null" : "sortKey=" + down.getSortKey() + "  curFlow=" + down.getCurrentFlowRate() + "  drop=" + down.getDrop()))
                .text("East: " + (east == null ? "null" : "sortKey=" + east.getSortKey() + "  curFlow=" + east.getCurrentFlowRate() + "  drop=" + east.getDrop()))
                .text("West: " + (west == null ? "null" : "sortKey=" + west.getSortKey() + "  curFlow=" + west.getCurrentFlowRate() + "  drop=" + west.getDrop()))
                .text("North: " + (north == null ? "null" : "sortKey=" + north.getSortKey() + "  curFlow=" + north.getCurrentFlowRate() + "  drop=" + north.getDrop()))
                .text("South: " + (south == null ? "null" : "sortKey=" + south.getSortKey() + "  curFlow=" + south.getCurrentFlowRate() + "  drop=" + south.getDrop()));
        }
        
    }

}
