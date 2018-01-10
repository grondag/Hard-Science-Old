package grondag.hard_science.simulator.device;

import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;

public interface IDeviceComponent extends IDomainMember
{
    public IDevice device();
    
    /**
     * Shorthand for {@link #device()#getDomain()}
     */
    @Override
    public default Domain getDomain()
    {
        return this.device().getDomain();
    }
    
    /**
     * Shorthand for {@link #device()#isConnected()}
     */
    public default boolean isConnected()
    {
        return this.device().isConnected();
    }
    
    /**
     * Shorthand for {@link #device()#setDirty()}
     */
    public default void setDirty()
    {
        this.device().setDirty();
    }
    
    /**
     * Shorthand for {@link #device()#isOn()}
     */
    public default boolean isOn()
    {
        return this.device().isOn();
    }
}
