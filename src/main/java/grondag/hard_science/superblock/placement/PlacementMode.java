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
 * Determines operation to be performed on selected blocks
 */
public enum PlacementMode implements IMessagePlusImmutable<PlacementMode>, IReadWriteNBTImmutable<PlacementMode>, ILocalized
{
    ADJUST_AND_SKIP(true, true),
    ADJUST_AND_DISALLOW(false, true),
    SKIP(true, false),
    DISALLOW(false, false);
    /**
     * FIXME: add
     * REPLACE_EXISTING
     * EXCAVATE
     * IGNORE_EXISTING
     */
    
    public final boolean skip;
    
    /**
     * True if this type of placement should use selection region adjustment if that feature is enabled. 
     * (Is controlled by RegionOrientation.)
     */
    public final boolean adjustIfEnabled;
    
    private PlacementMode(boolean skip, boolean adjustIfEnabled)
    {
        this.skip = skip;
        this.adjustIfEnabled = adjustIfEnabled;
    }
    @Override
    public PlacementMode deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_OBSTACLE_MODE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_OBSTACLE_MODE, this);
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
        return I18n.translateToLocal("placement.obdstacle_mode." + this.name().toLowerCase());
    }
}
