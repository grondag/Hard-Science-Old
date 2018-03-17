package grondag.hard_science.volcano.lava.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.world.Location;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.Log;
import grondag.hard_science.init.ModNBTTag;
import grondag.hard_science.simulator.ISimulationTickable;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.persistence.IDirtKeeper;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import grondag.hard_science.volcano.VolcanoTileEntity.VolcanoStage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VolcanoManager implements ISimulationTickable, IPersistenceNode
{
    private HashMap<Location, VolcanoNode> nodes = new HashMap<Location, VolcanoNode>();
    
    private VolcanoNode activeNode = null; 
    private boolean isDirty = true;
    
    private LinkedList<Ticket> tickets = new LinkedList<Ticket>();
    private  boolean isChunkloadingDirty = true;
    
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
            World worldObj = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(node.getDimension());
            
            Ticket chunkTicket = null;
            int chunksUsedThisTicket = 0;
            
            for(int x = -7; x <= 7; x++)
            {
                for(int z = -7; z <= 7; z++)
                {
                    if(chunkTicket == null || (chunksUsedThisTicket == chunkTicket.getChunkListDepth()))
                    {
                        chunkTicket = ForgeChunkManager.requestTicket(HardScience.INSTANCE, worldObj, ForgeChunkManager.Type.NORMAL);
//                        chunkTicket.getModData().setInteger("TYPE", this.getID());
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
        return this.nodes.get(new Location(pos, dimensionID));
    }
    
    public VolcanoNode createNode(BlockPos pos, int dimensionID)
    {
        Location loc = new Location(pos, dimensionID);
        VolcanoNode result = new VolcanoNode(loc);
        this.nodes.put(loc, result);
        this.setSaveDirty(true);
        return result;
    }
    
    /**
     * Not thread-safe.  
     * Should only ever be called from server thread during server start up.
     */
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.activeNode = null;
        nodes = new HashMap<Location, VolcanoNode>();
        
        if(nbt != null)
        {
            NBTTagList nbtSubNodes = nbt.getTagList(ModNBTTag.VOLCANO_NODES, 10);
            if( nbtSubNodes != null && !nbtSubNodes.hasNoTags())
            {
                for (int i = 0; i < nbtSubNodes.tagCount(); ++i)
                {
                    VolcanoNode node = new VolcanoNode(null);
                    node.deserializeNBT(nbtSubNodes.getCompoundTagAt(i));
                    nodes.put(node.getLocation(), node);
                    if(node.isActive) this.activeNode = node;
                }   
            }
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        // always save *something* to prevent "not checked" warning when there are no volcanos
        nbt.setBoolean(ModNBTTag.VOLCANO_MANAGER_IS_CREATED, true);
        
        // Do start because any changes made after this point aren't guaranteed to be saved
        this.setSaveDirty(false);

        NBTTagList nbtSubNodes = new NBTTagList();
        
        if(!this.nodes.isEmpty())
        {
            for (VolcanoNode node : this.nodes.values())
            {
                NBTTagCompound nodeTag = new NBTTagCompound();
                node.serializeNBT(nodeTag);
                nbtSubNodes.appendTag(nodeTag);
            }
        }
        nbt.setTag(ModNBTTag.VOLCANO_NODES, nbtSubNodes);
    }

    public class VolcanoNode implements IReadWriteNBT, IDirtKeeper
    {
        /** 
         * Occasionally updated by TE based on how
         * long the containing chunk has been inhabited.
         * Does not need to be thread-safe because it
         * will only be updated by server tick thread.
         */
        private int weight = 0;
        private VolcanoStage stage;
        
        private int height = 0;
        
        private volatile boolean isActive = false;
        
        /** stores total world time of last TE update */
        private volatile long keepAlive;
        
        private Location location;
        
        private boolean isDirty;
        
        /** 
         * Last time (sim ticks) this volcano became active.
         * If 0, has never been active.
         * If the volcano is active, can be used to calculate how long it has been so.
         */
        private volatile int lastActivationTick;
        
        public VolcanoNode(Location location)
        {
            this.location = location;
        }
        
        @Override
        public void setSaveDirty(boolean isDirty)
        {
            if(isDirty != this.isDirty)
            {
                this.isDirty = isDirty;
                if(isDirty) VolcanoManager.this.setSaveDirty(true);
            }
        }
        
        @Override
        public boolean isSaveDirty()
        {
            return this.isDirty;
        }
        
        /** 
         * Called by TE from world tick thread.
         */
        public void updateWorldState(int newWeight, int newHeight, VolcanoStage newStage)
        {
            boolean isDirty = false;
            if(newWeight != weight)
            {
                this.weight = newWeight;
                isDirty = true;
            }
            if(newHeight != height)
            {
                this.height = newHeight;
                isDirty = true;
            }
            if(newStage != stage)
            {
                this.stage = newStage;
                isDirty = true;
            }
            this.keepAlive = Simulator.instance().getWorld().getTotalWorldTime();
            
            if(isDirty) this.setDirty();
//            HardScience.log.info("keepAlive=" + this.keepAlive);
        }
        
        
        /** called periodically on server tick thread by volcano manager when this is the active node */
        public void update()
        {
            if(this.isActive && this.keepAlive + 2048L < Simulator.instance().getWorld().getTotalWorldTime())
            {
                Log.warn("Active volcano tile entity at " + this.location.toString()
                + " has not reported in. Deactivating volcano simulation node.");
                this.deActivate();
            }
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            this.weight = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_WEIGHT);                  
            this.height = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_HEIGHT);
            this.stage = VolcanoStage.values()[nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_STAGE)];
            int x = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_X);
            int y = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_Y);
            int z = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_Z);
            int dimensionID = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_DIMENSION);
            this.setLocation(new BlockPos(x, y, z), dimensionID);
            this.isActive = nbt.getBoolean(ModNBTTag.VOLCANO_NODE_TAG_ACTIVE);
            this.lastActivationTick = nbt.getInteger(ModNBTTag.VOLCANO_NODE_TAG_LAST_ACTIVATION_TICK);
        }

        @Override
        public void serializeNBT(NBTTagCompound nbt)
        {
            synchronized(this)
            {
                this.setSaveDirty(false);
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_WEIGHT, this.weight);
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_HEIGHT, this.height);
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_STAGE, this.stage.ordinal());
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_X, this.getX());
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_Y, this.getY());
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_Z, this.getZ());
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_DIMENSION, this.getDimension());
                nbt.setBoolean(ModNBTTag.VOLCANO_NODE_TAG_ACTIVE, this.isActive);
                nbt.setInteger(ModNBTTag.VOLCANO_NODE_TAG_LAST_ACTIVATION_TICK, this.lastActivationTick);
            }
        }

        public void setLocation(BlockPos pos, int dimensionID) 
        { 
            synchronized(this)
            {
                this.location = new Location(pos, dimensionID);
                this.setSaveDirty(true);
            }
        }
        
        public boolean wantsToActivate()
        {
            if(this.isActive || this.height >= Configurator.VOLCANO.maxYLevel) return false;
            
            int dormantTime = Simulator.instance().getTick() - this.lastActivationTick;
            
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
                    this.lastActivationTick = Simulator.instance().getTick();
                    this.setSaveDirty(true);
                    VolcanoManager.this.activeNode = this;
                    VolcanoManager.this.isChunkloadingDirty = true;
                    this.keepAlive = Simulator.instance().getWorld().getTotalWorldTime();
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
        
        public int getX() { return this.location.getX(); }
        public int getY() { return this.location.getY(); }
        public int getZ() { return this.location.getZ(); }
        public int getDimension() { return this.location.dimensionID(); }
        public Location getLocation() { return this.location; }
        public int getWeight() { return this.weight; }
        public boolean isActive() { return this.isActive; }
        public int getLastActivationTick() { return this.lastActivationTick; }

    }

    @Override
    public boolean isSaveDirty()
    {
        return this.isDirty;
    }

    @Override
    public void setSaveDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
        
    }

    @Override
    public String tagName()
    {
        return ModNBTTag.VOLCANO_MANAGER;
    }
}
