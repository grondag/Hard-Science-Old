package grondag.hard_science.machines.impl.processing;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import grondag.hard_science.crafting.processing.MicronizerRecipe;
import grondag.hard_science.library.varia.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.Simulator;
import grondag.hard_science.simulator.domain.Domain;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.fobs.SimpleProcurementTask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

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
 * TODO: This is a (mostly) mock implementation
 *
 */
public class MicronizerInputSelector
{
    /**
     * Stores mole fraction contribution of an input resources to producing
     * bulk processing outputs.
     */
    //    private static final Key2List<Triple<IResource<?>, FluidResource, Double>, IResource<?>, FluidResource> weights 
    //    = new Key2List.Builder<Triple<IResource<?>, FluidResource, Double>, IResource<?>, FluidResource>().
    //          withKey1Map(Triple::getLeft).
    //          withKey2Map(Triple::getMiddle).
    //          build();

    /** cache the per-domain input count whenver it is looked up */
    private static final Object2IntOpenHashMap<Domain> backlogs
    = new Object2IntOpenHashMap<>();

    /**
     * Last tick in which any backlog estimate was updated
     */
    private static int lastTick = 0;

    /**
     * Domains that have already had backlog estimates updated in the current tick.
     */
    private static SimpleUnorderedArrayList<Domain> checkedDomains 
    = new SimpleUnorderedArrayList<>();

    /**
     * Do not access directly, use {@link #endProducts()}
     */
    //    private static ImmutableList<IResource<?>> endProducts;

    //    static
    //    {
    //        // find all crusher inputs
    //        
    //        // for each input, identify all managed bulk resources that can be derived
    ////        endProducts = ImmutableList.of();
    //        
    //        // for each derived output, compute the contributing mole fraction of the input
    //        weights.add(Triple.of(
    //                ItemResource.fromItem(Item.getItemFromBlock(Blocks.COBBLESTONE)), 
    //                ModBulkResources.CALCIUM.fluidResource(), 
    //                0.05));
    //
    //    }


    /**
     * Finds best inputs for micronizing.
     * Items are in order of preference.
     * Must be called from item storage service thread due to calls it makes.
     */
    private static List<AbstractResourceWithQuantity<StorageTypeStack>> findBestInputs(Domain domain)
    {
        List<AbstractResourceWithQuantity<StorageTypeStack>> candidates 
        = domain.itemStorage.findQuantityAvailable(MicronizerRecipe.RESOURCE_PREDICATE);

        updateBacklogDepth(domain, candidates);
        return candidates;

    }


    private static void updateBacklogDepth(Domain domain, List<AbstractResourceWithQuantity<StorageTypeStack>> candidates)
    {
        if(Simulator.instance().getTick() != lastTick)
        {
            lastTick = Simulator.instance().getTick();
            checkedDomains.clear();
        }

        if(checkedDomains.contains(domain)) return;

        int result = 0;
        for(AbstractResourceWithQuantity<StorageTypeStack> d : candidates)
        {
            result += (int)d.getQuantity();
        }
        backlogs.put(domain, result);
    }

    /**
     * 
     * Estimated number of crusher cycles that need to run to catch
     * up with demand in the given domain based on input material
     * available.
     */
    public static int estimatedBacklogDepth(MicronizerMachine micronizerMachine)
    {
        return backlogs.getInt(micronizerMachine.getDomain());
    }

    public static Future<NewProcurementTask<StorageTypeStack>> requestInput(MicronizerMachine machine)
    {
        return LogisticsService.ITEM_SERVICE.executor.submit(new Callable<NewProcurementTask<StorageTypeStack>>()
        {
            @Override
            public NewProcurementTask<StorageTypeStack> call() throws Exception
            {
                // abort if requesting machine isn't connected to anything
                if(!machine.itemTransport().hasAnyCircuit()) return null;

                // look for output resources that are in demand
                // for those in demand, take the highest priority
                List<AbstractResourceWithQuantity<StorageTypeStack>> candidates
                = findBestInputs(machine.getDomain());

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
}
