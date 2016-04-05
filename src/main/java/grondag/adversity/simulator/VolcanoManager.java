package grondag.adversity.simulator;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import grondag.adversity.Adversity;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VolcanoManager extends SimulationNode implements Runnable
{

    private static int MAX_NODES = 1024;
    
    private AtomicInteger maxIdInUse = new AtomicInteger(-1);
    
    private ConcurrentLinkedQueue<Integer> availableIDs;
    
    private VolcanoNode[] nodes;
    
    
    private static final int NO_ACTIVE_INDEX = -1;
    private volatile int activeIndex = NO_ACTIVE_INDEX;
    
    private LinkedList<Ticket> tickets = new LinkedList<Ticket>();
    private  AtomicBoolean isChunkloadingDirty = new AtomicBoolean(true);
    
    protected VolcanoManager()
    {
        super(NodeRoots.VOLCANO_MANAGER.ordinal());
    }

    private NBTTagCompound nbtVolcanoManager;
    
    /** 
     * Tracks the total weight of all known volcanoes.
     * Maintaining this values as we go allows us to do a random
     * weighted selection for activation with only a single traversal of the map.
     * Otherwise we'd have to iterate an additional time to add up all the weights.
     */
    private AtomicInteger totalWeight = new AtomicInteger(0);
    
    /** not thread-safe - to be called on world sever thread */
    public void updateChunkLoading()
    {

            if(!this.isChunkloadingDirty.compareAndSet(true, false)) return;
    
            for(Ticket oldTicket : this.tickets)
            {
                ForgeChunkManager.releaseTicket(oldTicket);
            } 
            tickets.clear();
            
            if(this.activeIndex == NO_ACTIVE_INDEX) return;
            
            VolcanoNode node = nodes[activeIndex];
           
            int centerX = node.getX() >> 4;
            int centerZ = node.getZ() >> 4;
            World worldObj = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);
            
            Ticket chunkTicket = null;
            int chunksUsedThisTicket = 0;
            
            for(int x = -7; x <= 7; x++)
            {
                for(int z = -7; z <= 7; z++)
                {
                    if(chunkTicket == null || (chunksUsedThisTicket == chunkTicket.getChunkListDepth()))
                    {
                        chunkTicket = ForgeChunkManager.requestTicket(Adversity.instance, worldObj, ForgeChunkManager.Type.NORMAL);
                        chunkTicket.getModData().setInteger("TYPE", this.getID());
                        tickets.add(chunkTicket);
                        chunksUsedThisTicket = 0;
                    }
                    // 7 chunk radius
                    if(x*x + z*z <= 49)
                    {
                        ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair(centerX + x, centerZ + z));
                        chunksUsedThisTicket++;
                    }
                }
            }
        
    }
    
    
    @Override
    public void run()
    {
        // if a volcano is active, check for inactivity
        
        if(this.activeIndex != NO_ACTIVE_INDEX)
        {
            VolcanoNode active = nodes[this.activeIndex];
            if(active==null)
            {
                // Should never happen, but handle just in case
                this.activeIndex = NO_ACTIVE_INDEX;
            }
            else
            {
                active.update();
            }
        }
    }

    public VolcanoNode findNode(int nodeID)
    {
        return nodes[nodeID];
    }
    
    public VolcanoNode createNode()
    {
        if(maxIdInUse.get() == MAX_NODES -1) return null;
        
        Integer nodeID;
        
        nodeID = availableIDs.poll();
        
        if(nodeID == null)
        {
            nodeID = maxIdInUse.incrementAndGet();
        }
  
        synchronized(nodes)
        {
            nodes[nodeID] = new VolcanoNode(nodeID);
        }
        this.setSaveDirty(true);
        return nodes[nodeID];
    }
    
    /** to be called by nodes whenever their TE gives them a new weight value */
    private void updateWeight(int oldWeight, int newWeight)
    {
        totalWeight.addAndGet(newWeight - oldWeight);
    }

    /**
     * Not thread-safe.  
     * Should only ever be called from server thread during server start up.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        nbtVolcanoManager = nbt.getCompoundTag(NodeRoots.VOLCANO_MANAGER.getTagKey());
        NBTTagCompound nbtSubNodes;
        totalWeight.set(0);
        this.maxIdInUse.set(-1);
        nodes = new VolcanoNode[MAX_NODES];
        availableIDs = new ConcurrentLinkedQueue<Integer>();
        
        if(nbtVolcanoManager == null)
        {
            nbtVolcanoManager = new NBTTagCompound();
            nbtSubNodes = new NBTTagCompound();
            nbtVolcanoManager.setTag(NodeRoots.SUBNODES_TAG, nbtSubNodes);
        }
        else
        {
            nbtSubNodes = nbtVolcanoManager.getCompoundTag(NodeRoots.SUBNODES_TAG);
            // should never happen but just in case...
            if( nbtSubNodes == null)
            {
                nbtSubNodes = new NBTTagCompound();
                nbtVolcanoManager.setTag(NodeRoots.SUBNODES_TAG, nbtSubNodes);
            }

            for(String key : nbtSubNodes.getKeySet())
            {
                // note that totalWeights is updated by individual nodes during readFromNBT
                VolcanoNode node = new VolcanoNode(getNodeIdFromTagKey(key));
                node.readFromNBT(nbtSubNodes.getCompoundTag(key));
                nodes[node.getID()] = node;
                // no need to synchonize here - readNBT always single threaded
                this.maxIdInUse.set(Math.max(this.maxIdInUse.get(), node.getID()));
                //simNodes.put(node.getID(), node);
                //saveNodes.put(node.getID(), node);   
            }
            
            if(this.maxIdInUse.get() >= 0)
            {
                for(int i = 0; i < this.maxIdInUse.get(); i++)
                {
                    if(nodes[i] == null)
                    {
                        availableIDs.offer(new Integer(i));
                    }
                }
            }
        }    
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        // Do first because any changes made after this point aren't guaranteed to be saved
        this.setSaveDirty(false);

        // nothing to do if no nodes
        if(maxIdInUse.get() == -1) return;
        
        NBTTagCompound nbtSubNodes = nbtVolcanoManager.getCompoundTag(NodeRoots.SUBNODES_TAG);
        VolcanoNode node;
        
        for ( int i = 0; i <= maxIdInUse.get(); i++) {
            node = nodes[i];
            if(node != null)
            {
                if(node.isDeleted())
                {
                    nbtSubNodes.removeTag(node.getTagKey());
                    synchronized(nodes)
                    {
                        nodes[nodeID] = null;
                    }
                    availableIDs.offer(new Integer(i));
                }
                else if (node.isSaveDirty())
                {
                    node.writeToNBT(nbtSubNodes.getCompoundTag(node.getTagKey()));
                }
            }
        }

        nbt.setTag(NodeRoots.VOLCANO_MANAGER.getTagKey(), nbtVolcanoManager);
    }

    public class VolcanoNode extends SimulationNode
    {
        private static final int MAX_VOLCANO_HEIGHT = 180;
        private static final int AVERAGE_TICKS_TO_ACTIVE = 20 * 24000;
        private static final int MIN_ACTIVE_TICKS = 4 * 24000;
        
        private volatile boolean isDeleted = false;
        
        /** 
         * Occasionally updated by TE based on how
         * long the containing chunk has been inhabited.
         * Does not need to be thread-safe because it
         * will only be updated by server tick thread.
         */
        private int weight = 0;
        private static final String TAG_WEIGHT = "w";
        
        private int height = 0;
        private static final String TAG_HEIGHT = "h";
        
        private volatile int x;
        private static final String TAG_X = "x";
        
        private volatile int y;
        private static final String TAG_Y = "y";
        
        private volatile int z;
        private static final String TAG_Z= "z";
        
        private volatile int dimension;
        private static final String TAG_DIMENSION = "d";
        
        private volatile boolean isActive = false;
        private static final String TAG_ACTIVE = "a";
        
        /** 
         * Last time (sim ticks) this volcano became active.
         * If 0, has never been active.
         * If the volcano is active, can be used to calculate how long it has been so.
         */
        private volatile int lastActivationTick;
        private static final String TAG_LAST_ACTIVATION_TICK = "t";
        
        public VolcanoNode(int nodeID)
        {
            super(nodeID);
        }
        
        /** want to set Parent dirty also, if dirty*/
        @Override
        public void setSaveDirty(boolean isDirty)
        {
            super.setSaveDirty(isDirty);
            if(isDirty) VolcanoManager.this.setSaveDirty(isDirty);
        }
        
        /** 
         * Called by TE from server tick thread.
         * Not thread-safe.
         */
        public void setInWorldStats(int newWeight, int newHeight)
        {
            if(newWeight != weight)
            {
                updateWeight(weight, newWeight);
                this.weight = newWeight;
                this.setSaveDirty(true);
            }
            if(newHeight != height)
            {
                this.height = newHeight;
                this.setSaveDirty(true);
            }
        }
        
        /** called periodically by volcano manager when this is the active node */
        public void update(int currentSimTick)
        {
            // check for inactivity
            
            
        }
        
        @Override
        public void readFromNBT(NBTTagCompound nbt)
        {
            this.weight = nbt.getInteger(TAG_WEIGHT);                
            updateWeight(0, weight);   
            this.height = nbt.getInteger(TAG_HEIGHT);
            this.x = nbt.getInteger(TAG_X);
            this.y = nbt.getInteger(TAG_Y);
            this.z = nbt.getInteger(TAG_Z);
            this.dimension = nbt.getInteger(TAG_DIMENSION);
            this.isActive = nbt.getBoolean(TAG_ACTIVE);
            this.lastActivationTick = nbt.getInteger(TAG_LAST_ACTIVATION_TICK);
        }

        @Override
        public void writeToNBT(NBTTagCompound nbt)
        {
            synchronized(this)
            {
                this.setSaveDirty(false);
                nbt.setInteger(TAG_WEIGHT, this.weight);
                nbt.setInteger(TAG_HEIGHT, this.height);
                nbt.setInteger(TAG_X, this.x);
                nbt.setInteger(TAG_Y, this.y);
                nbt.setInteger(TAG_Z, this.z);
                nbt.setInteger(TAG_DIMENSION, this.dimension);
                nbt.setBoolean(TAG_ACTIVE, this.isActive);
                nbt.setInteger(TAG_LAST_ACTIVATION_TICK, this.lastActivationTick);
            }
        }
        
        public boolean isDeleted() { return this.isDeleted;  }
        
        public void delete() 
        { 
            this.isDeleted = true; 
            this.setWeight(0);
            this.deActivate();
            this.setSaveDirty(true);
        }
        
        public int getX() { return this.x; }
        public int getY() { return this.y; }
        public int getZ() { return this.z; }
        public int getDimension() { return this.dimension; }

        public void setLocation(int x, int y, int z, int dimension) 
        { 
            synchronized(this)
            {
                this.x = x;
                this.y = y;
                this.z = z;
                this.dimension = dimension; 
                this.setSaveDirty(true);
            }
        }
        
        public boolean isActive() { return this.isActive; }
        public int getLastActivationTick() { return this.lastActivationTick; }
        
        public void activate( int activationTick)
        {
            // should really be handled by caller but in case not
            if(VolcanoManager.this.activeIndex != NO_ACTIVE_INDEX && VolcanoManager.this.activeIndex != this.nodeID)
            {
                VolcanoNode oldActive = VolcanoManager.this.nodes[VolcanoManager.this.activeIndex];
                if(oldActive != null)
                {
                    oldActive.deActivate();
                }
            }
            
            synchronized(this)
            {
                if(!this.isActive)
                {
                    this.isActive = true;
                    this.lastActivationTick = activationTick;
                    this.setSaveDirty(true);
                    VolcanoManager.this.activeIndex = this.nodeID;
                    VolcanoManager.this.isChunkloadingDirty.set(true);
                }
            }
        }

        public void deActivate()
        {
            synchronized(this)
            {
                if(this.isActive)
                {
                    this.isActive = false;
                    this.setSaveDirty(true);
                    VolcanoManager.this.activeIndex = NO_ACTIVE_INDEX;
                    VolcanoManager.this.isChunkloadingDirty.set(true);
                }
            }
        }
    }
}
