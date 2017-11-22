package grondag.hard_science.simulator.persistence;

import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

public class PersistenceManager 
{
    /** use to persist new nodes */
    public static void registerNode(World world, IPersistenceNode node)
    {
        PersistenceDelegate instance = new PersistenceDelegate(node);
        world.getMapStorage().setData(instance.mapName, instance);
    }
    
    /** 
     * Use to load nodes persisted earlier.
     * Returns true if node checked and loaded, false otherwise.
     */
    public static boolean loadNode(World world, IPersistenceNode node)
    {
        MapStorage storage = world.getMapStorage();
        PersistenceDelegate instance = (PersistenceDelegate) storage.getOrLoadData(PersistenceDelegate.class, node.tagName());
        if(instance == null) return false;
        instance.setNode(node);
        instance.readCachedNBT();
        return true;
    }
    

}
