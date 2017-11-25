package grondag.hard_science.virtualblock;

import java.util.HashMap;
import java.util.Map;

import grondag.hard_science.Log;
import grondag.hard_science.library.world.WorldMap;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderRefresh;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderUpdate;
import grondag.hard_science.simulator.base.DomainManager;
import grondag.hard_science.simulator.base.jobs.Job;
import grondag.hard_science.simulator.base.jobs.WorldTaskManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;

public class ExcavationRenderTracker extends WorldMap<Int2ObjectOpenHashMap<ExcavationRenderEntry>>
{
    /**
     * 
     */
    private static final long serialVersionUID = 3622879833810354529L;
    
    public static final ExcavationRenderTracker INSTANCE = new ExcavationRenderTracker();
    
    private static HashMap<EntityPlayerMP, PlayerData> playerTracking = new HashMap<EntityPlayerMP, PlayerData>();

    @Override
    protected Int2ObjectOpenHashMap<ExcavationRenderEntry> load(int dimension)
    {
        return new Int2ObjectOpenHashMap<ExcavationRenderEntry>();
    }
    
    public void add(Job job)
    {
        ExcavationRenderEntry entry = new ExcavationRenderEntry(job);
        Log.info("id = %d new Entry, valid=%s", entry.id, Boolean.toString(entry.isValid()));
        if(entry.isValid())
        {
            synchronized(this)
            {
                this.get(entry.dimensionID).put(entry.id, entry);
            }
            
            for(Map.Entry<EntityPlayerMP, PlayerData> playerEntry : playerTracking.entrySet())
            {
                
                Log.info("adding listeners for %s", playerEntry.getKey().getName());
                PlayerData pd = playerEntry.getValue();
                if(pd.dimensionID == entry.dimensionID && pd.domainID == entry.domainID)
                {
                    entry.addListener(playerEntry.getKey(), true);
                }
            }
        }
    }
    
    public synchronized void remove(ExcavationRenderEntry entry)
    {
        this.get(entry.dimensionID).remove(entry.id);
        
        PacketExcavationRenderUpdate packet = new PacketExcavationRenderUpdate(entry.id);
        
        for(Map.Entry<EntityPlayerMP, PlayerData> playerEntry : playerTracking.entrySet())
        {
            PlayerData pd = playerEntry.getValue();
            if(pd.dimensionID == entry.dimensionID && pd.domainID == entry.domainID)
            {
                entry.removeListener(playerEntry.getKey());
                WorldTaskManager.sendPacketFromServerThread(packet, playerEntry.getKey());
            }
        }
    }
    
    /**
     * Call when player joins server, changes dimension or changes active domain.
     */
    public void updatePlayerTracking(EntityPlayerMP player)
    {
        Log.info("updatePlayerTracking for %s", player.getName());
        
        PlayerData newData = new PlayerData(player);
        PlayerData oldData = playerTracking.get(player);
        
        if(oldData != null && oldData.dimensionID == newData.dimensionID && oldData.domainID == newData.domainID)
        {
            // no changes
            Log.info("updatePlayerTracking exit no changes");
            return;
        }
        
        playerTracking.put(player, newData);

        // remove old listeners if needed
        if(oldData != null)
        {
            synchronized(this)
            {
                Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.dimensionID);
                if(entries != null && !entries.isEmpty())
                {
                    for(ExcavationRenderEntry entry : entries.values())
                    {
                        if(entry.domainID == oldData.domainID) entry.removeListener(player);
                    }
                }
            }
        }
        
        // build refresh
        PacketExcavationRenderRefresh packet = new PacketExcavationRenderRefresh();
        synchronized(this)
        {
            Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(newData.dimensionID);
            if(entries != null && !entries.isEmpty())
            {
                for(ExcavationRenderEntry entry : entries.values())
                {
                    if(entry.domainID == newData.domainID)
                    {
                        entry.addListener(player, false);
                        if(entry.isFirstComputeDone()) packet.addRender(entry);
                    }
                }
            }
        }
        
        WorldTaskManager.sendPacketFromServerThread(packet, player);
    }
    
    public void stopPlayerTracking(EntityPlayerMP player)
    {
        PlayerData oldData = playerTracking.get(player);
        
        if(oldData == null) return;
        
        synchronized(this)
        {
            Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.dimensionID);
            if(entries != null && !entries.isEmpty())
            {
                for(ExcavationRenderEntry entry : entries.values())
                {
                    if(entry.domainID == oldData.domainID) entry.removeListener(player);
                }
            }
        }
    }
    
    private static class PlayerData
    {
        private final int domainID;
        private final int dimensionID;
        
        private PlayerData(EntityPlayerMP player)
        {
            this.domainID = DomainManager.INSTANCE.getActiveDomain(player).getId();
            this.dimensionID = player.dimension;
        }
    }
}
