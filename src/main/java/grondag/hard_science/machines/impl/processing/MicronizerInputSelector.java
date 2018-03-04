package grondag.hard_science.machines.impl.processing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.domain.ProcessManager.ProcessInfo;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.fobs.SimpleProcurementTask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import it.unimi.dsi.fastutil.objects.AbstractObject2LongMap.BasicEntry;
import net.minecraft.util.math.MathHelper;

/**
 * Answers the question:  if I have machine that
 * can process two things, and I have both things on hand, which thing
 * should I process first?<p>
 * 
 * General rules
 * Process resources that satisfy higher-priority requests first
 * Process resources that will satisfy the largest number of requests.
 * Don't process a resource if an immediate output has reached max backlog or storage is full.
 *
 *
 */
public class MicronizerInputSelector implements IDomainMember
{
    private final Domain domain;
    private int maxBacklog;
    private int backlogDepth;
    private int nextUpdateTick;
    
    private HashMap<FluidResource, Demand> demands = new HashMap<>();
    
    private ImmutableList<AbstractResourceWithQuantity<StorageTypeStack>> bestInputs;
    
    public MicronizerInputSelector(Domain domain)
    {
        this.domain = domain;
    }

    private void updateBacklogIfStale()
    {
        if(Simulator.instance().getTick() < nextUpdateTick) return;
        
        nextUpdateTick = Simulator.instance().getTick() + 20;
        
        long maxDemand = 0;
        long totalDemand = 0;
        
        this.demands.clear();
        for(BulkResource br : MicronizerRecipe.allOutputs())
        {
            ProcessInfo info = this.domain.processManager.getInfo(br.fluidResource());
            maxDemand += info.minStockLevel();
            long demand = info.demand();
            if(demand > 0)
            {
                totalDemand += demand;
                int priority = (int) (info.minStockLevel() > 0
                        ? Short.MAX_VALUE - (demand * Short.MAX_VALUE / info.minStockLevel())
                        : 0);
                this.demands.put(br.fluidResource(),
                        new Demand(demand, priority));
            }
        }
        this.maxBacklog = MathHelper.ceil(VolumeUnits.nL2Liters(maxDemand));
        this.backlogDepth = MathHelper.ceil(VolumeUnits.nL2Liters(totalDemand));
        
        
        // find all inputs
        List<AbstractResourceWithQuantity<StorageTypeStack>> candidates 
            = domain.itemStorage.findEstimatedAvailable(MicronizerRecipe.INPUT_RESOURCE_PREDICATE);
        
        ArrayList<BasicEntry<AbstractResourceWithQuantity<StorageTypeStack>>> allInputs  = new ArrayList<>();
        
        // for each input, see if outputs are needed and prioritize
        for(AbstractResourceWithQuantity<StorageTypeStack> c : candidates)
        {
            ItemResource inputRes = (ItemResource)c.resource();
            
            MicronizerRecipe recipe = MicronizerRecipe.getForInput(inputRes);
            if(recipe == null) continue;
            
            Demand demand = this.demands.get(recipe.outputResource().fluidResource());
            if(demand == null || demand.quantity == 0) continue;
                    
            int inputDemand = recipe.inputQtyForOutputQty(inputRes.sampleItemStack(), demand.quantity);
            
            int inputFactor = 0;
            ProcessInfo inputInfo = domain.processManager.getInfo(inputRes);
            if(inputInfo != null)
            {
                inputFactor = (Short.MAX_VALUE - inputInfo.priority());
                
                if(inputInfo.minStockLevel() > 0)
                {
                    inputFactor |= (Short.MAX_VALUE
                        - (inputInfo.demand() * Short.MAX_VALUE / inputInfo.minStockLevel())) 
                            << 8;
                }
            }
            
            // rank value components from most significant to least: 
            //  output priority (inverted), 
            //  output stocking factor (low = more available)
            //  input availability/value factor (low = less available/valuable)
            long ranking = (demand.priority << 16) | inputFactor;
                    
            allInputs.add(new BasicEntry<>(inputRes.withQuantity(inputDemand), ranking));
        }
        
        this.bestInputs = allInputs.stream()
                .sorted(new Comparator<BasicEntry<AbstractResourceWithQuantity<StorageTypeStack>>>()
                {
                    @Override
                    public int compare(BasicEntry<AbstractResourceWithQuantity<StorageTypeStack>> o1, BasicEntry<AbstractResourceWithQuantity<StorageTypeStack>> o2)
                    {
                        return Long.compare(o1.getLongValue(), o2.getLongValue());
                    }
                })
                .map(p -> p.getKey())
                .collect(ImmutableList.toImmutableList());
    }
    
    private static class Demand
    {
        private final long quantity;
        private final int priority;
        
        private Demand(long demand, int priority)
        {
            this.quantity = demand;
            this.priority = priority;
        }
    }
    
    /**
     * Finds best inputs for micronizing, and an upper bound
     * for how much of it should be processed.  Upper bound
     * because could exist multiple items that satisfy the need.
     * 
     * TODO: Use request priority as an input - right now only considers
     *  domain stocking levels and priorities
     */
    private List<AbstractResourceWithQuantity<StorageTypeStack>> findBestInputs()
    {
        this.updateBacklogIfStale();
        return this.bestInputs;
    }

    /**
     * 
     * Estimated number of crusher cycles that need to run to catch
     * up with quantity in the given domain based on input material
     * available.
     */
    public int estimatedBacklogDepth()
    {
        this.updateBacklogIfStale();
        return this.backlogDepth;
    }

    public int maxBacklogDepth()
    {
        this.updateBacklogIfStale();
        return this.maxBacklog;
    }
    
    public Future<NewProcurementTask<StorageTypeStack>> requestInput(MicronizerMachine machine)
    {
        return LogisticsService.ITEM_SERVICE.executor.submit(new Callable<NewProcurementTask<StorageTypeStack>>()
        {
            @Override
            public NewProcurementTask<StorageTypeStack> call() throws Exception
            {
                // abort if requesting machine isn't connected to anything
                if(!machine.itemTransport().hasAnyCircuit()) return null;

                // look for output resources that are in quantity
                // for those in quantity, take the highest priority
                List<AbstractResourceWithQuantity<StorageTypeStack>> candidates
                    = findBestInputs();

                NewProcurementTask<StorageTypeStack> result = null;

                if(!candidates.isEmpty())
                {
                    for(AbstractResourceWithQuantity<StorageTypeStack> rwq : candidates)
                    {
                        List<IResourceContainer<StorageTypeStack>> sources = 
                                machine.getDomain().itemStorage.findSourcesFor(rwq.resource(), machine);

                        if(sources.isEmpty()) continue;

                        // skip if machine can't accept this resource
                        if(machine.getBufferManager().itemInput().availableCapacityFor(rwq.resource()) == 0) continue;
                        
                        IResourceContainer<StorageTypeStack> store = sources.get(0);

                        result = new SimpleProcurementTask<>(
                                machine.getDomain().systemTasks, rwq.resource(), 1);

                        if(machine.getDomain().itemStorage.setAllocation(rwq.resource(), result, 1) == 1)
                        {
                            // already on service thread so send immediately
                            LogisticsService.ITEM_SERVICE.sendResourceNow(rwq.resource(), 1L, store.device(), machine, false, false, result);
                        }
                        
                        // release any outstanding allocation that wasn't transported
                        result.cancel();
                    }
                }
                return result;
            }
        }, false);

    }


    @Override
    public Domain getDomain()
    {
        return this.domain;
    }
}
