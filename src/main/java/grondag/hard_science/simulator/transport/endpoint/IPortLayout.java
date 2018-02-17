package grondag.hard_science.simulator.transport.endpoint;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public interface IPortLayout
{
    public PortFace getFace(EnumFacing face);
    
    public default ImmutableList<Port<StorageTypeFluid>> createFluidPorts(IDevice owner)
    {
        return Helper.createFluidPorts(this, owner, owner.getLocation());
    }
    
    public default ImmutableList<Port<StorageTypeStack>> createItemPorts(IDevice owner)
    {
        return Helper.createPorts(StorageType.ITEM, this, owner, owner.getLocation(), owner.getChannel());
    }
    
    public default ImmutableList<Port<StorageTypePower>> createPowerPorts(IDevice owner)
    {
        return Helper.createPorts(StorageType.POWER, this, owner, owner.getLocation(), owner.getChannel());
    }
    
    public static class Helper
    {
        private static <T extends StorageType<T>> ImmutableList<Port<T>> createPorts(T storageType, IPortLayout layout, IDevice owner, BlockPos pos, int channel)
        {
            CarrierPortGroup<T> carrierGroup = null;
            
            ImmutableList.Builder<Port<T>> builder = ImmutableList.builder();
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                PortFace pf = layout.getFace(face);
                pf = pf.withDeviceChannel(owner.getChannel());
                
                for(PortDescription<?> pd : pf.ports(storageType))
                {
                    @SuppressWarnings("unchecked")
                    IPortDescription<T> desc = (IPortDescription<T>) pd;
                    
                    if(desc.function() == PortFunction.DIRECT)
                    {
                        builder.add(new DirectPortState<T>(owner, desc, pos, face));
                    }
                    else
                    {
                        if(carrierGroup == null)
                        {
                            carrierGroup = new CarrierPortGroup<T>(
                                    owner, desc.storageType(), desc.level());
                            carrierGroup.setCarrierChannel(desc.getChannel());
                        }
                        else
                        {
                            assert carrierGroup.level() == desc.level()
                                : "Mixed carrier port levels for same device";
                            
                            assert carrierGroup.getCarrierChannel() == desc.getChannel()
                                    : "Mixed carrier channels for same device";
                        }
                        builder.add(carrierGroup.createPort(
                                desc.function() == PortFunction.BRIDGE,
                                        desc.connector(), pos, face));   
                    }
                }
            }
            return builder.build();
        }
        
        private static ImmutableList<Port<StorageTypeFluid>> createFluidPorts(IPortLayout layout, IDevice owner, BlockPos pos)
        {
            Int2ObjectOpenHashMap<CarrierPortGroup<StorageTypeFluid>> carrierGroups = 
                    new Int2ObjectOpenHashMap<CarrierPortGroup<StorageTypeFluid>>();
            
            ImmutableList.Builder<Port<StorageTypeFluid>> builder = ImmutableList.builder();
            
            for(EnumFacing face : EnumFacing.VALUES)
            {
                PortFace pf = layout.getFace(face);
                
                for(PortDescription<?> pd : pf.ports(StorageType.FLUID))
                {
                    @SuppressWarnings("unchecked")
                    IPortDescription<StorageTypeFluid> desc = (IPortDescription<StorageTypeFluid>) pd;
                    
                    if(desc.function() == PortFunction.DIRECT)
                    {
                        builder.add(new DirectPortState<StorageTypeFluid>(owner, desc, pos, face));
                    }
                    else
                    {
                        CarrierPortGroup<StorageTypeFluid> group = carrierGroups.get(desc.getChannel());
                        if(group == null)
                        {
                            group = new CarrierPortGroup<StorageTypeFluid>(
                                    owner, StorageType.FLUID, desc.level());
                            group.setCarrierChannel(desc.getChannel());
                            carrierGroups.put(desc.getChannel(), group);
                        }
                        else
                        {
                            assert group.level() == desc.level()
                                : "Mixed carrier port levels for same device";
                            
                            assert group.getCarrierChannel() == desc.getChannel()
                                    : "Mixed carrier channels for same device";
                        }
                        builder.add(group.createPort(
                                desc.function() == PortFunction.BRIDGE,
                                        desc.connector(), pos, face));   
                    }
                }
            }
            return builder.build();
        }
    }
    

    /**
     * True if this layout has at least one port on the given
     * face that could connect with the opposite face on the
     * other layout provided.<p>
     * 
     * The default implementation in the interface is dumb and slow
     * and it will be called frequently client-side for cable rendering.
     * Implementations cache the results for this reason, and this
     * in turn is why we accept a dumb and slow default implementation.
     */
    public default boolean couldConnect(EnumFacing face, int fromChannel, IPortLayout otherLayout, int toChannel)
    {
        return this.getFace(face)
                .withDeviceChannel(fromChannel)
                .couldConnectWith(
                        otherLayout
                            .getFace(face.getOpposite())
                            .withDeviceChannel(toChannel));
    }

    /**
     * Name of this underlying port layout (with no localization) in forge registry.  
     */
    public ResourceLocation getRegistryName();

}