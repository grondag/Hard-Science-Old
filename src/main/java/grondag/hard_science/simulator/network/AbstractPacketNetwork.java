package grondag.hard_science.simulator.network;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Base class for network simulation.
 * 
 * Networks transfer a packet of some type.
 * 
 */
public abstract class AbstractPacketNetwork<T>
{
    private static final String NBT_TAG_PACKET_NETWORK_ID = "PNID";
    
    public final int id;
    
    protected abstract void handleNBTRead(NBTTagCompound tag);
    protected abstract void handleNBTWrite(NBTTagCompound tag);

    protected AbstractPacketNetwork(NBTTagCompound tag)
    {
        int idIn = 0;
        if(tag != null)
        {
            idIn = tag.getInteger(NBT_TAG_PACKET_NETWORK_ID);
        }
        id = idIn != 0 ? idIn : AssignedNumbersAuthority.INSTANCE.generateNewNumber(AssignedNumber.NETWORK);
        this.handleNBTRead(tag);
    }
    
    public final void writeNBT(NBTTagCompound tag)
    {
        tag.setInteger(NBT_TAG_PACKET_NETWORK_ID, this.id);
        this.handleNBTWrite(tag);
    }
    
}
