package grondag.hard_science.init;

import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import grondag.hard_science.matter.MatterPhase;
import grondag.hard_science.simulator.resource.BulkResource;
import grondag.hard_science.simulator.resource.FluidResource;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import jline.internal.Log;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModFluids
{
    public static final IResourcePredicate<StorageTypeFluid> PREDICATE_NORMAL_FLUIDS; 
    public static final IResourcePredicate<StorageTypeFluid> PREDICATE_LIGHT_PRESSURE; 
    public static final IResourcePredicate<StorageTypeFluid> PREDICATE_HIGH_PRESSURE; 
    public static final IResourcePredicate<StorageTypeFluid> PREDICATE_BULK_SOLIDS; 
    
    static
    {
        for(BulkResource m : ModBulkResources.all().values())
        {
            m.registerFluid();
        }
        
        PREDICATE_NORMAL_FLUIDS = new BulkPredicate(r -> r.phase() == MatterPhase.LIQUID
                      && r.pressureAtm() == 1);
    
        PREDICATE_LIGHT_PRESSURE = new BulkPredicate(r -> 
            (r.phase() != MatterPhase.SOLID
            && r.pressureAtm() > 1
            && r.pressureAtm() <= 20)
            ||
            (r.phase() != MatterPhase.GAS
            && r.pressureAtm() == 1));
    
        PREDICATE_HIGH_PRESSURE = new BulkPredicate(r -> 
            r.phase() != MatterPhase.SOLID
              && r.pressureAtm() > 20);
    
        PREDICATE_BULK_SOLIDS = new BulkPredicate(r -> 
            r.phase() == MatterPhase.SOLID);
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        // force execution of static block
        Log.info("Registering Hard Science fluids");
    }
    
    private static class BulkPredicate implements IResourcePredicate<StorageTypeFluid>
    {
        final ImmutableSet<FluidResource> allowed;

        private BulkPredicate(Predicate<BulkResource> predicate)
        {
            this.allowed = ModBulkResources.all().values().stream()
                    .filter(predicate)
                    .map(r -> r.fluidResource())
                    .collect(ImmutableSet.toImmutableSet());
        }
        
        @Override
        public boolean test(IResource<StorageTypeFluid> t)
        {
            return this.allowed.contains(t);
        }
    }
}
