package grondag.adversity.taskmaster;

import grondag.adversity.Adversity;
import grondag.adversity.taskmaster.base.NodeBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class PersistenceManager 
{
    /** use to persist new nodes */
    public static void registerNode(World world, NodeBase node)
    {
        PersistenceDelegate instance = new PersistenceDelegate(Adversity.MODID + node.getID());
        instance.setNode(node);
        world.getMapStorage().setData(instance.mapName, instance);
    }
    
    /** 
     * Use to load nodes persisted earlier.
     * Returns true if node found and loaded, false otherwise.
     */
    public boolean loadNode(World world, NodeBase node)
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
    public static void unRegisterNode(World world, NodeBase node)
    {
        MapStorage storage = world.getMapStorage();
        PersistenceDelegate instance = (PersistenceDelegate) storage.loadData(PersistenceDelegate.class, Adversity.MODID + node.getID());
        if(instance != null) instance.setNode(null);
    }

    private static class PersistenceDelegate extends WorldSavedData
    {
        private NodeBase node;
        /** saves initial nbt load until we know our node reference */
        private NBTTagCompound nbtCache;
        
        private PersistenceDelegate(String name) {
            super(name);
        }
        
        private void setNode(NodeBase node) { this.node = node; }
    
        @Override
        public void readFromNBT(NBTTagCompound nbt)
        {
            if(node == null)
            {
                nbtCache = nbt;
            }
            else 
            {
                node.readFromNBT(nbt); 
            }
        }
        
        private void readCachedNBT()
        {
            if(node != null && nbtCache != null)
            {
                node.readFromNBT(this.nbtCache);
                nbtCache = null;
            }
        }
        
        @Override
        public void writeToNBT(NBTTagCompound nbt) { if(node != null) node.writeToNBT(nbt); }
    
        @Override
        public boolean isDirty() { return node == null ? false : node.isSaveDirty(); }
    
        @Override
        public void setDirty(boolean isDirty) { if(node != null) node.setSaveDirty(isDirty); }
    }
}
