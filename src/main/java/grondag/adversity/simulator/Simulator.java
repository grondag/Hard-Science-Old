package grondag.adversity.simulator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import grondag.adversity.Adversity;
import grondag.adversity.simulator.base.SimulationNode;
import grondag.adversity.simulator.base.NodeRoots;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
        this.volcanoManager = new VolcanoManager(this.taskCounter);
    }

    public static final Simulator instance = new Simulator();
	
    private VolcanoManager volcanoManager;
    
	private static ExecutorService executor;
    
    /** used for world time */
    private World world;
    
    private volatile boolean isRunning = false;
    
    private final TaskCounter taskCounter = new TaskCounter();

 //   private AtomicInteger nextNodeID = new AtomicInteger(NodeRoots.FIRST_NORMAL_NODE_ID);
    private static final String TAG_NEXT_NODE_ID = "nxid";
	
    /** Last tick that is executing or has completed.
     * If = lastSimTick, means simulation is caught up or catching up with most recent activity.
     */
	private AtomicInteger currentSimTick = new AtomicInteger(0);
    private static final String TAG_CURRENT_SIM_TICK = "cstk";
	
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


	// Main control
	public void start()
	{
        Adversity.log.info("starting sim");
	    synchronized(this)
	    {
	        // we're going to assume for now that all the dimensions we care about are using the overworld clock
	        this.world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
	        
            if(!PersistenceManager.loadNode(world, this))
    	    {
                Adversity.log.info("creating sim");
                // Not found, assume new game and new simulation
    	        this.worldTickOffset = -world.getWorldTime();
    	        this.setSaveDirty(true);
    	        PersistenceManager.registerNode(world, this);

    	    }
    		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    		this.isRunning = true;
            Adversity.log.info("starting first frame");
    		executor.execute(new FrameStarter());
	    }
	}
	
    /** 
     * Called from ServerStopping event.
     * Should be no more ticks after that.
     */
	public synchronized void stop()
	{
        Adversity.log.info("stopping server");
		this.isRunning = false;
        
		// wait for simulation to catch up
		while(this.currentSimTick.get() < this.lastSimTick)
		{
		    try
            {
	            Adversity.log.info("waiting for catch up");
                this.wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
		}
        Adversity.log.info("waiting for last frame task completion");
		this.taskCounter.waitUntilAllTasksComplete();
        Adversity.log.info("shutting down thread pool");
	    executor.shutdown();
	    this.world = null;
	}
	
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {

        
        // thought it might offer more determinism if we run after block/entity ticks
        if(event.phase == TickEvent.Phase.END && this.isRunning)
        {

            int newLastSimTick = (int) (world.getWorldTime() + this.worldTickOffset);

            // Simulation clock can't move backwards.
            // NB: don't need CAS because only ever changed by game thread in this method
            if(newLastSimTick > lastSimTick)
            {
               // if((newLastSimTick & 31) == 31) Adversity.log.info("changing lastSimTick, old=" + lastSimTick + ", new=" + newLastSimTick);
                this.isDirty = true;
                this.lastSimTick = newLastSimTick;
            }
            
            // accesses world, so needs to run on server thread
            volcanoManager.updateChunkLoading();
        }
        

    }

    /**
     * Increments active task count.
     * Call whenever adding another task to the queue.
     * Notify is for stop method, which may be waiting for count to catch up.
     */
    private void incrementSimTick() {
        setSaveDirty(true);
        currentSimTick.incrementAndGet();
        synchronized(this)
        {
            this.notifyAll();
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
        this.currentSimTick.set(nbt.getInteger(TAG_CURRENT_SIM_TICK));
        this.lastSimTick = nbt.getInteger(TAG_LAST_SIM_TICK);
        this.worldTickOffset = nbt.getLong(TAG_WORLD_TICK_OFFSET);
        
        volcanoManager.readFromNBT(nbt);
    }

    @Override
    public boolean isSaveDirty()
    {
        return super.isSaveDirty() || volcanoManager.isSaveDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        Adversity.log.info("saving simulation state");
        nbt.setInteger(TAG_CURRENT_SIM_TICK, currentSimTick.get());
        nbt.setInteger(TAG_LAST_SIM_TICK, lastSimTick);
        nbt.setLong(TAG_WORLD_TICK_OFFSET, worldTickOffset);
        volcanoManager.writeToNBT(nbt);
    }
    
    public World getWorld() { return this.world; }
    public int getCurrentSimTick() { return this.currentSimTick.get(); }
    
    public VolcanoManager getVolcanoManager() { return this.volcanoManager; }

    // Frame execution logic

    private class FrameStarter implements Runnable
    {
        /** 
         * Sets up a new simulation frame.  This includes:
         * 
         * 1) Wait for all tasks from previous simulation frame to finish.
         * Known by checking active activeTaskCount
         * 
         * 2) Check for world ticks to process.
         * Known by comparing current sim tick with last sim tick.
         * If no ticks are available, wait for world clock to advance,
         * unless the simulation is shutting down.  In that case, exit.
         * 
         * 3) Generate and execute node tasks for all nodes that activate in this tick.
         * Increment activeTaskCount for each one.
         * 
         * 4) Advance the frame.
         * 
         * 5) Create and execute a new FrameStarter for the frame after this.
         * Do NOT increment activeTaskCount.  
         * The new FrameStarter will wait for tasks from this frame to complete.
         * It cannot wait for itself to complete.
         */
        @Override
        public void run()
        {
            //if((currentSimTick.get() & 31) == 31) Adversity.log.info("starting frame with currentSimTick=" + currentSimTick.get());
            // wait for any previous frames to finish
            taskCounter.waitUntilAllTasksComplete();
            
            // wait for world tick to advance or for the server to stop
            while(isRunning)
            {
                if((currentSimTick.get() == lastSimTick))
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    // do all the things!
                    this.startNodeTasks();
                    
                    // advance the frame counter
                    incrementSimTick();
                    
                    // start the next frame
                    executor.execute(this);
                    
                    return;
                }
            }
        }
        
        private void startNodeTasks()
        {
            /**
             * Things to add:
             * Adversary thinking
             * Mining manager
             * Power transfer manager
             * Material/fluid transfer manager
             * Inventory manager/monitor
             * Redstone manager
             * Crafting manager
             * Material processing
             * Meteor manager
             * Storm manager
             * Computer processing
             * Plant growth
             * Plant production
             * 
             * Hypermaterial Manager? - probably best to leave this world-side
             */
            if((currentSimTick.get() & 1023) == 1023)
            {
                executor.execute(volcanoManager);
            }
        }
 
        
    }
    
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
