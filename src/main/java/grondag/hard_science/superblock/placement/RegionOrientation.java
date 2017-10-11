package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

public enum RegionOrientation implements IMessagePlusImmutable<RegionOrientation>, IReadWriteNBTImmutable<RegionOrientation>, ILocalized
{
    XYZ,
    ZYX,
    ZXY,
    XZY,
    YXZ,
    YZX;
    
    @Override
    public RegionOrientation deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_REGION_ORIENTATION, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_REGION_ORIENTATION, this);
    }

    @Override
    public RegionOrientation fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(RegionOrientation.class);
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
        return I18n.translateToLocal("placement.orientation.region." + this.name().toLowerCase());
    }
    
    public BlockPos rotatedRegionPos(BlockPos fromPos)
    {
        switch(this)
        {
        case XYZ:
        default:
            return fromPos;
            
        case XZY:
            return new BlockPos(fromPos.getX(), fromPos.getZ(), fromPos.getY());

        case YXZ:
            return new BlockPos(fromPos.getY(), fromPos.getX(), fromPos.getZ());

        case YZX:
            return new BlockPos(fromPos.getY(), fromPos.getZ(), fromPos.getX());

        case ZXY:
            return new BlockPos(fromPos.getZ(), fromPos.getX(), fromPos.getY());

        case ZYX:
            return new BlockPos(fromPos.getZ(), fromPos.getY(), fromPos.getX());
        
        }
    }
}
