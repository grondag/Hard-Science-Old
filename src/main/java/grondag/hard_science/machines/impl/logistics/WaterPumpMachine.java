package grondag.hard_science.machines.impl.logistics;

import grondag.hard_science.machines.base.AbstractSimpleMachine;

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
}
