package grondag.hard_science.library.serialization;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * Convenience combination of IMessagePlus and IReadWriteNBT 
 */
public interface IMultiSerializable extends IMessagePlus, IReadWriteNBT
{
    /**
    * with addition of deserialization methods that detect if state change occurred.
    */
    public static interface IMultiSerializableNotifying extends IMultiSerializable
    {
        /** Same as {@link #fromBytes(ByteBuf)} but returns true if resulted in any state change. */
        public boolean fromBytesDetectChanges(PacketBuffer buf);
        
        /** Same as {@link #deserializeNBT(NBTTagCompound)} but returns true if resulted in any state change. */
        public boolean deserializeNBTDetectChanges(NBTTagCompound tag);
    }
}
