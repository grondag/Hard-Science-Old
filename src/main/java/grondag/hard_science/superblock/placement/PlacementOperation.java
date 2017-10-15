package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 * Type of placement operations that have duration.
 * Is essentially the "mode" of a placement item.
 */
public enum PlacementOperation implements IMessagePlusImmutable<PlacementOperation>, IReadWriteNBTImmutable<PlacementOperation>
{
    NONE,
    SELECTING;


    @Override
    public PlacementOperation deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_OPERATION_IN_PROGRESS, this);
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_OPERATION_IN_PROGRESS, this);
    }
    
    @Override
    public PlacementOperation fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(PlacementOperation.class);
    }
    
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this);
    }
}