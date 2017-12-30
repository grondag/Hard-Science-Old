package grondag.hard_science.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import grondag.hard_science.Configurator;
import grondag.hard_science.Log;
import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.persistence.IPersistenceNode;
import grondag.hard_science.simulator.persistence.PersistenceManager;
import grondag.hard_science.volcano.lava.simulator.LavaSimulator;
import grondag.hard_science.volcano.lava.simulator.VolcanoManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;


/**
 * Events are processed from a queue in the order they arrive.
 * 
 * World events are always added to the queue as soon as they arrive.
 * 
 * Simulation ticks are generated as the world clock advances
 * at the rate of one simulation tick per world tick.
 * 
 * Simulation ticks are added to the queue by a privileged task 
 * that is added at the end of simulation tick.  No new 
 * simulation ticks are added until all tasks in the last tick are complete.
 * 
 * No simulation ticks are ever skipped. This means that if players
 * sleep and the work clock advances, the simulation will continue
 * running as quickly as possible until caught up.  
 * 
 * However, world events will continue to be processed as soon as they
 * arrive.  This means that a player waking up and interacting with
 * machines immediately may not see that all processing is complete but
 * will observe that the machines are running very quickly.
 * 
 */
public class Simulator  implements IPersistenceNode, ForgeChunkManager.OrderedLoadingCallback
{
    /**
     * Only use if need a reference before it starts.
     */
    public static final Simulator RAW_INSTANCE_DO_NOT_USE = new Simulator();
    
    private VolcanoManager volcanoManager;

    private LavaSimulator lavaSimulator;

    /**
     * General-purpose thread pool. Use for any simulation-related activity
     * so long as it doesn't have specific timing or sequencing requirements.
     */
    public static final ExecutorService SIMULATION_POOL 
        = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new ThreadFactory()
    {
        private AtomicInteger count = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r)
        {
            Thread thread = new Thread(r, "Hard Science Simulation Thread -" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });


    /**
     * For simulation step control - do not use for actual work.
     */
    private static final ExecutorService CONTROL_THREAD = Executors.newSingleThreadExecutor(
            new ThreadFactory()
            {
                private AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r)
                {
                    Thread thread = new Thread(r, "Hard Science Simulation Control Thread -" + count.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            });

    private List<ISimulationTickable> tickables = new ArrayList<ISimulationTickable>();

    private Future<?> lastTickFuture = null;

    /** used for world time */
    private World world;

    private boolean isDirty;

    private volatile boolean isRunning = false;
    public boolean isRunning() { return isRunning; }

    /** true if we've warned once about clock going backwards - prevents log spam */
    private boolean isClockSetbackNotificationNeeded = true;

    //   private AtomicInteger nextNodeID = new AtomicInteger(NodeRoots.FIRST_NORMAL_NODE_ID);
    //    private static final String TAG_NEXT_NODE_ID = "nxid";


    /** 
     * Set to worldTickOffset + lastWorldTick at end of server tick.
     * If equal to currentSimTick, means simulation is caught up with world ticks.
     */
    private volatile int lastSimTick = 0;

    /** worldTickOffset + lastWorldTick = max value of current simulation tick.
     * Updated on server post tick, *after* all world tick events should be submitted.
     */
    private volatile long worldTickOffset = 0; 
    
    private void start()  
    {
        synchronized(this)
        {
            Log.info("Simulator initialization started.");
    
            // we're going to assume for now that all the dimensions we care about are using the overworld clock
            this.world = FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
        
            this.tickables.clear();
            
            MapStorage mapStore = this.world.getMapStorage();
            DeviceManager devMgr = DeviceManager.RAW_INSTANCE_DO_NOT_USE;
            DomainManager dm = DomainManager.RAW_INSTANCE_DO_NOT_USE;

            if(Configurator.VOLCANO.enableVolcano)
            {
                this.volcanoManager = new VolcanoManager();
                this.lavaSimulator = new LavaSimulator(this.world);
            }
            
            if(PersistenceManager.loadNode(mapStore, this))
            {
                if(!PersistenceManager.loadNode(mapStore, dm))
                {
                    Log.warn("Domain manager data not found.  Some world state may be lost.");
                    dm.loadNew();
                    PersistenceManager.registerNode(mapStore, dm);                   
                }

                devMgr.clear();
                if(!PersistenceManager.loadNode(mapStore, devMgr))
                {
                    Log.warn("Device manager data not found.  Some world state may be lost.");
                    PersistenceManager.registerNode(mapStore, devMgr);                   
                }

                if(Configurator.VOLCANO.enableVolcano)
                {
                    if(!PersistenceManager.loadNode(mapStore, this.volcanoManager))
                    {
                        Log.warn("Volcano manager data not found - recreating.  Some world state may be lost.");
                        PersistenceManager.registerNode(mapStore, this.volcanoManager);
                    }
        
                    if(!PersistenceManager.loadNode(mapStore, this.lavaSimulator))
                    {
                        Log.warn("Lava simulator data not found - recreating.  Some world state may be lost.");
                        PersistenceManager.registerNode(mapStore, this.lavaSimulator);
                    }
                }
                
                dm.afterDeserialization();
                devMgr.afterDeserialization();
            }
            else
            {
                Log.info("Creating new simulation.");
                
                // Assume new game and new simulation
                this.lastSimTick = 0; 
                this.worldTickOffset = -this.world.getWorldTime();

                this.setSaveDirty(true);
                PersistenceManager.registerNode(mapStore, this);

                dm.loadNew();
                devMgr.clear();
                PersistenceManager.registerNode(mapStore, dm);
                PersistenceManager.registerNode(mapStore, devMgr);
                
                if(Configurator.VOLCANO.enableVolcano)
                {
                    PersistenceManager.registerNode(mapStore, this.volcanoManager);
                    PersistenceManager.registerNode(mapStore, this.lavaSimulator);
                }
            }
 
            this.tickables.add(devMgr);
            if(Configurator.VOLCANO.enableVolcano)
            {
                this.tickables.add(this.volcanoManager);
                this.tickables.add(this.lavaSimulator);
            }
            
            Log.info("Simulator initialization complete. Simulator running.");
        }
    }

    /** 
     * Called from ServerStopping event.
     * Should be no more ticks after that.
     */
    public synchronized void stop()
    {
        Log.info("stopping server");
        this.isRunning = false;

        // wait for simulation to catch up
        if(this.lastTickFuture != null && !this.lastTickFuture.isDone())
        {
            Log.info("waiting for last frame task completion");
            try
            {
                this.lastTickFuture.get(5, TimeUnit.SECONDS);
            }
            catch (Exception e)
            {
                Log.warn("Timeout waiting for simulation shutdown");
                e.printStackTrace();
            }
        }

        this.volcanoManager = null;
        this.lavaSimulator = null;
        DomainManager.RAW_INSTANCE_DO_NOT_USE.unload();
        DeviceManager.RAW_INSTANCE_DO_NOT_USE.unload();
        this.world = null;
        this.lastTickFuture = null;
    }

    public void onServerTick(ServerTickEvent event) 
    {
        if(this.isRunning)
        {
           
            if(lastTickFuture == null || lastTickFuture.isDone())
            {

                int newLastSimTick = (int) (world.getWorldTime() + this.worldTickOffset);

                // Simulation clock can't move backwards.
                // NB: don't need CAS because only ever changed by game thread in this method
                if(newLastSimTick > lastSimTick)
                {
                    // if((newLastSimTick & 31) == 31) HardScience.log.info("changing lastSimTick, old=" + lastSimTick + ", new=" + newLastSimTick);
                    this.isDirty = true;
                    this.lastSimTick = newLastSimTick;          
                }
                else
                {
                    // world clock has gone backwards or paused, so readjust offset
                    this.lastSimTick++;
                    this.worldTickOffset = this.lastSimTick - world.getWorldTime();
                    this.setSaveDirty(true);
                    if(isClockSetbackNotificationNeeded)
                    {
                        Log.warn("World clock appears to have run backwards.  Simulation clock offset was adjusted to compensate.");
                        Log.warn("Next tick according to world was " + newLastSimTick + ", using " + this.lastSimTick + " instead.");
                        Log.warn("If this recurs, simulation clock will be similarly adjusted without notification.");
                        isClockSetbackNotificationNeeded = false;
                    }
                }

                if(!Simulator.this.tickables.isEmpty())
                {
                    for(ISimulationTickable tickable : Simulator.this.tickables)
                    {
                        tickable.doOnTick();
                    }
                }

                lastTickFuture = CONTROL_THREAD.submit(this.offTickFrame);
            }
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        Log.info("Simulator read from NBT");
        this.lastSimTick = nbt.getInteger(ModNBTTag.SIMULATION_LAST_TICK);
        this.worldTickOffset = nbt.getLong(ModNBTTag.SIMULATION_WORLD_TICK_OFFSET);
    }

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        Log.info("saving simulation state");
        nbt.setInteger(ModNBTTag.SIMULATION_LAST_TICK, lastSimTick);
        nbt.setLong(ModNBTTag.SIMULATION_WORLD_TICK_OFFSET, worldTickOffset);
    }

    public World getWorld() { return this.world; }
    public int getTick() { return this.lastSimTick; }

    public VolcanoManager volcanoManager() { return this.volcanoManager; }
    public LavaSimulator lavaSimulator() { return this.lavaSimulator; }

    // Frame execution logic
    Runnable offTickFrame = new Runnable()
    {
        @Override
        public void run()
        {
            if(!Simulator.this.tickables.isEmpty())
            {
                for(ISimulationTickable tickable : Simulator.this.tickables)
                {
                    try
                    {
                        tickable.doOffTick();
                    }
                    catch(Exception e)
                    {
                        Log.error("Exception during simulator off-tick processing", e);
                    }
                }
            }
        }    
    };

    // CHUNK LOADING START UP HANDLERS

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world)
    {
        // For volcanos we re-force chunks when simulation loaded
        // or when activation changes. Should get no tickets.
        ;
    }

    @Override
    public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount)
    {
        // For volcanos we re-force chunks when simulation loaded
        // or when activation changes. Dispose of all tickets.
        List<ForgeChunkManager.Ticket> validTickets = Lists.newArrayList();
        return validTickets;
    }

    @Override
    public boolean isSaveDirty()
    {
        return this.isDirty;
    }

    @Override
    public void setSaveDirty(boolean isDirty)
    {
        this.isDirty = true;

    }

    @Override
    public String tagName()
    {
        return ModNBTTag.SIMULATOR;
    }

    public static Simulator instance()
    {
        loadSimulatorIfNotLoaded();
        return RAW_INSTANCE_DO_NOT_USE;
        
    }

    /**
     * Simulator is lazily loaded because needs world to be loaded
     * but is also referenced by tile entities during chunk load.
     * No forge event that lets us load after worlds loaded but
     * before chunk loading, so using start reference as the trigger.
     */
    public static void loadSimulatorIfNotLoaded()
    {
        // If called from world thread before loaded,
        // want to block and complete loading before return.
        // However, if the load process (running on the calling
        // thread) makes a re-entrant call we want to return the 
        // instance so that loading can progress.
        
        if(RAW_INSTANCE_DO_NOT_USE.isRunning) return;
        synchronized(RAW_INSTANCE_DO_NOT_USE)
        {
            RAW_INSTANCE_DO_NOT_USE.isRunning = true;
            RAW_INSTANCE_DO_NOT_USE.start();
        }
    }
}
