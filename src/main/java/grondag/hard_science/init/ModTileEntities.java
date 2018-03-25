package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.machines.impl.building.BlockFabricatorTileEntity;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryTileEntity;
import grondag.hard_science.machines.impl.processing.DigesterTileEntity;
import grondag.hard_science.machines.impl.processing.MicronizerTileEntity;
import grondag.hard_science.superblock.virtual.VirtualTileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;


public class ModTileEntities
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        GameRegistry.registerTileEntity(VirtualTileEntity.class, HardScience.INSTANCE.prefixResource("virtual_tile"));
        
        GameRegistry.registerTileEntity(MachineTileEntity.class, HardScience.INSTANCE.prefixResource("machine_tile"));
        GameRegistry.registerTileEntity(MachineTileEntityTickable.class, HardScience.INSTANCE.prefixResource("machine_tickable_tile"));
        GameRegistry.registerTileEntity(BlockFabricatorTileEntity.class, HardScience.INSTANCE.prefixResource("block_fabricator_tile"));
        GameRegistry.registerTileEntity(ChemicalBatteryTileEntity.class, HardScience.INSTANCE.prefixResource("chemical_battery_tile"));
        GameRegistry.registerTileEntity(MicronizerTileEntity.class, HardScience.INSTANCE.prefixResource("crushinator_tile"));
        GameRegistry.registerTileEntity(DigesterTileEntity.class, HardScience.INSTANCE.prefixResource("digester_tile"));
    }
}
