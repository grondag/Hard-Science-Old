package grondag.hard_science.network.server_to_client;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.network.AbstractServerToPlayerPacket;
import grondag.exotic_matter.world.IntegerAABB;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.superblock.virtual.ExcavationRenderEntry;
import grondag.hard_science.superblock.virtual.ExcavationRenderManager;
import grondag.hard_science.superblock.virtual.ExcavationRenderer;
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
        final BlockPos[] positions;
        
        private RenderData(ExcavationRenderEntry entry)
        {
            this(entry.id, entry.aabb(), entry.isExchange, entry.renderPositions());
        }
        
        private RenderData(int id, @Nonnull IntegerAABB aabb, boolean isExchange, @Nullable BlockPos[] positions)
        {
            this.id = id;
            this.aabb = aabb;
            this.isExchange = isExchange;
            this.positions = positions;
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
            pBuff.writeInt(r.positions == null ? 0 : r.positions.length);
            if(r.positions != null)
            {
                if(Configurator.logExcavationRenderTracking) HardScience.INSTANCE.info("id %d Refresh toBytes position count = %d", r.id, r.positions == null ? 0 : r.positions.length);

                for(BlockPos pos : r.positions)
                {
                    pBuff.writeLong(pos.toLong());
                }
            }
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
                int positionCount = pBuff.readInt();
                BlockPos[] list;
                if(positionCount == 0)
                {
                    list = null;
                }
                else
                {
                    list = new BlockPos[positionCount];
                    for(int j = 0; j < positionCount; j++)
                    {
                        list[j] = BlockPos.fromLong(pBuff.readLong());
                    }
                    
                }
                if(Configurator.logExcavationRenderTracking) HardScience.INSTANCE.info("id %d Refresh toBytes position count = %d", id, list == null ? 0 : list.length);

                this.renders.add(new RenderData(id, new IntegerAABB(minPos, maxPos), isExchange, list));
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
                entries[i] = new ExcavationRenderer(data.id, data.aabb.toAABB(), data.isExchange, data.positions);
            }
            ExcavationRenderManager.addOrUpdate(entries);
        }
    }
}
