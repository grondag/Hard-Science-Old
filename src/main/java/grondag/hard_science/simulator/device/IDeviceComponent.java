package grondag.hard_science.simulator.device;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;

public interface IDeviceComponent extends IDomainMember
{
    public IDevice device();
    
    @Override
    public default Domain getDomain()
    {
        return this.device().getDomain();
    }
}
