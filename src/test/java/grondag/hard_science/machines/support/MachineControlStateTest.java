package grondag.hard_science.machines.support;

import org.junit.Test;

import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.MachineControlState.RenderLevel;
import grondag.hard_science.superblock.model.shape.ModelShape;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class MachineControlStateTest
{

    @Test
    public void test()
    {
        ModelState ms = new ModelState();
        ms.setMetaData(7);
        ms.setShape(ModelShape.COLUMN_SQUARE);
        
        BlockPos bp = new BlockPos(5, 9, 84);
        
        MachineControlState original = new MachineControlState();
        original.setLightValue(5);
        original.setMeta(3);
        original.setModelState(ms);
        original.setTargetPos(bp);
        original.setMachineState(MachineState.FABRICATING);
        original.setRenderLevel(RenderLevel.MINIMAL);
//        original.setSubstance(BlockSubstance.HDPE);
        original.startJobTicks((short) 500);
        original.setJobRemainingTicks((short) 50);
        
        assert original.getLightValue() == 5;
        assert original.getMeta() == 3;
        assert original.getModelState().equals(ms);
        assert original.getTargetPos().equals(bp);
        assert original.getMachineState() == MachineState.FABRICATING;
        assert original.getRenderLevel() == RenderLevel.MINIMAL;
//        assert original.getSubstance() == BlockSubstance.HDPE;
        assert  original.getJobDurationTicks() == 500;
        assert  original.getJobRemainingTicks() == 50;
        
        PacketBuffer buff = new PacketBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        
        original.toBytes(buff);
        
        MachineControlState copy = new MachineControlState();
        copy.fromBytes(buff);
        
        assert copy.getLightValue() == original.getLightValue();
        assert copy.getMeta() == original.getMeta();
        assert copy.getModelState().equals(original.getModelState());
        assert copy.getTargetPos().equals(original.getTargetPos());
        assert copy.getMachineState() == original.getMachineState();
        assert copy.getRenderLevel() == original.getRenderLevel();
//        assert copy.getSubstance() == original.getSubstance();
        assert copy.getJobDurationTicks() == original.getJobDurationTicks();
        assert copy.getJobRemainingTicks() == original.getJobRemainingTicks();
        
        NBTTagCompound tag = original.serializeNBT();
        copy = new MachineControlState();
        copy.deserializeNBT(tag);
        
        assert copy.getLightValue() == original.getLightValue();
        assert copy.getMeta() == original.getMeta();
        assert copy.getModelState().equals(original.getModelState());
        assert copy.getTargetPos().equals(original.getTargetPos());
        assert copy.getMachineState() == original.getMachineState();
        assert copy.getRenderLevel() == original.getRenderLevel();
//        assert copy.getSubstance() == original.getSubstance();
        assert copy.getJobDurationTicks() == original.getJobDurationTicks();
        assert copy.getJobRemainingTicks() == original.getJobRemainingTicks();
        
    }

}
