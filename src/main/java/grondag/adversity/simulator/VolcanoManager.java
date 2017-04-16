package grondag.adversity.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import grondag.adversity.Adversity;
import grondag.adversity.Configurator;
import grondag.adversity.feature.volcano.TileVolcano.VolcanoStage;
import grondag.adversity.simulator.base.NodeRoots;
import grondag.adversity.simulator.base.SimulationNode;
import grondag.adversity.simulator.base.SimulationNodeRunnable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VolcanoManager extends SimulationNodeRunnable
{

    private static int MAX_NODES = 1024;
    
    private AtomicInteger maxIdInUse = new AtomicInteger(-1);
    
    private volatile VolcanoNode[] nodes = new VolcanoNode[MAX_NODES];
    
    private static final int NO_ACTIVE_INDEX = -1;
    private volatile int activeIndex = NO_ACTIVE_INDEX;
    
    private LinkedList<Ticket> tickets = new LinkedList<Ticket>();
    private  AtomicBoolean isChunkloadingDirty = new AtomicBoolean(true);
    
    protected VolcanoManager()
    {
        super(NodeRoots.VOLCANO_MANAGER.ordinal());
    }

    private NBTTagCompound nbtVolcanoManager = new NBTTagCompound();
        
    /** not thread-safe - to be called on world sever thread */
    @Override
    public void doOnTick()
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
                        chunkTicket = ForgeChunkManager.requestTicket(Adversity.INSTANCE, worldObj, ForgeChunkManager.Type.NORMAL);
                        chunkTicket.getModData().setInteger("TYPE", this.getID());
                        tickets.add(chunkTicket);
                        chunksUsedThisTicket = 0;
                    }
                    // 7 chunk radius
                    if(x*x + z*z <= 49)
                    {
                        ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(centerX + x, centerZ + z));
                        chunksUsedThisTicket++;
                    }
                }
            }
        
    }
    
    /**
     * Checks for activation if no volcanos are active,
     * or updates the active volcano is there is one.
     */
    @Override
    public void doOffTick()
    {
        
        if(this.activeIndex == NO_ACTIVE_INDEX)
        {
            ArrayList<VolcanoNode> candidates = new ArrayList<VolcanoNode>(MAX_NODES);
            long totalWeight = 0;
            
            for ( int i = 0; i <= maxIdInUse.get(); i++) {
                VolcanoNode node = nodes[i];
                if(node != null 
                        && node.getWeight() > 0
                        && node.wantsToActivate())
                {
                    candidates.add(node);
                    totalWeight += node.getWeight();
                }
            }
            
            if(!candidates.isEmpty())
            {
                long targetWeight = (long) (ThreadLocalRandom.current().nextFloat() * totalWeight);
                
                for(VolcanoNode candidate : candidates)
                {
                    targetWeight -= candidate.getWeight();
                    if(targetWeight < 0)
                    {
                        candidate.activate();
                        return;
                    }
                }
            }
            
        }
        else
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
        if(nodes == null)
        {
            Adversity.LOG.warn("Volcano simulation manager not properly initialized."
                    + " Volcano simulation state will be invalid.");
            return null;
        }
        if(nodeID < 0 || nodeID >= nodes.length)
        {
            Adversity.LOG.warn("Invalid volcano node id: " + nodeID
                    + ". Volcano simulation state will be invalid.");
            return null;
        }
        return nodes[nodeID];
    }
    
    public VolcanoNode findNode(BlockPos pos)
    {
        if(nodes == null)
        {
            Adversity.LOG.warn("Volcano simulation manager not properly initialized."
                    + " Volcano simulation state will be invalid.");
            return null;
        }
        for(int nodeID = 0; nodeID < this.maxIdInUse.get(); nodeID++)
        {
            VolcanoNode node = nodes[nodeID];
            if(node != null && node.getX() == pos.getX() && node.getY() == pos.getY() && node.getZ() == pos.getZ())
            {   
                return node;
            }
        }
        return null;
    }
    
    public VolcanoNode createNode()
    {
        // TODO: expand array or switch to a concurrent structure
        
        int nodeID = maxIdInUse.incrementAndGet();

        if(nodeID >= MAX_NODES) return null;
  
        synchronized(nodes)
        {
            nodes[nodeID] = new VolcanoNode(nodeID);
        }
        this.setSaveDirty(true);
        return nodes[nodeID];
    }
    
    /**
     * Not thread-safe.  
     * Should only ever be called from server thread during server start up.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        
//        Adversity.log.info("readNBT volcanoManager");
        
        nbtVolcanoManager = nbt.getCompoundTag(NodeRoots.VOLCANO_MANAGER.getTagKey());
        NBTTagCompound nbtSubNodes;
        this.maxIdInUse.set(-1);
        this.activeIndex = NO_ACTIVE_INDEX;
        nodes = new VolcanoNode[MAX_NODES];
        
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
        if(nbtSubNodes == null) nbtSubNodes = new NBTTagCompound();
        
        VolcanoNode node;
        
        for (int i = 0; i <= maxIdInUse.get(); i++)
        {
            node = nodes[i];
            if(node != null && node.isSaveDirty())
            {
                NBTTagCompound nodeTag = new NBTTagCompound();
                node.writeToNBT(nodeTag);
                nbtSubNodes.setTag(node.getTagKey(), nodeTag);
            }
        }
        nbtVolcanoManager.setTag(NodeRoots.SUBNODES_TAG, nbtSubNodes);

        nbt.setTag(NodeRoots.VOLCANO_MANAGER.getTagKey(), nbtVolcanoManager);
    }

    public class VolcanoNode extends SimulationNode
    {
        /** 
         * Occasionally updated by TE based on how
         * long the containing chunk has been inhabited.
         * Does not need to be thread-safe because it
         * will only be updated by server tick thread.
         */
        private int weight = 0;
        private static final String TAG_WEIGHT = "w";
        
        private VolcanoStage stage;
        private static final String TAG_STAGE = "s";
        
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
        
        /** stores total world time of last TE update */
        private volatile long keepAlive;
        
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
         * Called by TE from world tick thread.
         */
        public void updateWorldState(int newWeight, int newHeight, VolcanoStage newStage)
        {
            if(newWeight != weight)
            {
                this.weight = newWeight;
                this.setSaveDirty(true);
            }
            if(newHeight != height)
            {
                this.height = newHeight;
                this.setSaveDirty(true);
            }
            if(newStage != stage)
            {
                this.stage = newStage;
                this.setSaveDirty(true);
            }
            this.keepAlive = Simulator.INSTANCE.getWorld().getTotalWorldTime();
//            Adversity.log.info("keepAlive=" + this.keepAlive);
        }
        
        
        /** called periodically on server tick thread by volcano manager when this is the active node */
        public void update()
        {
            if(this.isActive && this.keepAlive + 2048L < Simulator.INSTANCE.getWorld().getTotalWorldTime())
            {
                Adversity.LOG.warn("Active volcano tile entity at " + this.x + ", " + this.y + ", " + this.z 
                + " has not reported in. Deactivating volcano simulation node.");
                this.deActivate();
            }
        }
        
        @Override
        public void readFromNBT(NBTTagCompound nbt)
        {
            this.weight = nbt.getInteger(TAG_WEIGHT);                  
            this.height = nbt.getInteger(TAG_HEIGHT);
            this.stage = VolcanoStage.values()[nbt.getInteger(TAG_STAGE)];
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
                nbt.setInteger(TAG_STAGE, this.stage.ordinal());
                nbt.setInteger(TAG_X, this.x);
                nbt.setInteger(TAG_Y, this.y);
                nbt.setInteger(TAG_Z, this.z);
                nbt.setInteger(TAG_DIMENSION, this.dimension);
                nbt.setBoolean(TAG_ACTIVE, this.isActive);
                nbt.setInteger(TAG_LAST_ACTIVATION_TICK, this.lastActivationTick);
            }
        }

        public void setLocation(BlockPos pos, int dimension) 
        { 
            synchronized(this)
            {
                this.x = pos.getX();
                this.y = pos.getY();
                this.z = pos.getZ();
                this.dimension = dimension; 
                this.setSaveDirty(true);
            }
        }
        
        public boolean wantsToActivate()
        {
            if(this.isActive || this.height >= Configurator.VOLCANO.maxYLevel) return false;
            
            int dormantTime = Simulator.INSTANCE.getTick() - this.lastActivationTick;
            
            if(dormantTime < Configurator.VOLCANO.minDormantTicks) return false;
            
            float chance = (float)dormantTime / Configurator.VOLCANO.maxDormantTicks;
            chance = chance * chance * chance;
            
            return ThreadLocalRandom.current().nextFloat() <= chance;

        }
        
        public void activate()
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
                    this.lastActivationTick = Simulator.INSTANCE.getTick();
                    this.setSaveDirty(true);
                    VolcanoManager.this.activeIndex = this.nodeID;
                    VolcanoManager.this.isChunkloadingDirty.set(true);
                    this.keepAlive = Simulator.INSTANCE.getWorld().getTotalWorldTime();
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
        
        public int getX() { return this.x; }
        public int getY() { return this.y; }
        public int getZ() { return this.z; }
        public int getDimension() { return this.dimension; }
        public int getWeight() { return this.weight; }
        public boolean isActive() { return this.isActive; }
        public int getLastActivationTick() { return this.lastActivationTick; }

    }
}
