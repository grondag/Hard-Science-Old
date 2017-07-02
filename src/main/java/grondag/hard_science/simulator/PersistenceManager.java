package grondag.hard_science.simulator;

import grondag.hard_science.HardScience;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

public class PersistenceManager 
{
    /** use to persist new nodes */
    public static void registerNode(World world, SimulationNode node)
    {
        PersistenceDelegate instance = new PersistenceDelegate(HardScience.MODID + node.getID());
        instance.setNode(node);
        world.getMapStorage().setData(instance.mapName, instance);
    }
    
    /** 
     * Use to load nodes persisted earlier.
     * Returns true if node found and loaded, false otherwise.
     */
    public static boolean loadNode(World world, SimulationNode node)
    {
        MapStorage storage = world.getMapStorage();
        PersistenceDelegate instance = (PersistenceDelegate) storage.getOrLoadData(PersistenceDelegate.class, HardScience.MODID + node.getID());
        if(instance == null) return false;
        instance.setNode(node);
        instance.readCachedNBT();
        return true;
    }
    
    /**
     * Use to inactivate nodes already created.  
     * Note that the node files are never removed.
     */
    public static void unRegisterNode(World world, SimulationNode node)
    {
        MapStorage storage = world.getMapStorage();
        PersistenceDelegate instance = (PersistenceDelegate) storage.getOrLoadData(PersistenceDelegate.class, HardScience.MODID + node.getID());
        if(instance != null) instance.setNode(null);
    }
}
