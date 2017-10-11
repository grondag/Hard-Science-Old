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

public enum BlockOrientationFace implements IMessagePlusImmutable<BlockOrientationFace>, IReadWriteNBTImmutable<BlockOrientationFace>, ILocalized
{
    DYNAMIC(null),
    MATCH_CLOSEST(null),
    UP(EnumFacing.UP),
    DOWN(EnumFacing.DOWN),
    NORTH(EnumFacing.NORTH),
    EAST(EnumFacing.EAST),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST);
    
    public final EnumFacing face;
    
    private BlockOrientationFace(EnumFacing face)
    {
        this.face = face;
    }
    
    @Override
    public BlockOrientationFace deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_FACE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_FACE, this);
    }

    @Override
    public BlockOrientationFace fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(BlockOrientationFace.class);
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
        return I18n.translateToLocal("placement.orientation.face." + this.name().toLowerCase());
    }
    
    public boolean isFixed()
    {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
