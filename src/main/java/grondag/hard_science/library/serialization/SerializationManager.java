package grondag.hard_science.library.serialization;

import javax.annotation.Nonnull;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class SerializationManager<T> extends SimpleUnorderedArrayList<AbstractSerializer<T>> implements ISerializer<T>
{
    
    /**
     * Anything stored in this tag will not be sent to clients.
     */
    public static final String NBT_SERVER_SIDE_TAG = "SrvData";
    
    /** Returns server-side tag if one is present, creating it if not. */
    private static @Nonnull NBTTagCompound getServerTag(@Nonnull NBTTagCompound fromTag)
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
    
    /**
     * Always excludes server-only handlers.
     */
    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    {
        if(this.isEmpty()) return;
        for(int i = 0; i < this.size; i++)
        {
            AbstractSerializer<T> serializer = this.get(i);
            if(!serializer.isServerSideOnly) serializer.fromBytes(target, pBuff);
        }        
    }

    /**
     * Always excludes server-only handlers.
     */
    @Override
    public void toBytes(T target, PacketBuffer pBuff)
    {
        if(this.isEmpty()) return;
        for(int i = 0; i < this.size; i++)
        {
            AbstractSerializer<T> serializer = this.get(i);
            if(!serializer.isServerSideOnly) serializer.toBytes(target, pBuff);
        } 
    }

    /**
     * Will skip server-only serializers if no server tag is present
     * to avoid putting in bad values from read.
     */
    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        if(this.isEmpty()) return;

        if(tag.hasKey(NBT_SERVER_SIDE_TAG))
        {
            NBTTagCompound serverTag = getServerTag(tag);
            for(int i = 0; i < this.size; i++)
            {
                AbstractSerializer<T> serializer = this.get(i);
                serializer.deserializeNBT(target, serializer.isServerSideOnly ? serverTag : tag);
            }
        }
        else
        {
            for(int i = 0; i < this.size; i++)
            {
                AbstractSerializer<T> serializer = this.get(i);
                if(!serializer.isServerSideOnly)
                {
                    serializer.deserializeNBT(target, tag);
                }
            }
        }
        
    }
    
    /**
     * All server-side only serializers will be segregated to a separate
     * sub tag that can be removed via {@link #withoutServerTag(NBTTagCompound)}.
     * The sub tag is not created if there are no server-side only serializers.
     */
    @Override
    public void serializeNBT(T target, NBTTagCompound tag)
    {
        if(this.isEmpty()) return;
        NBTTagCompound serverTag = getServerTag(tag);
        
        for(int i = 0; i < this.size; i++)
        {
            AbstractSerializer<T> serializer = this.get(i);
            serializer.serializeNBT(target, serializer.isServerSideOnly ? serverTag : tag);
        } 
        
        //don't emit empty server-side tags
        if(serverTag.hasNoTags()) tag.removeTag(NBT_SERVER_SIDE_TAG);
    }

    /** 
     * Convenience method for static initializers
     */
    public SerializationManager<T> addThen(AbstractSerializer<T> handler)
    {
        this.add(handler);
        return this;
    }
}
