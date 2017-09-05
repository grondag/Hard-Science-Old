package grondag.hard_science.library.serialization;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSerializer<T> implements ISerializer<T>
{
    /**
     * Anything stored in this tag will not be sent to clients.
     */
    public static final String NBT_SERVER_SIDE_TAG = "SrvData";
    
    /** Returns server-side tag if one is present, creating it if not. */
    public static @Nonnull NBTTagCompound getServerTag(@Nonnull NBTTagCompound fromTag)
    {
        NBTBase result = fromTag.getTag(NBT_SERVER_SIDE_TAG);
        if(result == null || result.getId() != 10)
        {
            result = new NBTTagCompound();
            fromTag.setTag(NBT_SERVER_SIDE_TAG, result);
        }
        return (NBTTagCompound) result;
    }
    
    /** Returns tag stripped of server-side tag if it is present. 
     * If the tag must be stripped, returns a modified copy. Otherwise returns input tag.
     * Will return null if a null tag is passed in.
     */
    public static NBTTagCompound withoutServerTag(NBTTagCompound inputTag)
    {
        if(inputTag != null && inputTag.hasKey(NBT_SERVER_SIDE_TAG))
        {
            inputTag = inputTag.copy();
            inputTag.removeTag(NBT_SERVER_SIDE_TAG);
        }
        return inputTag;
    }

    public final boolean isServerSideOnly;

    public AbstractSerializer(boolean isServerSideOnly)
    {
        this.isServerSideOnly = isServerSideOnly;
    }
    
    /**
     * Returns server-side tag if this instance should use one, creating it if necessary.
     * Otherwise returns input tag.
     */
    protected final @Nonnull NBTTagCompound getTargetTag(@Nonnull NBTTagCompound fromTag)
    {
        return this.isServerSideOnly ? getServerTag(fromTag) : fromTag;
    }
}
