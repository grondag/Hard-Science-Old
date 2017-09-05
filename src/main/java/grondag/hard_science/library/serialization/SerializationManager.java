package grondag.hard_science.library.serialization;

import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class SerializationManager<T> extends SimpleUnorderedArrayList<AbstractSerializer<T>> implements ISerializer<T>
{
    /**
     * Always excludes server-only handlers.
     */
    @Override
    public void fromBytes(T target, PacketBuffer pBuff)
    {
        if(this.isEmpty()) return;
        for(int i = 0; i < this.size; i++)
        {
            AbstractSerializer<T> handler = this.get(i);
            if(!handler.isServerSideOnly) handler.fromBytes(target, pBuff);
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
            AbstractSerializer<T> handler = this.get(i);
            if(!handler.isServerSideOnly) handler.toBytes(target, pBuff);
        } 
    }

    /**
     * Will skip server-only members if no server tag is present
     * to avoid putting in bad values from read.
     */
    @Override
    public void deserializeNBT(T target, NBTTagCompound tag)
    {
        if(this.isEmpty()) return;

        boolean hasServerTag = tag.hasKey(AbstractSerializer.NBT_SERVER_SIDE_TAG);
        
        for(int i = 0; i < this.size; i++)
        {
            AbstractSerializer<T> handler = this.get(i);
            // if no server tag, skip server-only members
            // getTargetTag routes to the base or server-side tag as appropriate
            if(hasServerTag || !handler.isServerSideOnly) handler.deserializeNBT(target, tag);
        } 
        
    }

    @Override
    public void serializeNBT(T target, NBTTagCompound tag)
    {
        if(this.isEmpty()) return;
        for(int i = 0; i < this.size; i++)
        {
            AbstractSerializer<T> handler = this.get(i);
            handler.serializeNBT(target, tag);
        } 
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
