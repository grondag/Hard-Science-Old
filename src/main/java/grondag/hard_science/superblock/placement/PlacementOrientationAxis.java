package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;

public enum PlacementOrientationAxis implements IMessagePlusImmutable<PlacementOrientationAxis>, IReadWriteNBTImmutable<PlacementOrientationAxis>, ILocalized
{
    DYNAMIC(null),
    MATCH_CLOSEST(null),
    X(EnumFacing.Axis.X),
    Y(EnumFacing.Axis.Y),
    Z(EnumFacing.Axis.Z);
    
    public final EnumFacing.Axis axis;
    
    private PlacementOrientationAxis(EnumFacing.Axis axis)
    {
        this.axis = axis;
    }
    
    @Override
    public PlacementOrientationAxis deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_AXIS, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_AXIS, this);
    }

    @Override
    public PlacementOrientationAxis fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(PlacementOrientationAxis.class);
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
        return I18n.translateToLocal("placement.orientation.axis." + this.name().toLowerCase());
    }
    
    public boolean isFixed()
    {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
