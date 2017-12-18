package grondag.hard_science.simulator.persistence;

import net.minecraft.world.storage.MapStorage;

public class PersistenceManager 
{
    /** use to persist new nodes */
    public static void registerNode(MapStorage mapStore, IPersistenceNode node)
    {
        PersistenceDelegate instance = new PersistenceDelegate(node);
        mapStore.setData(instance.mapName, instance);
    }
    
    /** 
     * Use to load nodes persisted earlier.
     * Returns true if node checked and loaded, false otherwise.
     */
    public static boolean loadNode(MapStorage mapStore, IPersistenceNode node)
    {
        PersistenceDelegate instance = (PersistenceDelegate) mapStore.getOrLoadData(PersistenceDelegate.class, node.tagName());
        if(instance == null) return false;
        instance.setNode(node);
        instance.readCachedNBT();
        return true;
    }
    

}
