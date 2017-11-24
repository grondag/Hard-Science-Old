package grondag.hard_science.network.server_to_client;

import java.util.ArrayList;

import grondag.hard_science.library.world.IntegerAABB;
import grondag.hard_science.network.AbstractServerToPlayerPacket;
import grondag.hard_science.virtualblock.ExcavationRenderEntry;
import grondag.hard_science.virtualblock.ExcavationRenderManager;
import grondag.hard_science.virtualblock.ExcavationRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Replaces all excavation render entries.  
 * Sent when players logs in, changes dimension or changes active domain.
 */
public class PacketExcavationRenderRefresh extends AbstractServerToPlayerPacket<PacketExcavationRenderRefresh>
{
    private ArrayList<RenderData> renders = new ArrayList<RenderData>();
    
    private static class RenderData
    {
        final int id;
        final IntegerAABB aabb;
        final boolean isExchange;
        
        private RenderData(ExcavationRenderEntry entry)
        {
            this(entry.id, entry.aabb(), entry.isExchange);
        }
        
        private RenderData(int id, IntegerAABB aabb, boolean isExchange)
        {
            this.id = id;
            this.aabb = aabb;
            this.isExchange = isExchange;
        }
    }

    public void addRender(ExcavationRenderEntry entry)
    {
        this.renders.add(new RenderData(entry));
    }
    
    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeInt(this.renders.size());
        for(RenderData r : this.renders)
        {
            pBuff.writeInt(r.id);
            pBuff.writeLong(r.aabb.minPos().toLong());
            pBuff.writeLong(r.aabb.maxPos().toLong());
            pBuff.writeBoolean(r.isExchange);
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        int count = pBuff.readInt();
        if(count > 0)
        {
            for(int i = 0; i < count; i++)
            {
                int id = pBuff.readInt();
                BlockPos minPos = BlockPos.fromLong(pBuff.readLong());
                BlockPos maxPos = BlockPos.fromLong(pBuff.readLong());
                boolean isExchange = pBuff.readBoolean();
                this.renders.add(new RenderData(id, new IntegerAABB(minPos, maxPos), isExchange));
            }
        }
    }

    @Override
    protected void handle(PacketExcavationRenderRefresh message, MessageContext context)
    {
        ExcavationRenderManager.clear();
        if(!message.renders.isEmpty())
        {
            ExcavationRenderer[] entries = new ExcavationRenderer[message.renders.size()];
            for(int i = 0; i < message.renders.size(); i++)
            {
                RenderData data = message.renders.get(i);
                entries[i] = new ExcavationRenderer(data.id, data.aabb.toAABB(), data.isExchange);
            }
            ExcavationRenderManager.addOrUpdate(entries);
        }
    }
}
