package grondag.hard_science.machines.impl.logistics;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.matbuffer.BufferManager2;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.StorageType;

public class WaterPumpMachine extends AbstractSimpleMachine
{
    protected WaterPumpMachine()
    {
        super();
    }

    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return true;
    }

    protected BufferManager2 createBufferManager()
    {
        return new BufferManager2(
                this, 
                0L, 
                StorageType.ITEM.MATCH_NONE, 
                0L, 
                0L, 
                StorageType.FLUID.MATCH_NONE, 
                VolumeUnits.KILOLITER.nL * 16);
    }
}
