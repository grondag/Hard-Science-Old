package grondag.hard_science.library.serialization;

import net.minecraft.network.PacketBuffer;

/**
 * Packet read/write interface for classes with immutable values
 */
public interface IMessagePlusImmutable<T>
{
    public abstract T fromBytes(PacketBuffer buf);
    
    public abstract void toBytes(PacketBuffer pBuff);

}
