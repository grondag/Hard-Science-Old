package grondag.adversity.feature.volcano.lava.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import grondag.adversity.Adversity;
import grondag.adversity.Configurator;
import grondag.adversity.Log;
import grondag.adversity.feature.volcano.VolcanoTileEntity.VolcanoStage;
import grondag.adversity.library.world.UniversalPos;
import grondag.adversity.simulator.NodeRoots;
import grondag.adversity.simulator.SimulationNode;
import grondag.adversity.simulator.SimulationNodeRunnable;
import grondag.adversity.simulator.Simulator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VolcanoManager extends SimulationNodeRunnable
{
    private int nextNodeID = 0;
    
    private HashMap<UniversalPos, VolcanoNode> nodes = new HashMap<UniversalPos, VolcanoNode>();
    
    private VolcanoNode activeNode = null; 
    
    private LinkedList<Ticket> tickets = new LinkedList<Ticket>();
    private  boolean isChunkloadingDirty = true;
    
    public VolcanoManager()
    {
        super(NodeRoots.VOLCANO_MANAGER.ordinal());
    }

    private NBTTagCompound nbtVolcanoManager = new NBTTagCompound();
        
    /** not thread-safe - to be called on world sever thread */
    @Override
    public void doOnTick()
    {

        if(this.isChunkloadingDirty)
        {
            this.isChunkloadingDirty = false;
            
            for(Ticket oldTicket : this.tickets)
            {
                ForgeChunkManager.releaseTicket(oldTicket);
            } 
            tickets.clear();
            
            VolcanoNode node = this.activeNode;
            
            if(node == null) return;
           
            int centerX = node.getX() >> 4;
            int centerZ = node.getZ() >> 4;
            World worldObj = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.getDimension());
            
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
    }
    
    /**
     * Checks for activation if no volcanos are active,
     * or updates the active volcano is there is one.
     */
    @Override
    public void doOffTick()
    {
        
        VolcanoNode active = this.activeNode;
        
        if(active == null)
        {
            long totalWeight = 0;
            
            ArrayList<VolcanoNode> candidates = new ArrayList<VolcanoNode>(this.nodes.size());
            
            for ( VolcanoNode node : this.nodes.values()) 
            {
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
            active.update();
        }
    }

    public VolcanoNode findNode(BlockPos pos, int dimensionID)
    {
        if(nodes == null)
        {
            Log.warn("Volcano simulation manager not properly initialized."
                    + " Volcano simulation state will be invalid.");
            return null;
        }
        return this.nodes.get(new UniversalPos(pos, dimensionID));
    }
    
    public VolcanoNode createNode(BlockPos pos, int dimensionID)
    {
        VolcanoNode result = new VolcanoNode(this.nextNodeID++, new UniversalPos(pos, dimensionID));
        this.nodes.put(new UniversalPos(pos, dimensionID), result);
        this.setSaveDirty(true);
        return result;
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
        this.nextNodeID = 0;
        this.activeNode = null;
        nodes = new HashMap<UniversalPos, VolcanoNode>();
        
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
                VolcanoNode node = new VolcanoNode(getNodeIdFromTagKey(key), null);
                node.readFromNBT(nbtSubNodes.getCompoundTag(key));
                nodes.put(node.getUniversalPos(), node);
                this.nextNodeID = (Math.max(this.nextNodeID, node.getID() + 1));
            }   
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        // Do first because any changes made after this point aren't guaranteed to be saved
        this.setSaveDirty(false);

        // nothing to do if no nodes
        if(this.nodes.isEmpty()) return;
        
        NBTTagCompound nbtSubNodes = nbtVolcanoManager.getCompoundTag(NodeRoots.SUBNODES_TAG);
        if(nbtSubNodes == null) nbtSubNodes = new NBTTagCompound();
        
        for (VolcanoNode node : this.nodes.values())
        {
            if(node.isSaveDirty())
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
        
        private static final String TAG_X = "x";
        private static final String TAG_Y = "y";
        private static final String TAG_Z= "z";
        private static final String TAG_DIMENSION = "d";
        
        private volatile boolean isActive = false;
        private static final String TAG_ACTIVE = "a";
        
        /** stores total world time of last TE update */
        private volatile long keepAlive;
        
        private UniversalPos uPos;
        
        /** 
         * Last time (sim ticks) this volcano became active.
         * If 0, has never been active.
         * If the volcano is active, can be used to calculate how long it has been so.
         */
        private volatile int lastActivationTick;
        private static final String TAG_LAST_ACTIVATION_TICK = "t";
        
        public VolcanoNode(int nodeID, UniversalPos uPos)
        {
            super(nodeID);
            this.uPos = uPos;
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
                Log.warn("Active volcano tile entity at " + this.uPos.pos.toString()
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
            int x = nbt.getInteger(TAG_X);
            int y = nbt.getInteger(TAG_Y);
            int z = nbt.getInteger(TAG_Z);
            int dimensionID = nbt.getInteger(TAG_DIMENSION);
            this.setLocation(new BlockPos(x, y, z), dimensionID);
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
                nbt.setInteger(TAG_X, this.getX());
                nbt.setInteger(TAG_Y, this.getY());
                nbt.setInteger(TAG_Z, this.getZ());
                nbt.setInteger(TAG_DIMENSION, this.getDimension());
                nbt.setBoolean(TAG_ACTIVE, this.isActive);
                nbt.setInteger(TAG_LAST_ACTIVATION_TICK, this.lastActivationTick);
            }
        }

        public void setLocation(BlockPos pos, int dimensionID) 
        { 
            synchronized(this)
            {
                this.uPos = new UniversalPos(pos, dimensionID);
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
            if(VolcanoManager.this.activeNode != this)
            {
                VolcanoNode oldActive = VolcanoManager.this.activeNode;
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
                    VolcanoManager.this.activeNode = this;
                    VolcanoManager.this.isChunkloadingDirty = true;
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
                    VolcanoManager.this.activeNode = null;
                    VolcanoManager.this.isChunkloadingDirty = true;
                }
            }
        }
        
        public int getX() { return this.uPos.pos.getX(); }
        public int getY() { return this.uPos.pos.getY(); }
        public int getZ() { return this.uPos.pos.getZ(); }
        public int getDimension() { return this.uPos.dimensionID; }
        public UniversalPos getUniversalPos() { return this.uPos; }
        public int getWeight() { return this.weight; }
        public boolean isActive() { return this.isActive; }
        public int getLastActivationTick() { return this.lastActivationTick; }

    }
}
