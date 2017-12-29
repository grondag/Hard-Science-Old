package grondag.hard_science.machines;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;

public class SolarCableMachine extends AbstractSimpleMachine
{
    public SolarCableMachine()
    {
        super(CarrierLevel.BOTTOM, PortType.CARRIER);
    }
        
    @Override
    public long onProduce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
    {
        return 0;
    }
    
    @Override
    public long onConsume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
    {
        return 0;
    }
}
