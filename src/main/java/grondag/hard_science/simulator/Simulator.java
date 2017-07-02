package grondag.hard_science.simulator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Lists;

import grondag.hard_science.Log;
import grondag.hard_science.feature.volcano.lava.simulator.LavaSimulator;
import grondag.hard_science.feature.volcano.lava.simulator.VolcanoManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;


/**
 * Events are processed from a queue in the order they arrive.
 * 
 * World events are always added to the queue as soon as they arrive.
 * 
 * Simulation ticks are generated as the world clock advances
 * at the rate of one simulation tick per world tick.
 * 
 * Simulation ticks are added to the queue by a special task 
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
public class Simulator extends SimulationNode implements ForgeChunkManager.OrderedLoadingCallback
{

	protected Simulator()
    {
        super(NodeRoots.SIMULATION.ordinal());
    }

    public static final Simulator INSTANCE = new Simulator();
	
    private VolcanoManager volcanoManager;
    
    private LavaSimulator lavaSimulator;
    
	public static ExecutorService executor;
	private static ExecutorService controlThread;
	
	private Future<?> lastTickFuture = null;
    
    /** used for world time */
    private World world;
    
    private volatile boolean isRunning = false;
    public boolean isRunning() { return isRunning; }
    
 //   private AtomicInteger nextNodeID = new AtomicInteger(NodeRoots.FIRST_NORMAL_NODE_ID);
//    private static final String TAG_NEXT_NODE_ID = "nxid";
	
	
    /** 
     * Set to worldTickOffset + lastWorldTick at end of server tick.
     * If equal to currentSimTick, means simulation is caught up with world ticks.
     */
    private volatile int lastSimTick = 0;
    private static final String TAG_LAST_SIM_TICK = "lstk";

    /** worldTickOffset + lastWorldTick = max value of current simulation tick.
	 * Updated on server post tick, *after* all world tick events should be submitted.
	 */
	private volatile long worldTickOffset = 0; 
    private static final String TAG_WORLD_TICK_OFFSET= "wtos";

    static
    {
        // would a bounded backing queue be faster due to cache coherence
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        controlThread = Executors.newSingleThreadExecutor();

    }

	// Main control
	public void start()  
	{
        Log.info("starting sim");
	    synchronized(this)
	    {
	        // we're going to assume for now that all the dimensions we care about are using the overworld clock
	        this.world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
	        this.volcanoManager = new VolcanoManager();
	        this.lavaSimulator = new LavaSimulator(this.world);
	        
            if(PersistenceManager.loadNode(world, this))
            {
                if(!PersistenceManager.loadNode(world, this.volcanoManager))
                {
                    Log.info("Volcano manager data not found - recreating.  Some world state may be lost.");
                    PersistenceManager.registerNode(world, this.volcanoManager);
                }
                
                if(!PersistenceManager.loadNode(world, this.lavaSimulator))
                {
                    Log.info("Lava simulator data not found - recreating.  Some world state may be lost.");
                    PersistenceManager.registerNode(world, this.lavaSimulator);
                }
            }
            else
    	    {
                Log.info("creating sim");
                // Not found, assume new game and new simulation
    	        this.worldTickOffset = -world.getWorldTime();
    	        this.lastSimTick = 0;
    	        this.setSaveDirty(true);
    	        PersistenceManager.registerNode(world, this);
    	        PersistenceManager.registerNode(world, this.volcanoManager);
    	        PersistenceManager.registerNode(world, this.lavaSimulator);

    	    }

    		this.isRunning = true;
            Log.info("starting first frame");
//    		executor.execute(new FrameStarter());
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
		
	    this.world = null;
	    this.lastTickFuture = null;
	}
	
    public void onServerTick(ServerTickEvent event) 
    {
        
        // thought it might offer more determinism if we run after block/entity ticks
        if(event.phase == TickEvent.Phase.END && this.isRunning)
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
                    Log.warn("World clock appears to have run backwards.  Simulation clock offset was adjusted to compensate.");
                    Log.warn("Next tick according to world was " + newLastSimTick + ", using " + this.lastSimTick + " instead.");
                }
                
                this.volcanoManager.doOnTick();
                this.lavaSimulator.doOnTick();
                
                lastTickFuture = controlThread.submit(this.offTickFrame);
            }
        }
    }

    
//    /**
//     * Call to claim a new ID for all created nodes.
//     * Node #0 is reserved for the simulation object itself.
//     * Claiming a node ID does not cause it to be ticked or persisted.
//     */
//    private int getNewNodeID()
//    {
//        this.setSaveDirty(true);
//        return this.nextNodeID.getAndIncrement();
//    }
 
	// Node interface implementation
	

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        Log.info("Simulator read from NBT");
        this.lastSimTick = nbt.getInteger(TAG_LAST_SIM_TICK);
        this.worldTickOffset = nbt.getLong(TAG_WORLD_TICK_OFFSET);
    }

    @Override
    public boolean isSaveDirty()
    {
        return super.isSaveDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        Log.info("saving simulation state");
        nbt.setInteger(TAG_LAST_SIM_TICK, lastSimTick);
        nbt.setLong(TAG_WORLD_TICK_OFFSET, worldTickOffset);
    }
    
    public World getWorld() { return this.world; }
    public int getTick() { return this.lastSimTick; }
    
    public VolcanoManager getVolcanoManager() { return this.volcanoManager; }
    public LavaSimulator getFluidTracker() { return this.lavaSimulator; }

    // Frame execution logic
    Runnable offTickFrame = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                Simulator.this.volcanoManager.doOffTick();
                Simulator.this.lavaSimulator.doOffTick();
            }
            catch(Exception e)
            {
                Log.error("Exception during simulator off-tick processing", e);
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

 
}
