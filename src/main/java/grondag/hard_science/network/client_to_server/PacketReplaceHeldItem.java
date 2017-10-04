package grondag.hard_science.network.client_to_server;


import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import java.io.IOException;

import grondag.hard_science.network.AbstractPlayerToServerPacket;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 * FIXME: security
 */
public class PacketReplaceHeldItem extends AbstractPlayerToServerPacket<PacketReplaceHeldItem>
{
    
    private ItemStack stack;

    public PacketReplaceHeldItem() 
    {
    }
    
    public PacketReplaceHeldItem(ItemStack stack) 
    {
        this.stack = stack;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) 
    {
        try
        {
            this.stack = pBuff.readItemStack();
        }
        catch (IOException e)
        {
            this.stack = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff) 
    {
        pBuff.writeItemStack(this.stack);
    }
   
    @Override
    protected void handle(PacketReplaceHeldItem message, EntityPlayerMP player)
    {
        player.setHeldItem(EnumHand.MAIN_HAND, message.stack);
    }
}
