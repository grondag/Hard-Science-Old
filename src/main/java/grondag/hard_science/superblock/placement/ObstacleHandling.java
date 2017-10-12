package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

public enum ObstacleHandling implements IMessagePlusImmutable<ObstacleHandling>, IReadWriteNBTImmutable<ObstacleHandling>, ILocalized
{
    SKIP,
    DISALLOW,
    ADJUST;
    
    @Override
    public ObstacleHandling deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_OBSTACLE_MODE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_OBSTACLE_MODE, this);
    }

    @Override
    public ObstacleHandling fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(ObstacleHandling.class);
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
        return I18n.translateToLocal("placement.obdstacle_mode." + this.name().toLowerCase());
    }
}
