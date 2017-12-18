package grondag.hard_science.superblock.virtual;

import java.util.HashMap;
import java.util.Map;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.world.WorldMap;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderRefresh;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderUpdate;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.storage.jobs.Job;
import grondag.hard_science.simulator.storage.jobs.WorldTaskManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;

public class ExcavationRenderTracker extends WorldMap<Int2ObjectOpenHashMap<ExcavationRenderEntry>>
{
    /**
     * 
     */
    private static final long serialVersionUID = 3622879833810354529L;
    
    public static final ExcavationRenderTracker INSTANCE = new ExcavationRenderTracker();
    
    private HashMap<String, PlayerData> playerTracking = new HashMap<String, PlayerData>();

    @Override
    protected Int2ObjectOpenHashMap<ExcavationRenderEntry> load(int dimension)
    {
        return new Int2ObjectOpenHashMap<ExcavationRenderEntry>();
    }
    
    public void add(Job job)
    {
        ExcavationRenderEntry entry = new ExcavationRenderEntry(job);
        if(Configurator.logExcavationRenderTracking) Log.info("id = %d new Entry, valid=%s", entry.id, Boolean.toString(entry.isValid()));
        if(entry.isValid())
        {
            synchronized(this)
            {
                this.get(entry.dimensionID).put(entry.id, entry);
            }
            
            for(Map.Entry<String, PlayerData> playerEntry : playerTracking.entrySet())
            {
                PlayerData pd = playerEntry.getValue();
                if(pd.dimensionID == entry.dimensionID && pd.domainID == entry.domainID)
                {
                    if(Configurator.logExcavationRenderTracking) Log.info("adding listeners for %s", playerEntry.getKey());
                    entry.addListener(pd.player, true);
                }
            }
        }
    }
    
    public synchronized void remove(ExcavationRenderEntry entry)
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id = %d removing excavation render entry", entry.id);
        
        this.get(entry.dimensionID).remove(entry.id);
        
        PacketExcavationRenderUpdate packet = new PacketExcavationRenderUpdate(entry.id);
        
        for(Map.Entry<String, PlayerData> playerEntry : playerTracking.entrySet())
        {
            PlayerData pd = playerEntry.getValue();
            if(pd.dimensionID == entry.dimensionID && pd.domainID == entry.domainID)
            {
                entry.removeListener(pd.player);
                WorldTaskManager.sendPacketFromServerThread(packet, pd.player, false);
            }
        }
    }
    
    /**
     * Call when player joins server, changes dimension or changes active domain.
     */
    public void updatePlayerTracking(EntityPlayerMP player)
    {
        if(Configurator.logExcavationRenderTracking) Log.info("updatePlayerTracking for %s", player.getName());
        
        PlayerData newData = new PlayerData(player);
        PlayerData oldData = playerTracking.get(player.getName());
        
        if(oldData != null && oldData.dimensionID == newData.dimensionID && oldData.domainID == newData.domainID)
        {
            // no changes
            if(Configurator.logExcavationRenderTracking) Log.info("updatePlayerTracking exit no changes");
            return;
        }
        
        playerTracking.put(player.getName(), newData);

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
        
        WorldTaskManager.sendPacketFromServerThread(packet, player, true);
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
        
        playerTracking.remove(player);
    }
    
    private static class PlayerData
    {
        private final int domainID;
        private final int dimensionID;
        private final EntityPlayerMP player;
        
        private PlayerData(EntityPlayerMP player)
        {
            this.domainID = DomainManager.instance().getActiveDomain(player).getId();
            this.dimensionID = player.dimension;
            this.player = player;
        }
    }
    
    @Override
    public void clear()
    {
        super.clear();
        playerTracking.clear();
    }
}
