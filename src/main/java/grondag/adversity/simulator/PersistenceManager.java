package grondag.adversity.simulator;

import grondag.adversity.Adversity;
import grondag.adversity.simulator.base.INode;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

public class PersistenceManager 
{
    /** use to persist new nodes */
    public static void registerNode(World world, INode node)
    {
        PersistenceDelegate instance = new PersistenceDelegate(Adversity.MODID + node.getID());
        instance.setNode(node);
        world.getMapStorage().setData(instance.mapName, instance);
    }
    
    /** 
     * Use to load nodes persisted earlier.
     * Returns true if node found and loaded, false otherwise.
     */
    public static boolean loadNode(World world, INode node)
    {
        MapStorage storage = world.getMapStorage();
        PersistenceDelegate instance = (PersistenceDelegate) storage.loadData(PersistenceDelegate.class, Adversity.MODID + node.getID());
        if(instance == null) return false;
        instance.setNode(node);
        instance.readCachedNBT();
        return true;
    }
    
    /**
     * Use to inactivate nodes already created.  
     * Note that the node files are never removed.
     */
    public static void unRegisterNode(World world, INode node)
    {
        MapStorage storage = world.getMapStorage();
        PersistenceDelegate instance = (PersistenceDelegate) storage.loadData(PersistenceDelegate.class, Adversity.MODID + node.getID());
        if(instance != null) instance.setNode(null);
    }
}
