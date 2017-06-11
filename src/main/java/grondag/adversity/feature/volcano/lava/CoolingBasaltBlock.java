package grondag.adversity.feature.volcano.lava;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import grondag.adversity.feature.volcano.lava.simulator.WorldStateBuffer;
import grondag.adversity.init.ModBlocks;
import grondag.adversity.simulator.Simulator;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.terrain.TerrainBlock;
import grondag.adversity.superblock.terrain.TerrainDynamicBlock;
import grondag.adversity.superblock.varia.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoolingBasaltBlock extends TerrainDynamicBlock
{

    protected TerrainDynamicBlock nextCoolingBlock;
    protected int heatLevel = 0;

    public CoolingBasaltBlock(String blockName, BlockSubstance substance, ModelState defaultModelState, boolean isFiller)
    {
        super(blockName, substance, defaultModelState, isFiller);
        this.setTickRandomly(true);
    }

    public static enum CoolingResult
    {
        /** means no more cooling can take place */
        COMPLETE,
        /** means one stage completed - more remain */
        PARTIAL,
        /** means block wan't ready to cool */
        UNREADY,
        /** means this isn't a cooling block*/
        INVALID
    }
    
    /**
     * Cools this block if ready and returns true if successful.
     */
    public CoolingResult tryCooling(WorldStateBuffer worldIn, BlockPos pos, final IBlockState state)
    {
        if(state.getBlock() == this)
        {
            if(canCool(worldIn, pos, state))
            {
                if(this.nextCoolingBlock == ModBlocks.basalt_cool_dynamic_height)
                {
                    if( TerrainBlock.shouldBeFullCube(state, worldIn, pos))
                    {
                        worldIn.setBlockState(pos.getX(), pos.getY(), pos.getZ(), ModBlocks.basalt_cut.getDefaultState().withProperty(SuperBlock.META, state.getValue(SuperBlock.META)), state);
                    }
                    else
                    {
                        worldIn.setBlockState(pos.getX(), pos.getY(), pos.getZ(), this.nextCoolingBlock.getDefaultState().withProperty(SuperBlock.META, state.getValue(SuperBlock.META)), state);
                    }
                    return CoolingResult.COMPLETE;
                }
                else
                {
                    worldIn.setBlockState(pos.getX(), pos.getY(), pos.getZ(), this.nextCoolingBlock.getDefaultState().withProperty(SuperBlock.META, state.getValue(SuperBlock.META)), state);
                    return CoolingResult.PARTIAL;
                }
            }
            else
            {
                return CoolingResult.UNREADY;
            }
            
        }
        else
        {
            return CoolingResult.INVALID;
        }
        
    }
    
    /** True if no adjacent blocks are hotter than me and at least four adjacent blocks are cooler.
     * Occasionally can cool if only three are cooler. */
    public boolean canCool(WorldStateBuffer worldIn, BlockPos pos, IBlockState state)
    {
        if(TerrainBlock.shouldBeFullCube(state, worldIn, pos)) return true;
        
        int chances = 0;
        boolean awayFromLava = true;
        for(EnumFacing face : EnumFacing.VALUES)
        {
            IBlockState testState = worldIn.getBlockState(pos.add(face.getDirectionVec()));
            if(testState != null)
            {
                Block neighbor = testState.getBlock();
                
                if(neighbor == ModBlocks.lava_dynamic_height
                        || neighbor == ModBlocks.lava_dynamic_filler) 
                {
                    awayFromLava = false;
                }
                else if(neighbor instanceof CoolingBasaltBlock)
                {
                    int heat = ((CoolingBasaltBlock) neighbor).heatLevel;
                    if(heat < this.heatLevel)
                    chances += (this.heatLevel - heat);
                }
                else
                {
                    chances += 2;
                }
            }
        }
       
        return (ThreadLocalRandom.current().nextInt(1) < chances) && (awayFromLava || ThreadLocalRandom.current().nextInt(10) == 0);
        
    }
    
    public CoolingBasaltBlock setCoolingBlockInfo(TerrainDynamicBlock nextCoolingBlock, int heatLevel)
    {
        this.nextCoolingBlock = nextCoolingBlock;
        this.heatLevel = heatLevel;
        return this;
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        // Gather orphaned blocks
        Simulator.INSTANCE.getFluidTracker().registerCoolingBlock(worldIn, pos);
    }
    
    
}
