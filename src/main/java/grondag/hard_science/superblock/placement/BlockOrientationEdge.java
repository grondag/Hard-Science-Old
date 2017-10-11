package grondag.hard_science.superblock.placement;

import grondag.hard_science.library.serialization.IMessagePlusImmutable;
import grondag.hard_science.library.serialization.IReadWriteNBTImmutable;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.library.varia.ILocalized;
import grondag.hard_science.library.varia.Useful;
import grondag.hard_science.library.world.BlockCorner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

public enum BlockOrientationEdge implements IMessagePlusImmutable<BlockOrientationEdge>, IReadWriteNBTImmutable<BlockOrientationEdge>, ILocalized
{
    DYNAMIC(null),
    MATCH_CLOSEST(null),
    UP_EAST(BlockCorner.UP_EAST),
    UP_WEST(BlockCorner.UP_WEST),
    UP_NORTH(BlockCorner.UP_NORTH),
    UP_SOUTH(BlockCorner.UP_SOUTH),
    NORTH_EAST(BlockCorner.NORTH_EAST),
    NORTH_WEST(BlockCorner.NORTH_WEST),
    SOUTH_EAST(BlockCorner.SOUTH_EAST),
    SOUTH_WEST(BlockCorner.SOUTH_WEST),
    DOWN_EAST(BlockCorner.DOWN_EAST),
    DOWN_WEST(BlockCorner.DOWN_WEST),
    DOWN_NORTH(BlockCorner.DOWN_NORTH),
    DOWN_SOUTH(BlockCorner.DOWN_SOUTH);
    
    public final BlockCorner edge;
    
    private BlockOrientationEdge(BlockCorner edge)
    {
        this.edge= edge;
    }

    @Override
    public BlockOrientationEdge deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_EDGE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.PLACEMENT_ORIENTATION_EDGE, this);
    }

    @Override
    public BlockOrientationEdge fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(BlockOrientationEdge.class);
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
        return I18n.translateToLocal("placement.orientation.edge." + this.name().toLowerCase());
    }
    
    public boolean isFixed()
    {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
