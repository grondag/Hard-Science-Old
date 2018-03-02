package grondag.hard_science.machines.impl.building;

import grondag.hard_science.library.serialization.ModNBTTag;
import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.PolyethyleneFuelCell;
import grondag.hard_science.machines.matbuffer.BufferManager2;
import grondag.hard_science.machines.matbuffer.VolumetricBufferSpec;
import grondag.hard_science.machines.matbuffer.VolumetricIngredient;
import grondag.hard_science.machines.matbuffer.VolumetricIngredientList;
import grondag.hard_science.machines.support.MachineControlState.MachineState;
import grondag.hard_science.matter.MatterPackaging;
import grondag.hard_science.matter.MatterUnits;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.domain.DomainManager;
import grondag.hard_science.simulator.jobs.tasks.BlockFabricationTask;
import grondag.hard_science.simulator.persistence.IIdentified;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;
import net.minecraft.entity.player.EntityPlayerMP;

public class BlockFabricatorMachine extends AbstractSimpleMachine
{
    ////////////////////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    //FIXME: make configurable
//    private static final int WATTS_IDLE = 20;
//    private static final int WATTS_FABRICATION = 1200;
//    private static final int JOULES_PER_TICK_IDLE = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_IDLE));
//    private static final int JOULES_PER_TICK_FABRICATING = Math.round(MachinePower.wattsToJoulesPerTick(WATTS_FABRICATION));
//    private static final long TICKS_PER_FULL_BLOCK = 40;

    private static final VolumetricIngredientList HDPE_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.HDPE);
    
    private static final VolumetricIngredientList FILLER_INGREDIENTS = new VolumetricIngredientList(
            MatterPackaging.RAW_MINERAL_DUST,
            MatterPackaging.DEPLETED_MINERAL_DUST,
            new VolumetricIngredient("sand", MatterUnits.nL_ONE_BLOCK),
            new VolumetricIngredient("red_sand", MatterUnits.nL_ONE_BLOCK));
    
    private static final VolumetricIngredientList RESIN_A_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.CONSTRUCTION_RESIN_A);
    
    private static final VolumetricIngredientList RESIN_B_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.CONSTRUCTION_RESIN_B);
    
    private static final VolumetricIngredientList NANOLIGHT_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.NANO_LIGHTS);
    
    private static final VolumetricIngredientList CYAN_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.DYE_CYAN);
    
    private static final VolumetricIngredientList MAGENTA_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.DYE_MAGENTA);
    
    private static final VolumetricIngredientList YELLOW_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.DYE_YELLOW);
    
    private static final VolumetricIngredientList TiO2_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.TIO2);
    
//    private static final VolumetricIngredientList CARBON_INGREDIENTS = new VolumetricIngredientList(MatterPackaging.CARBON_BLACK);

    // so TESR knows which buffer to render for each gauge
    public static final int BUFFER_INDEX_HDPE = 0;
    public static final int BUFFER_INDEX_FILLER = BUFFER_INDEX_HDPE + 1;
    public static final int BUFFER_INDEX_RESIN_A = BUFFER_INDEX_FILLER + 1;
    public static final int BUFFER_INDEX_RESIN_B = BUFFER_INDEX_RESIN_A + 1;
    public static final int BUFFER_INDEX_NANOLIGHT = BUFFER_INDEX_RESIN_B + 1;
    public static final int BUFFER_INDEX_CYAN = BUFFER_INDEX_NANOLIGHT + 1;
    public static final int BUFFER_INDEX_MAGENTA = BUFFER_INDEX_CYAN + 1;
    public static final int BUFFER_INDEX_YELLOW = BUFFER_INDEX_MAGENTA + 1;
    public static final int BUFFER_INDEX_TIO2 = BUFFER_INDEX_YELLOW + 1;
    
    public static final VolumetricBufferSpec[] BUFFER_SPECS = new VolumetricBufferSpec[BUFFER_INDEX_TIO2 + 1];
    
    static
    {
        BUFFER_SPECS[BUFFER_INDEX_HDPE] = new VolumetricBufferSpec(HDPE_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_HDPE, "hdpe");
        BUFFER_SPECS[BUFFER_INDEX_FILLER] = new VolumetricBufferSpec(FILLER_INGREDIENTS, MatterUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_MINERAL_FILLER, "filler");
        BUFFER_SPECS[BUFFER_INDEX_RESIN_A] = new VolumetricBufferSpec(RESIN_A_INGREDIENTS, MatterUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_RESIN_A, "resin_a");
        BUFFER_SPECS[BUFFER_INDEX_RESIN_B] = new VolumetricBufferSpec(RESIN_B_INGREDIENTS, MatterUnits.nL_FULL_STACK_OF_BLOCKS_nL, ModNBTTag.MATERIAL_RESIN_B, "resin_b");
        BUFFER_SPECS[BUFFER_INDEX_NANOLIGHT] = new VolumetricBufferSpec(NANOLIGHT_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_NANO_LIGHTS, "nanolights");
        BUFFER_SPECS[BUFFER_INDEX_CYAN] = new VolumetricBufferSpec(CYAN_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_DYE_CYAN, "cyan");
        BUFFER_SPECS[BUFFER_INDEX_MAGENTA] = new VolumetricBufferSpec(MAGENTA_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_DYE_MAGENTA, "magenta");
        BUFFER_SPECS[BUFFER_INDEX_YELLOW] = new VolumetricBufferSpec(YELLOW_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_DYE_YELLOW, "yellow");
        BUFFER_SPECS[BUFFER_INDEX_TIO2] = new VolumetricBufferSpec(TiO2_INGREDIENTS, MatterUnits.nL_TWO_BLOCKS, ModNBTTag.MATERIAL_TiO2, "tio2");
    }
    
    ////////////////////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ////////////////////////////////////////////////////////////////////////
    
    // job search - not persisted
//    private Future<AbstractTask> taskSearch = null;
    
    // current job id - persisted
    int taskID = IIdentified.UNASSIGNED_ID;
    
    // current task - lazy lookup, don't use directly
    BlockFabricationTask task = null;
    
    // Buffer setup - all persisted
//    private final BufferDelegate2 bufferFiller;
//    private final BufferDelegate2 bufferResinA;
//    private final BufferDelegate2 bufferResinB;
//    private final BufferDelegate2 bufferNanoLights;
//    private final BufferDelegate2 bufferCyan;
//    private final BufferDelegate2 bufferMagenta;
//    private final BufferDelegate2 bufferYellow;
//    private final BufferDelegate2 bufferTiO2;   
    
    public BlockFabricatorMachine()
    {
        super();
        // note that order has to match array declaration
//        BufferManager2 bufferManager = this.getBufferManager();
//        this.bufferFiller = bufferManager.getBuffer(BUFFER_INDEX_FILLER);
//        this.bufferResinA = bufferManager.getBuffer(BUFFER_INDEX_RESIN_A);
//        this.bufferResinB = bufferManager.getBuffer(BUFFER_INDEX_RESIN_B);
//        this.bufferNanoLights = bufferManager.getBuffer(BUFFER_INDEX_NANOLIGHT);
//        this.bufferCyan = bufferManager.getBuffer(BUFFER_INDEX_CYAN);
//        this.bufferMagenta = bufferManager.getBuffer(BUFFER_INDEX_MAGENTA);
//        this.bufferYellow = bufferManager.getBuffer(BUFFER_INDEX_YELLOW);
//        this.bufferTiO2 = bufferManager.getBuffer(BUFFER_INDEX_TIO2);
        this.statusState.setHasBacklog(true);
    }
    
    @Override
    protected BufferManager2 createBufferManager()
    {
        return new BufferManager2(
                this, 
                64L, 
                StorageType.ITEM.MATCH_ANY, 
                64L, 
                VolumeUnits.KILOLITER.nL * 64L, 
                StorageType.FLUID.MATCH_ANY, 
                64L);
    }

    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        PowerContainer output = new PowerContainer(this, ContainerUsage.PUBLIC_BUFFER_OUT);
        output.configure(VolumeUnits.LITER.nL, BatteryChemistry.SILICON);
        
        PowerContainer input = new PowerContainer(this, ContainerUsage.PRIVATE_BUFFER_IN);
        input.configure(VolumeUnits.MILLILITER.nL * 10L, BatteryChemistry.CAPACITOR);
        
        return new DeviceEnergyManager(
                this,
                PolyethyleneFuelCell.basic_1kw(this), 
                input,
                output);
    }
    
    public BlockFabricationTask task()
    {
        if(this.task == null && this.taskID != IIdentified.UNASSIGNED_ID)
        {
            this.task = (BlockFabricationTask) DomainManager.taskFromId(this.taskID);
        }
        return this.task;
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
        if(this.task() != null)
        {
            this.task.abandon();
            this.task = null;
            this.taskID = IIdentified.UNASSIGNED_ID;
        }
        this.getControlState().clearJobTicks();
        this.getControlState().setModelState(null);
        this.getControlState().setTargetPos(null);
        this.getControlState().setMachineState(MachineState.THINKING);
        this.setDirty();
    }
    
//    /**
//     * Call to confirm still have an active task.
//     * Returns false if no task or task abandoned.
//     */
//    private boolean isTaskAbandoned()
//    {
//        if(this.task() == null || this.task().getStatus() != RequestStatus.ACTIVE)
//        {
//            this.abandonTaskInProgress();
//            return true;
//        }
//        return false;
//    }
    
//    private void progressFabrication()
//    {
//        if(this.isTaskAbandoned()) return;
//        
//        if(this.energyManager().provideEnergy(JOULES_PER_TICK_FABRICATING, false, false) != 0)
//        {
//            if(this.getControlState().progressJob((short) 1))
//            {
//                this.getControlState().clearJobTicks();
//                this.getControlState().setMachineState(MachineState.TRANSPORTING);
//            }
//            this.setDirty();
//        }
//        else
//        {
//            this.blamePowerSupply();
//        }
//    }
    
//    /**
//     * Unlike inputs, outputs need to go into domain-managed storage
//     * so that drones can locate them for pickup. If no domain storage
//     * available, then stall.
//     */
//    private void outputFabricatedBlock()
//    {
//        if(this.isTaskAbandoned()) return;
//        
//        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
//        {
//            this.blamePowerSupply();
//            return;
//        }
//        
//        // If we got to this point, we have a fabricated block.
//        // Put it in domain storage if there is room for it
//        // and tag the task with the location of the bulkResource so drone can find it
//        // otherwise stall
//        if(this.getDomain() == null) return;
//        
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
//    }
   
//    /** returns substance that should be used to create the block in world if it can be successfully fabricated.  
//     * Must be called prior to calling fabricate */
//    private BlockSubstance prepareFabrication(ItemStack stack)
//    {
//        ModelState modelState = PlacementItem.getStackModelState(stack);
//        if(modelState == null) return null;
//        
//        BlockSubstance substance = PlacementItem.getStackSubstance(stack);
//        if(substance == null) return null;
//        
//        int lightValue = PlacementItem.getStackLightValue(stack);
//        
//        DemandManager2 demand = this.getBufferManager().demandManager();
//        
//        demand.clearAllDemand();
//        
//        SuperBlockMaterialCalculator needs = new SuperBlockMaterialCalculator(modelState, substance, lightValue);
//        
//        if(needs.filler_nL > 0) this.bufferFiller.addDemand(needs.filler_nL);
//        if(needs.resinA_nL > 0) this.bufferResinA.addDemand(needs.resinA_nL);
//        if(needs.resinB_nL > 0) this.bufferResinB.addDemand(needs.resinB_nL);
//        if(needs.nanoLights_nL > 0) this.bufferNanoLights.addDemand(needs.nanoLights_nL);
//        if(needs.cyan_nL > 0) this.bufferCyan.addDemand(needs.cyan_nL);
//        if(needs.magenta_nL > 0) this.bufferMagenta.addDemand(needs.magenta_nL);
//        if(needs.yellow_nL > 0) this.bufferYellow.addDemand(needs.yellow_nL);
//        if(needs.TiO2_nL > 0) this.bufferTiO2.addDemand(needs.TiO2_nL);
//    
//        
//        if(demand.canAllDemandsBeMetAndBlameIfNot())
//        {
//            // As soon as we are able to make any block, forget that we have material shortages.
//            // Maybe some other builder will handle.
//            this.getBufferManager().forgiveAll();
//            return needs.actualSubtance;
//        }
//        else
//        {
//            return null;
//        }
//    }

    @Override
    public void onDisconnect()
    {
        this.abandonTaskInProgress();
        super.onDisconnect();
    }

//    private void searchForWork()
//    {
//        if(this.getDomain() == null) return;
//        
//        if(this.energyManager().provideEnergy(JOULES_PER_TICK_IDLE, false, false) == 0)
//        {
//            this.blamePowerSupply();
//            return;
//        }
//        
//        // because we consumed power
//        this.setDirty();
//        this.markTEPlayerUpdateDirty(false);
//        
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
//        ItemStack stack = this.task.procurementTask().getStack();
//        
//        BlockSubstance substance = this.prepareFabrication(stack);
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
//    }

//    @Override
//    public void updateMachine(long tick)
//    {
//        super.updateMachine(tick);
//        
//        if(!this.isOn()) return;
//        
//        if(this.getDomain() == null) return;
//        
//        if((tick & 0x1F) == 0x1F)
//        {
//            this.setCurrentBacklog(this.getDomain().jobManager.getQueueDepth(TaskType.BLOCK_FABRICATION));
//        }
//        
//        switch(this.getControlState().getMachineState())
//        {
//        case FABRICATING:
//            this.progressFabrication();
//            break;
//            
//        case TRANSPORTING:
//            this.outputFabricatedBlock();
//            break;
//            
//        default:
//            this.searchForWork();
//            break;
//            
//        }
//    }
}
