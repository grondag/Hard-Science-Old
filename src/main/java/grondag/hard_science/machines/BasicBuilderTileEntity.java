package grondag.hard_science.machines;

import grondag.hard_science.Configurator;
import grondag.hard_science.init.ModBlocks;
import grondag.hard_science.init.ModSuperModelBlocks;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.virtualblock.VirtualBlock;
import grondag.hard_science.virtualblock.VirtualBlockTracker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class BasicBuilderTileEntity extends MachineTileEntity implements ITickable
{
    private int xChunkOffset = 0;
    private int zChunkOffset = 0;
    private BlockPos checkPos;

    @Override
    public void update()
    {
        if(world.isRemote || !this.isOn()) return;
        
        if(checkPos == null) checkPos = this.pos;
        
        // look for a virtual block in current target chunk
        BlockPos targetPos = VirtualBlockTracker.INSTANCE.get(this.world).dequeue(checkPos);
        
        // if nothing found in this chunk, move to next chunk and exit
        if(targetPos == null)
        {
            if(++xChunkOffset >= Configurator.MACHINES.basicBuilderChunkRadius)
            {
                xChunkOffset = -Configurator.MACHINES.basicBuilderChunkRadius;
                if(++zChunkOffset > Configurator.MACHINES.basicBuilderChunkRadius)
                {
                    zChunkOffset = -Configurator.MACHINES.basicBuilderChunkRadius;
                }
            }
            this.checkPos = this.pos.add(this.xChunkOffset * 16, 0, this.zChunkOffset * 16);
            return;
        }
        
        // look for a virtual block at target position, get state if found
        IBlockState oldState = this.world.getBlockState(targetPos);
        
        if(oldState.getBlock() != ModBlocks.virtual_block) return;
        
        TileEntity rawTE = world.getTileEntity(targetPos);
        if(rawTE == null || !(rawTE instanceof SuperModelTileEntity)) return;
        SuperModelTileEntity oldTE = (SuperModelTileEntity)rawTE;
        
        ModelState modelState = ((VirtualBlock)ModBlocks.virtual_block).getModelStateAssumeStateIsCurrent(oldState, world, targetPos, true);
        SuperModelBlock newBlock = ModSuperModelBlocks.findAppropriateSuperModelBlock(oldTE.getSubstance(), modelState);

        world.setBlockState(targetPos, newBlock.getDefaultState().withProperty(SuperBlock.META, oldState.getValue(SuperBlock.META)));
        
        SuperModelTileEntity newTE = (SuperModelTileEntity)world.getTileEntity(targetPos);
        if (newTE == null) return;
        
        newTE.setLightValue(oldTE.getLightValue());
        newTE.setSubstance(oldTE.getSubstance());
        newTE.setModelState(modelState);
    }
}
