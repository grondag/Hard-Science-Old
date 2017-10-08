package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

/** 
 * Determines how placed blocks are oriented (orthogonalAxis, rotation)
 */
public enum PlacementMode implements IMessagePlusImmutable<PlacementMode>, IReadWriteNBTImmutable<PlacementMode>, ILocalized
{
    /** place selected block */
    SINGLE_BLOCK,
    
    /** fill selected AABB*/
    FILL_REGION,
    
    /** like a typical MC builder's wand */
    ADD_TO_EXISTING;
    
    @Override
    public PlacementMode deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_MODE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_MODE, this);
    }

    @Override
    public PlacementMode fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(PlacementMode.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String localizedName()
    {
        return I18n.translateToLocal("placement.mode." + this.name().toLowerCase());
    }
}
