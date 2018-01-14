package grondag.hard_science.machines.base;

import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.device.blocks.SimpleBlockHandler;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.PortType;
import grondag.hard_science.simulator.transport.management.ITransportManager;
import grondag.hard_science.simulator.transport.management.SimpleTransportManager;

/**
 * Base class for single-block machines.
 *
 */
public abstract class AbstractSimpleMachine extends AbstractMachine
{
    /**
     * Ports on a simple machine are all of the same level.
     */
    protected final CarrierLevel carrierLevel;
    
    /**
     * Ports on a simple machine are all of the same type.
     */
    protected final PortType portType;

    protected AbstractSimpleMachine(CarrierLevel carrierLevel, PortType portType)
    {
        this.carrierLevel = carrierLevel;
        this.portType = portType;
    }
    
    @Override
    protected ITransportManager<StorageTypeStack> createItemTransportManager()
    {
        return new SimpleTransportManager<StorageTypeStack>(this, StorageType.ITEM);
    }

    @Override
    protected ITransportManager<StorageTypeFluid> createFluidTransportManager()
    {
        return new SimpleTransportManager<StorageTypeFluid>(this, StorageType.FLUID);
    }
    
    @Override
    protected ITransportManager<StorageTypePower> createPowerTransportManager()
    {
        return new SimpleTransportManager<StorageTypePower>(this, StorageType.POWER);
    }
    
    @Override
    protected IDeviceBlockManager createBlockManager()
    {
        SimpleBlockHandler result = new SimpleBlockHandler(
                this, 
                this.hasChannel() ? this.getChannel() : 0, 
                this.carrierLevel, 
                this.portType);
        
        return result;
    }
    
//    @Override
//    public boolean hasFront() { return true; }
//    
//    @Override
//    public void setFront(@Nonnull EnumFacing frontFace)
//    {
//        this.frontFace = frontFace;
//        
//        // TODO: if changed after connected, 
//        // then need to remove and replace device blocks
//        // OR ensure this use case doesn't happen
//    }
//    
//    @Nullable
//    @Override
//    public EnumFacing getFront()
//    {
//        return this.frontFace;
//    }    
    
    
}
