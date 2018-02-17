package grondag.hard_science.machines.impl.processing;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.jobs.TaskType;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;
import net.minecraft.entity.player.EntityPlayerMP;

public class CrushinatorMachine extends AbstractSimpleMachine
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    //FIXME: make configurable
    private static final int WATTS_IDLE = 20;
    private static final int WATTS_FABRICATION = 1200;
    private static final int JOULES_PER_TICK_IDLE = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_IDLE));
    private static final int JOULES_PER_TICK_FABRICATING = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_FABRICATION));
//    private static final long TICKS_PER_FULL_BLOCK = 40;

    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    
    public CrushinatorMachine()
    {
        super();
        this.statusState.setHasBacklog(true);
    }
    
//    @Override
//    protected BufferManager2 createBufferManager()
//    {
//        return new BufferManager2(BUFFER_SPECS);
//    }

    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        PowerContainer input = new PowerContainer(this, ContainerUsage.BUFFER_IN);
        input.configure(VolumeUnits.MILLILITER.nL * 10L, BatteryChemistry.CAPACITOR);
        
        return new DeviceEnergyManager(
                this,
                null, 
                input,
                null);
    }
    
    
    @Override
    public boolean togglePower(EntityPlayerMP player)
    {
        boolean result = super.togglePower(player);
        if(result && !this.isOn())
        {
            this.abandonTaskInProgress();
        }
        return result;
    }
    
    private void abandonTaskInProgress()
    {
        this.getControlState().clearJobTicks();
        this.getControlState().setModelState(null);
        this.getControlState().setTargetPos(null);
        this.getControlState().setMachineState(MachineState.THINKING);
        this.setDirty();
    }
    
    
    private void progressFabrication()
    {
        if(this.energyManager().provideEnergy(JOULES_PER_TICK_FABRICATING, false, false) != 0)
        {
            if(this.getControlState().progressJob((short) 1))
            {
                this.getControlState().clearJobTicks();
                this.getControlState().setMachineState(MachineState.TRANSPORTING);
            }
            this.setDirty();
        }
        else
        {
            this.blamePowerSupply();
        }
    }
    
    /**
     * Unlike inputs, outputs need to go into domain-managed storage
     * so that drones can locate them for pickup. If no domain storage
     * available, then stall.
     */
    private void outputFabricatedBlock()
    {
        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
        {
            this.blamePowerSupply();
            return;
        }
        
        // If we got to this point, we have a fabricated block.
        // Put it in domain storage if there is room for it
        // and tag the task with the location of the resource so drone can find it
        // otherwise stall
        if(this.getDomain() == null) return;
        
//        ItemStack stack = this.task().procurementTask().getStack().copy();
//        stack.setItemDamage(this.getControlState().getMeta());
//        PlacementItem.setStackLightValue(stack, this.getControlState().getLightValue());
//        PlacementItem.setStackSubstance(stack, this.getControlState().getSubstance());
//        PlacementItem.setStackModelState(stack, this.getControlState().getModelState());
//        
//        ItemResource res = ItemResource.fromStack(stack);
//        
//        //TODO: pass in procurement request
//        List<IResourceContainer<StorageTypeStack>> locations = this.getDomain().itemStorage.findSpaceFor(res, this);
//        
//        if(locations.isEmpty()) return;
//        
//       // this all needs to happen in any case
//          this.task.procurementTask().setStack(stack);
//          this.task.complete();
//          this.task = null;
//          this.taskID = IIdentified.UNASSIGNED_ID;
//          this.getControlState().setModelState(null);
//          this.getControlState().setTargetPos(null);
//          this.getControlState().setMachineState(MachineState.THINKING);
//          this.setDirty();
//          return;
    }
   

    @Override
    public void onDisconnect()
    {
        this.abandonTaskInProgress();
        super.onDisconnect();
    }

    /**
     * Machine will first try to process whatever is in the input buffer.
     * If the input buffer is empty and the machine is in automatic mode, 
     * will attempt to maintain minimum stocking levels if any are below the mark.
     * 
     * Crushing is a multi-step process.  Block -> Cobble -> Gravel -> Sand.
     * Stocking levels for Cobble and Gravel may be zero.
     * If this is the case, and machine cannot find the input for the demand
     * it is trying to satisfy, will work back to whatever resource is available
     * and process it.
     */
    private void searchForWork()
    {
        
        
        if(this.getDomain() == null) return;
        
        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
        {
            this.blamePowerSupply();
            return;
        }
        
        // because we consumed power
        this.setDirty();
        this.markTEPlayerUpdateDirty(false);
        
//        // find a job
//        if(this.taskSearch == null)
//        {
//            this.taskSearch = this.getDomain().jobManager.claimReadyWork(TaskType.BLOCK_FABRICATION, null);
//            return;
//        }
//        else if(this.taskSearch.isDone())
//        {
//            try
//            {
//                this.task = (BlockFabricationTask) this.taskSearch.get();
//                if(this.task != null)
//                {
//                    this.taskID = this.task.getId();
//                    this.setDirty();
//                }
//            }
//            catch(Exception e)
//            {
//                this.task = null;
//                this.taskID = IIdentified.UNASSIGNED_ID;
//            }
//            this.taskSearch = null;
//        }
//        
//        if(this.task == null) return;
//        
//        
//        
//        if(substance == null)
//        {
//            this.task.abandon();
//        }
//        else
//        {
//            // setup job duration
//            this.getControlState().startJobTicks((short) (this.getBufferManager().demandManager().totalDemandNanoLiters() * TICKS_PER_FULL_BLOCK / VolumeUnits.KILOLITER.nL));
//
//            // consume resources
//            this.getBufferManager().demandManager().consumeAllDemandsAndClear();
//
//            // save placement info
//            this.getControlState().setModelState(PlacementItem.getStackModelState(stack));
//            this.getControlState().setTargetPos(task.procurementTask().pos());
//            this.getControlState().setLightValue(PlacementItem.getStackLightValue(stack));
//            this.getControlState().setSubstance(substance);
//            this.getControlState().setMeta(stack.getMetadata());
//            this.getControlState().setMachineState(MachineState.FABRICATING);
//            
//            // we want to send an immediate update when job starts
//            this.markTEPlayerUpdateDirty(true);
//        }
    }

    @Override
    public void updateMachine(long tick)
    {
        super.updateMachine(tick);
        
        if(!this.isOn()) return;
        
        if(this.getDomain() == null) return;
        
        if((tick & 0x1F) == 0x1F)
        {
            this.setCurrentBacklog(this.getDomain().jobManager.getQueueDepth(TaskType.BLOCK_FABRICATION));
        }
        
        switch(this.getControlState().getMachineState())
        {
        case FABRICATING:
            this.progressFabrication();
            break;
            
        case TRANSPORTING:
            this.outputFabricatedBlock();
            break;
            
        default:
            this.searchForWork();
            break;
            
        }
    }
}
