package grondag.hard_science.machines.base;

import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.device.blocks.SimpleBlockHandler;

/**
 * Base class for single-block machines.
 *
 */
public abstract class AbstractSimpleMachine extends AbstractMachine
{
    
//    /**
//     * Front (display) face of machine, if non-null.
//     * Used to determine placement of connectors.
//     */
//    @Nullable
//    protected EnumFacing frontFace;
    
    @Override
    protected IDeviceBlockManager createBlockManager()
    {
        SimpleBlockHandler result = new SimpleBlockHandler(this);
        
//        assert this.frontFace != null
//                : "Simple machine missing front face during block manager initializaiton";
        
//        for(EnumFacing face : EnumFacing.VALUES)
//        {
//            result.addPort(face, ModPorts.item_low, ItemBus.ITEM_BUS_LOCAL, 0);
//            result.addPort(face, ModPorts.power_low, PowerBus.POWER_BUS_LOCAL, 0);
//        }
        
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
