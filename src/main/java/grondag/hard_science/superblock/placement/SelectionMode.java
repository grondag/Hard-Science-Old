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
    ON_CLICKED_FACE(false),
    
    /** use the placement item's selection region */
    FILL_REGION(true),
    
    /** use only the exterior blocks the placement item's selection region*/
    HOLLOW_REGION(true),
    
    /** use the placement item's selection region ONLY if all blocks in region match the filter criteria */
    COMPLETE_REGION(true),
    
    /** flood fill search for blocks that match the clicked block - like an exchanger */
    MATCH_CLICKED_BLOCK(false),
    
    /** flood fill of adjacent surfaces that match the clicked block - like a builder's wand */
    ON_CLICKED_SURFACE(false);
    
    /**
     * If true, this mode uses the geometrically-defined volume defined by the placement item's current selection region.
     * By extension, also determines if the current filter mode applies.
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
