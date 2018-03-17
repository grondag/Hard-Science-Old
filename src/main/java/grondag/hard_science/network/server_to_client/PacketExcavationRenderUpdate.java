package grondag.hard_science.network.server_to_client;

import grondag.exotic_matter.world.IntegerAABB;
import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.network.AbstractServerToPlayerPacket;
import grondag.hard_science.superblock.virtual.ExcavationRenderEntry;
import grondag.hard_science.superblock.virtual.ExcavationRenderManager;
import grondag.hard_science.superblock.virtual.ExcavationRenderer;
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
    private BlockPos[] positions;
    
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
        this.positions = entry.renderPositions();
        if(Configurator.logExcavationRenderTracking) Log.info("id %d New update packet position count = %d, aabb=%s", this.id, this.positions == null ? 0 : this.positions.length, this.aabb == null ? "null" : this.aabb.toString());
    }
        
    /**
     * Use this constructor for deleted.
     */
    public PacketExcavationRenderUpdate(int deletedID)
    {
        this.id = deletedID;
        this.aabb = null;
        this.positions = null;
    }
    
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id %d Update toBytes position count = %d", this.id, this.positions == null ? 0 : this.positions.length);

        pBuff.writeInt(this.id);
        // deletion flag
        pBuff.writeBoolean(this.aabb == null);
        if(this.aabb != null)
        {
            pBuff.writeLong(this.aabb.minPos().toLong());
            pBuff.writeLong(this.aabb.maxPos().toLong());
            pBuff.writeBoolean(this.isExchange);
            
            pBuff.writeInt(this.positions == null ? 0 : this.positions.length);
            if(this.positions != null)
            {
                for(BlockPos pos : this.positions)
                {
                    pBuff.writeLong(pos.toLong());
                }
            }
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
            this.positions = null;
        }
        else
        {
            BlockPos minPos = BlockPos.fromLong(pBuff.readLong());
            BlockPos maxPos = BlockPos.fromLong(pBuff.readLong());
            this.aabb = new IntegerAABB(minPos, maxPos);
            this.isExchange = pBuff.readBoolean();
            
            int posCount = pBuff.readInt();
            if(posCount == 0)
            {
                this.positions = null;
            }
            else
            {
                this.positions = new BlockPos[posCount];
                for(int i = 0; i < posCount; i++)
                {
                    this.positions[i] = BlockPos.fromLong(pBuff.readLong());
                }
            }
        }
        if(Configurator.logExcavationRenderTracking) Log.info("id %d Update fromBytes position count = %d", this.id, this.positions == null ? 0 : this.positions.length);

    }

    @Override
    protected void handle(PacketExcavationRenderUpdate message, MessageContext context)
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id %d Update handler position count = %d, aabb=%s", message.id, message.positions == null ? 0 : message.positions.length, message.aabb == null ? "null" : message.aabb.toString());

        if(message.aabb == null)
        {
            ExcavationRenderManager.remove(message.id);
        }
        else
        {
            ExcavationRenderManager.addOrUpdate(new ExcavationRenderer(message.id, message.aabb.toAABB(), message.isExchange, message.positions));
        }
    }
}
