package grondag.hard_science.library.serialization;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * Combination of IMessagePlus and IReadWriteNBT 
 * with addition of deserialization methods that detect if state change occurred.
 */
public interface IFlexibleSerializer extends IMessagePlus, IReadWriteNBT
{
   
    //FIXME: I don't think the packet methods ever get used because of the 
    // fact that packet state has to be retrieved on a different thread.
    // And all the vanilla packets use NBT anyway.
    // IMessagePlus might be a waste of time elsewhere - should look.
    // Probably can remove.
    
        /** Same as {@link #fromBytes(ByteBuf)} but returns true if resulted in any state change. */
        public boolean fromBytesDetectChanges(PacketBuffer buf);
        
        /** Same as {@link #deserializeNBT(NBTTagCompound)} but returns true if resulted in any state change. */
        public boolean deserializeNBTDetectChanges(NBTTagCompound tag);
}
