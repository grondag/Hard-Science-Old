package grondag.hard_science.superblock.virtual;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.library.world.IntegerAABB;
import grondag.hard_science.network.ModMessages;
import grondag.hard_science.network.server_to_client.PacketExcavationRenderUpdate;
import grondag.hard_science.simulator.storage.jobs.AbstractPositionedTask;
import grondag.hard_science.simulator.storage.jobs.AbstractTask;
import grondag.hard_science.simulator.storage.jobs.ITaskListener;
import grondag.hard_science.simulator.storage.jobs.Job;
import grondag.hard_science.simulator.storage.jobs.WorldTaskManager;
import grondag.hard_science.simulator.storage.jobs.tasks.ExcavationTask;
import grondag.hard_science.simulator.storage.jobs.tasks.PlacementTask;
import grondag.hard_science.superblock.placement.BuildManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

/**
 * Class exists on server but render methods do not.
 * Server instantiates (and generates IDs) and transmits to clients.
 */
public class ExcavationRenderEntry implements ITaskListener, Runnable
{
    private static int nextID = 0;
    
    public final int id;
    
    /**
     * If true, is replacement instead of straight excavation.
     */
    public final boolean isExchange;
    
    public final int dimensionID;
    
    public final int domainID;
    
    private IntegerAABB aabb;
    
    private boolean isFirstComputeDone = false;
    
    /** Non-null if should render individual renderPositions instead of AABB */
    private BlockPos[] renderPositions = null;
    
    private Int2ObjectMap<AtomicInteger> xCounts = new Int2ObjectOpenHashMap<AtomicInteger>();
    private Int2ObjectMap<AtomicInteger> yCounts = new Int2ObjectOpenHashMap<AtomicInteger>();
    private Int2ObjectMap<AtomicInteger> zCounts = new Int2ObjectOpenHashMap<AtomicInteger>();
    
    private Set<BlockPos> positions = Collections.synchronizedSet(new HashSet<BlockPos>());
    
    private boolean isValid = true;
    
    /**
     * True if has been submitted for re-computation.
     * If true, will not be resubmitted when becomes dirty.
     */
    private AtomicBoolean isScheduled = new AtomicBoolean();
    
    /**
     * If true, has changed after the start of the last computation.  
     * Cleared at start of computation run. <p>
     *  
     * If dirty when computation completes,
     * computation will resubmit self to queue for recomputation.<p>
     * 
     * If becomes dirty while computation not in progress, 
     * {@link #setDirty()} will submit for computation.
     */
    private AtomicBoolean isDirty = new AtomicBoolean(true);
    
    /**
     * Players who could be viewing this excavation and should received client-side updates.
     */
    private SimpleUnorderedArrayList<EntityPlayerMP> listeners = new SimpleUnorderedArrayList<EntityPlayerMP>();
    
    private void addPos(BlockPos pos)
    {
        this.positions.add(pos);
        
        synchronized(xCounts)
        {
            AtomicInteger xCounter = xCounts.get(pos.getX());
            if(xCounter == null)
            {
                xCounter = new AtomicInteger(1);
                xCounts.put(pos.getX(), xCounter);
            }
            else
            {
                xCounter.incrementAndGet();
            }
        }
        
        synchronized(yCounts)
        {
            AtomicInteger yCounter = yCounts.get(pos.getY());
            if(yCounter == null)
            {
                yCounter = new AtomicInteger(1);
                yCounts.put(pos.getY(), yCounter);
            }
            else
            {
                yCounter.incrementAndGet();
            }
        }
        
        synchronized(zCounts)
        {
            AtomicInteger zCounter = zCounts.get(pos.getZ());
            if(zCounter == null)
            {
                zCounter = new AtomicInteger(1);
                zCounts.put(pos.getZ(), zCounter);
            }
            else
            {
                zCounter.incrementAndGet();
            }
        }
    }
    
    /** 
     * Returns true if any dimension had a count drop to zero
     */
    private boolean removePos(BlockPos pos)
    {
        this.positions.remove(pos);
        boolean gotZero = xCounts.get(pos.getX()).decrementAndGet() == 0;
        gotZero = yCounts.get(pos.getY()).decrementAndGet() == 0 || gotZero;
        gotZero = zCounts.get(pos.getZ()).decrementAndGet() == 0 || gotZero;
        return gotZero;
    }
    
    /**
     * For server side
     */
    public ExcavationRenderEntry(Job job)
    {
        this.id = nextID++;
        
        if(Configurator.logExcavationRenderTracking) Log.info("id = %d new Entry constructor", this.id);
        
        this.domainID = job.getDomain().getId();
        
        this.dimensionID = job.getDimensionID();
        
        boolean isExchange = false;
        
        for(AbstractTask task : job)
        {
            if(task instanceof ExcavationTask)
            {
                if(!task.isTerminated())
                {
                    task.addListener(ExcavationRenderEntry.this);
                    this.addPos(((ExcavationTask)task).pos());
                }
            }
            else if(!isExchange && task instanceof PlacementTask)
            {
                isExchange = true;
            }
        }
        
        this.isExchange = isExchange;
        
        if(ExcavationRenderEntry.this.positions.size() == 0)
        {
            if(Configurator.logExcavationRenderTracking) Log.info("id = %d new Entry constructor - invalid", this.id);
            this.isValid = false;
        }
        else
        {
            if(Configurator.logExcavationRenderTracking) Log.info("id = %d new Entry constructor - launching compute", this.id);
            ExcavationRenderEntry.this.compute();
        }
    }
    
    @Override
    public void onTaskComplete(AbstractTask task)
    {
        boolean needsCompute = this.removePos(((AbstractPositionedTask)task).pos());
        this.isValid = this.isValid && this.positions.size() > 0;
        if(this.isValid)
        {
            if(needsCompute) this.setDirty();
        }
        else
        {
            ExcavationRenderTracker.INSTANCE.remove(this);
        }
    }
    
    private void setDirty()
    {
        this.isDirty.compareAndSet(false, true);
        this.compute();
    }
    
    /**
     * If false, can't send packets with this.
     * Implies compute in progress or to be scheduled.
     */
    public boolean isFirstComputeDone()
    {
        return this.isFirstComputeDone;
    }

    private void compute()
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id = %d Compute called. Already running = %s", this.id, Boolean.toString(this.isScheduled.get()));
        if(this.isScheduled.compareAndSet(false, true))
        {
            BuildManager.EXECUTOR.execute(this);
        }
    }
    
    @Override
    public void run()
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id = %d Compute running.", this.id);
        
        this.isDirty.set(false);
        
        int count = this.positions.size();
        
        if(count == 0)
        {
            if(Configurator.logExcavationRenderTracking) Log.info("id = %d Compute existing due to empty positions.", this.id);
            this.updateListeners();
            ExcavationRenderTracker.INSTANCE.remove(this);
            return;
        }
        
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for(Int2ObjectMap.Entry<AtomicInteger> x : this.xCounts.int2ObjectEntrySet())
        {
            if(x.getValue().get() > 0)
            {
                minX = Math.min(minX, x.getIntKey());
                maxX = Math.max(maxX, x.getIntKey());
            }
        }
        
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for(Int2ObjectMap.Entry<AtomicInteger> y : this.yCounts.int2ObjectEntrySet())
        {
            if(y.getValue().get() > 0)
            {
                minY = Math.min(minY, y.getIntKey());
                maxY = Math.max(maxY, y.getIntKey());
            }
        }
        
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for(Int2ObjectMap.Entry<AtomicInteger> z : this.zCounts.int2ObjectEntrySet())
        {
            if(z.getValue().get() > 0)
            {
                minZ = Math.min(minZ, z.getIntKey());
                maxZ = Math.max(maxZ, z.getIntKey());
            }
        }
        
        IntegerAABB newBox = new IntegerAABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        
        // always send start time computed
        boolean needsListenerUpdate = !this.isFirstComputeDone;
        this.isFirstComputeDone = true;
        
        if(!newBox.equals(this.aabb))
        {   
            this.aabb = newBox;
            needsListenerUpdate = true;
        }
        
        if(count <= 16 && (this.renderPositions == null || this.renderPositions.length != count))
        {
            synchronized(this.positions)
            {
                BlockPos[] newPositions = new BlockPos[this.positions.size()];
                newPositions = this.positions.toArray(newPositions);
                this.renderPositions = newPositions;
            }
            needsListenerUpdate = true;
            if(Configurator.logExcavationRenderTracking) Log.info("id %d Computed render position length = %d", this.id, this.renderPositions == null ? 0 : this.renderPositions.length);
        }
        
        if(Configurator.logExcavationRenderTracking) Log.info("id = %d Compute done, updateListeners=%s, isDirty=%s", this.id, Boolean.toString(needsListenerUpdate),
                Boolean.toString(this.isDirty.get()));
        
        if(needsListenerUpdate) this.updateListeners();
        
        if(this.isDirty.get() && count > 0) 
            BuildManager.EXECUTOR.execute(this);
        else
            this.isScheduled.set(false);
    }

    /**
     * Checked by excavation tracker on creation and will not add if false.
     * {@link #onTaskComplete(AbstractTask)} also uses as signal to remove this instance from tracker.
     */
    public boolean isValid()
    {
        return isValid;
    }

    public IntegerAABB aabb()
    {
        return aabb;
    }
    
    public void addListener(EntityPlayerMP listener, boolean sendPacketIfNew)
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id=%d addListenger sendIfNew=%s, isValue=%s, isFirstComputeDone=%s",
                this.id,
                Boolean.toString(sendPacketIfNew),
                Boolean.toString(isValid),
                Boolean.toString(isFirstComputeDone));
        
        synchronized(this.listeners)
        {
            if(this.listeners.addIfNotPresent(listener) && sendPacketIfNew && this.isValid && this.isFirstComputeDone)
            {
                if(Configurator.logExcavationRenderTracking) Log.info("id=%d addListenger scheduling packet.", this.id);
                WorldTaskManager.sendPacketFromServerThread(new PacketExcavationRenderUpdate(ExcavationRenderEntry.this), listener, true);
            }
        }
    }
    
    public void removeListener(EntityPlayerMP listener)
    {
        synchronized(this.listeners)
        {
            this.listeners.removeIfPresent(listener);
        }
    }
    
    public void updateListeners()
    {
        if(this.listeners.isEmpty()) return;
        
        // think network operations need to run in world tick
        WorldTaskManager.enqueueImmediate(new Runnable() 
        {
            PacketExcavationRenderUpdate packet = 
                    ExcavationRenderEntry.this.isValid && ExcavationRenderEntry.this.positions.size() > 0
                        // update
                        ? new PacketExcavationRenderUpdate(ExcavationRenderEntry.this)
                        // remove
                        : new PacketExcavationRenderUpdate(ExcavationRenderEntry.this.id);
                        
            @Override
            public void run()
            {
                synchronized(ExcavationRenderEntry.this.listeners)
                {
                    if(!ExcavationRenderEntry.this.listeners.isEmpty())
                    {
                        for(EntityPlayerMP player : listeners)
                        {
                            ModMessages.INSTANCE.sendTo(packet, player);
                        }
                    }
                }
            }
        });
    }

    /**
     * Will be non-null if should render individual renderPositions. 
     * Populated when position count is small enough not to be a problem.
     */
    @Nullable
    public BlockPos[] renderPositions()
    {
        if(Configurator.logExcavationRenderTracking) Log.info("id %d Render position retrieval, count = %d", this.id, this.renderPositions == null ? 0 : this.renderPositions.length);
        return this.renderPositions;
    }
}
