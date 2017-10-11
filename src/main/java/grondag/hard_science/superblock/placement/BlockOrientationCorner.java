package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.FarCorner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

public enum BlockOrientationCorner implements IMessagePlusImmutable<BlockOrientationCorner>, IReadWriteNBTImmutable<BlockOrientationCorner>, ILocalized
{
    DYNAMIC(null),
    MATCH_CLOSEST(null),
    UP_NORTH_EAST(FarCorner.UP_NORTH_EAST),
    UP_NORTH_WEST(FarCorner.UP_NORTH_WEST),
    UP_SOUTH_EAST(FarCorner.UP_SOUTH_EAST),
    UP_SOUTH_WEST(FarCorner.UP_SOUTH_WEST),
    DOWN_NORTH_EAST(FarCorner.DOWN_NORTH_EAST),
    DOWN_NORTH_WEST(FarCorner.DOWN_NORTH_WEST),
    DOWN_SOUTH_EAST(FarCorner.DOWN_SOUTH_EAST),
    DOWN_SOUTH_WEST(FarCorner.DOWN_SOUTH_WEST);
    
    public final FarCorner corner;
    
    private BlockOrientationCorner(FarCorner corner)
    {
        this.corner = corner;
    }
    
    @Override
    public BlockOrientationCorner deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_CORNER, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_CORNER, this);
    }

    @Override
    public BlockOrientationCorner fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(BlockOrientationCorner.class);
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
        return I18n.translateToLocal("placement.orientation.corner." + this.name().toLowerCase());
    }
    
    public boolean isFixed()
    {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
