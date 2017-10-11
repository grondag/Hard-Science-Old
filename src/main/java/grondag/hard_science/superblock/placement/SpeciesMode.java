package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

public enum SpeciesMode implements IMessagePlusImmutable<SpeciesMode>, IReadWriteNBTImmutable<SpeciesMode>, ILocalized
{
    MATCH_CLICKED,
    COUNTER_CLICKED,
    MATCH_MOST,
    COUNTER_MOST;
    
    @Override
    public SpeciesMode deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_SPECIES_MODE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_SPECIES_MODE, this);
    }

    @Override
    public SpeciesMode fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(SpeciesMode.class);
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
        return I18n.translateToLocal("placement.species_mode." + this.name().toLowerCase());
    }
}
