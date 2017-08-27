package grondag.hard_science.network.server_to_client;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import grondag.hard_science.network.AbstractServerToPlayerPacket;
import grondag.hard_science.simulator.wip.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.wip.ItemResourceWithQuantity;
import grondag.hard_science.simulator.wip.OpenContainerStorageProxy;
import grondag.hard_science.simulator.wip.StorageType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent by OpenContainerStorageListner to synch user inventory display for IStorage
 */
public class PacketOpenContainerItemStorageRefresh extends AbstractServerToPlayerPacket<PacketOpenContainerItemStorageRefresh>
{
    
    private List<AbstractResourceWithQuantity<StorageType.StorageTypeStack>> items;
    private long capacity;
    
    public List<AbstractResourceWithQuantity<StorageType.StorageTypeStack>> items() { return this.items; };
    
    public PacketOpenContainerItemStorageRefresh() {};
    
    public PacketOpenContainerItemStorageRefresh(List<AbstractResourceWithQuantity<StorageType.StorageTypeStack>> items, long capacity) 
    {
        this.items = items;
        this.capacity = capacity;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        this.capacity = pBuff.readLong();
        int count = pBuff.readInt();
        if(count == 0)
        {
            this.items = Collections.emptyList();
        }
        else
        {
            this.items = new ArrayList<AbstractResourceWithQuantity<StorageType.StorageTypeStack>>(count);
            for(int i = 0; i < count; i++)
            {
                ItemResourceWithQuantity item = new ItemResourceWithQuantity();
                item.fromBytes(pBuff);
                this.items.add(item);
            }
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeLong(this.capacity);
        int count = items.size();
        pBuff.writeInt(count);
        if(count > 0)
        {
            for(int i = 0; i < count; i++)
            {
                this.items.get(i).toBytes(pBuff);
            }
        }
    }

    @Override
    public void handle(PacketOpenContainerItemStorageRefresh message, MessageContext ctx) 
    {
        OpenContainerStorageProxy.ITEM_PROXY.handleStorageRefresh(null, message.items, message.capacity);
    }
}