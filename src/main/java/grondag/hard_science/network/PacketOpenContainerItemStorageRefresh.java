package grondag.hard_science.network;


import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import grondag.hard_science.simulator.wip.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.wip.ItemResourceWithQuantity;
import grondag.hard_science.simulator.wip.OpenContainerStorageProxy;
import grondag.hard_science.simulator.wip.StorageType;

/**
 * Sent by OpenContainerStorageListner to synch user inventory display for IStorage
 */
public class PacketOpenContainerItemStorageRefresh implements IMessage
{
    
    private List<AbstractResourceWithQuantity<StorageType.StorageTypeStack>> items;

    public PacketOpenContainerItemStorageRefresh() 
    {
    }
    
    public List<AbstractResourceWithQuantity<StorageType.StorageTypeStack>> items() { return this.items; };
    
    public PacketOpenContainerItemStorageRefresh(List<AbstractResourceWithQuantity<StorageType.StorageTypeStack>> items) 
    {
        this.items = items;
    }

    @Override
    public void fromBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
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
    public void toBytes(ByteBuf buf) 
    {
        PacketBuffer pBuff = new PacketBuffer(buf);
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

    public static class Handler implements IMessageHandler<PacketOpenContainerItemStorageRefresh, IMessage> 
    {
        @Override
        public IMessage onMessage(PacketOpenContainerItemStorageRefresh message, MessageContext ctx) 
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketOpenContainerItemStorageRefresh message, MessageContext ctx) 
        {
            OpenContainerStorageProxy.ITEM_PROXY.handleStorageRefresh(null, message.items);
        }
    }
}
