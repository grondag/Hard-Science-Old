package grondag.hard_science.simulator.device.impl;

import grondag.hard_science.simulator.device.AbstractDevice;
import grondag.hard_science.simulator.resource.IResource;

public class TestDevice extends AbstractDevice
{

    @Override
    public long onProduce(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long onConsume(IResource<?> resource, long quantity, boolean allowPartial, boolean simulate)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
