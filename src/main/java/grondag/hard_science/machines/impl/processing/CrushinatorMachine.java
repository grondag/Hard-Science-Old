package grondag.hard_science.machines.impl.processing;

import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.crafting.BulkItemInput;
import grondag.hard_science.crafting.processing.CrushinatorRecipe2;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.machines.matbuffer.BufferManager2;
import grondag.hard_science.machines.matbuffer.BulkBufferPurpose;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.matter.BulkItem;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.BulkContainer;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.ItemContainer;
import grondag.hard_science.simulator.storage.PowerContainer;
import net.minecraft.item.ItemStack;

public class CrushinatorMachine extends AbstractSimpleMachine
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * 
     * Estimated number of crusher cycles that need to run to catch
     * up with demand in the given domain based on input material
     * available.
     */
    public static int estimatedBacklogDepth(Domain domain)
    {
        int result = 0;
        for(ItemResourceWithQuantity d : demands(domain))
        {
            result += (int)d.getQuantity();
        }
        return result;
    }
    
    public static ImmutableList<ItemResourceWithQuantity> demands(Domain domain)
    {
        return null;
    }
    
    private static final ImmutableList<Predicate<?>> imports;
    
    static
    {
        ImmutableList.Builder<Predicate<?>> importBuilder = ImmutableList.builder();
        for(BulkResource b: CrushinatorRecipe2.allInputs())
        {
            for(BulkItemInput i : BulkItemInput.inputsForResource(b))
            {
                importBuilder.add(i.ingredient());
            }
        }
        imports = importBuilder.build();
    }
    
    private static final long BULK_BUFFER_SIZE = VolumeUnits.KILOLITER.nL;
    
    //FIXME: make configurable
    private static final int WATTS_IDLE = 20;
    private static final int WATTS_FABRICATION = 1200;
    private static final int JOULES_PER_TICK_IDLE = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_IDLE));
    private static final int JOULES_PER_TICK_FABRICATING = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_FABRICATION));
//    private static final long TICKS_PER_FULL_BLOCK = 40;

    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    // these will be populated during construction 
    // when super constructor calls createBuffer method
    private ItemContainer itemInput;
    private ItemContainer itemOutput;
    private BulkContainer bulkInput;
    private BulkContainer bulkOutput;
    
    public CrushinatorMachine()
    {
        super();
        this.statusState.setHasBacklog(true);
    }
    
    @Override
    protected BufferManager2 createBufferManager()
    {
        this.itemInput = new ItemContainer(this, ContainerUsage.BUFFER_IN);
        this.itemInput.setCapacity(64);
        this.itemInput.setRegulator(new ThroughputRegulator.Tracking());
        this.itemInput.setContentPredicate(new Predicate<IResource<StorageTypeStack>>() 
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean test(IResource<StorageTypeStack> t)
            {
                ItemStack stack = ((ItemResource)t).sampleItemStack();
                for(Predicate p : imports)
                {
                    if(p.test(stack)) return true;
                }
                return false;
            }
        });
        
        this.itemOutput = new ItemContainer(this, ContainerUsage.BUFFER_OUT);
        this.itemOutput.setRegulator(new ThroughputRegulator.Tracking());
        this.itemOutput.setCapacity(64);
        this.itemOutput.setContentPredicate(new Predicate<IResource<StorageTypeStack>>() 
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean test(IResource<StorageTypeStack> t)
            {
                ItemStack stack = ((ItemResource)t).sampleItemStack();
                if(stack.getItem() instanceof BulkItem)
                {
                    BulkResource b = ((BulkItem)stack.getItem()).matter;
                    if(!CrushinatorRecipe2.getForOutput(b).isEmpty()) return true;
                }
                
                // allow inputs to be moved to export if operation is aborted
                for(Predicate p : imports)
                {
                    if(p.test(stack)) return true;
                }
                
                return false;
            }
        });
        
        this.bulkInput = new BulkContainer(this, BulkBufferPurpose.PRIMARY_INPUT);
        this.bulkInput.setCapacity(BULK_BUFFER_SIZE);
        
        this.bulkOutput = new BulkContainer(this, BulkBufferPurpose.PRIMARY_OUTPUT);
        this.bulkOutput.setCapacity(BULK_BUFFER_SIZE);
        
        return new BufferManager2(this, itemInput, itemOutput, bulkInput, bulkOutput);
       
    }

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
    
    private void clearState()
    {
        this.getControlState().clearJobTicks();
        this.getControlState().setRecipe(null);
        this.getControlState().setMachineState(MachineState.IDLE);
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
    
    private void outputFabricatedBlock()
    {
        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
        {
            this.blamePowerSupply();
            return;
        }
        
        // If we got to this point, we have a fabricated block.
        // Put it in domain storage if there is room for it
        // and tag the task with the location of the bulkResource so drone can find it
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
   
    /**
     * Machine will first try to process whatever is in the input buffer.
     * If the input buffer is empty and the machine is in automatic mode, 
     * will attempt to maintain minimum stocking levels if any are below the mark.
     * 
     * Crushing is a multi-step process.  Block -> Cobble -> Gravel -> Sand.
     * Stocking levels for Cobble and Gravel may be zero.
     * If this is the case, and machine cannot find the input for the demand
     * it is trying to satisfy, will work back to whatever bulkResource is available
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
    public void doOffTick()
    {
        super.doOffTick();
        
        //TODO: if the machine is off, export any input items
        
        if(!this.isOn()) return;
        
        if(this.getDomain() == null) return;
        
        if((Simulator.instance().getTick() & 0x1F) == 0x1F)
        {
            this.setCurrentBacklog(estimatedBacklogDepth(this.getDomain()));
        }
        
        // if we don't have power to do basic control functions
        // then abort and blame power
        if(!this.provideAllPowerOrBlameSupply(JOULES_PER_TICK_IDLE)) return;
        
        // if we don't have anything in item input buffer
        // and no request is pending then
        // (and the compute countdown timer is zero?)
        // then look for something so that we don't idle
        if(this.itemInput.isEmpty())
        {
            // look for bulk resources that are in demand
            // for those in demand, take the highest priority
        }
        
        // if there is something in item input and 
        // the bulk input buffer is empty, load the bulk input buffer
        // if we don't have power to do this then abort and blame power
        
        // if there is something in the bulk input buffer then
        // determine max output based on available space in
        // output buffers
        
        // process the bulk input buffer, up to the smaller of max output
        // and available material in the input buffer
        // also limit by power
        // if we don't have power to start or run out of power
        // before we finish then stop and blame power
        
        // add the material to the bulk output buffer
        // if at any point it becomes full, output a bulk container
        // and put the remainder in the (now empty) output buffer
        // if we don't have power to do this then abort and blame power
        
        // request that the output be put into primary storage if
        // an off-device storage location is available
        // if we don't have power to do this then abort and blame power
        
        switch(this.getControlState().getMachineState())
        {
        case FABRICATING:
            this.progressFabrication();
            break;
            
        default:
            this.searchForWork();
            break;
        }
    }
}
