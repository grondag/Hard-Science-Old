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
 * Determines how blocks are to be selected for operation of the placement item.
 */
public enum SelectionMode implements IMessagePlusImmutable<SelectionMode>, IReadWriteNBTImmutable<SelectionMode>, ILocalized
{
    /** affect a single block - normal MC  behavior*/
    SINGLE_BLOCK(false),
    
    /** use the placement item's selection region */
    REGION(true),
    
    /** use only the exterior blocks the placement item's selection region*/
    HOLLOW_REGION(true),
    
    /** search for and affect blocks that match the clicked block */
    MATCH_CLICKED(false);
    
    /**
     * If true, this mode uses the geometrically-defined volume defined by the placement item's current selection region.
     * If false, affects a single block or employs some other logic for determining what blocks are affected.
     */
    public final boolean usesSelectionRegion;
    
    private SelectionMode(boolean usesSelectionRegion)
    {
        this.usesSelectionRegion = usesSelectionRegion;
    }
    
    @Override
    public SelectionMode deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, ModNBTTag.SELECTION_MODE, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, ModNBTTag.SELECTION_MODE, this);
    }

    @Override
    public SelectionMode fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(SelectionMode.class);
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
        return I18n.translateToLocal("placement.selection_mode." + this.name().toLowerCase());
    }
}
