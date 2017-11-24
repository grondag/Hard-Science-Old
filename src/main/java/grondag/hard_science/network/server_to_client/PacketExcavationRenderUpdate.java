package grondag.hard_science.network.server_to_client;

import grondag.hard_science.library.world.IntegerAABB;
import grondag.hard_science.network.AbstractServerToPlayerPacket;
import grondag.hard_science.virtualblock.ExcavationRenderEntry;
import grondag.hard_science.virtualblock.ExcavationRenderManager;
import grondag.hard_science.virtualblock.ExcavationRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Adds, changes or removes an excavation render entry in current client list.  
 * Sent when a new excavation is added, the bounds change, or excavation is removed.
 */
public class PacketExcavationRenderUpdate extends AbstractServerToPlayerPacket<PacketExcavationRenderUpdate>
{
    private int id;
    private IntegerAABB aabb;
    private boolean isExchange;
    
    public PacketExcavationRenderUpdate()
    { 
        super();
    }

    /**
     * Use this constructor for new and changed.
     */
    public PacketExcavationRenderUpdate(ExcavationRenderEntry entry)
    {
        super();
        this.id = entry.id;
        this.aabb = entry.aabb();
        this.isExchange = entry.isExchange;
    }
        
    /**
     * Use this constructor for deleted.
     */
    public PacketExcavationRenderUpdate(int deletedID)
    {
        this.id = deletedID;
        this.aabb = null;
    }
    
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeInt(this.id);
        // deletion flag
        pBuff.writeBoolean(this.aabb == null);
        if(this.aabb != null)
        {
            pBuff.writeLong(this.aabb.minPos().toLong());
            pBuff.writeLong(this.aabb.maxPos().toLong());
            pBuff.writeBoolean(this.isExchange);
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.id = pBuff.readInt();
        if(pBuff.readBoolean())
        {
            // deletion
            this.aabb = null;
        }
        else
        {
            BlockPos minPos = BlockPos.fromLong(pBuff.readLong());
            BlockPos maxPos = BlockPos.fromLong(pBuff.readLong());
            this.aabb = new IntegerAABB(minPos, maxPos);
            this.isExchange = pBuff.readBoolean();
        }
    }

    @Override
    protected void handle(PacketExcavationRenderUpdate message, MessageContext context)
    {
        ExcavationRenderManager.clear();
        if(message.aabb == null)
        {
            ExcavationRenderManager.remove(message.id);
        }
        else
        {
            ExcavationRenderManager.addOrUpdate(new ExcavationRenderer(message.id, message.aabb.toAABB(), message.isExchange));
        }
    }
}
