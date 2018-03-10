package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.machines.impl.building.BlockFabricatorTileEntity;
import grondag.hard_science.machines.impl.logistics.ChemicalBatteryTileEntity;
import grondag.hard_science.machines.impl.processing.DigesterTileEntity;
import grondag.hard_science.machines.impl.processing.MicronizerTileEntity;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.block.SuperTileEntity;
import grondag.hard_science.superblock.virtual.VirtualTileEntity;
import grondag.hard_science.volcano.VolcanoTileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;


public class ModTileEntities
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        if(Configurator.VOLCANO.enableVolcano)
        {
            GameRegistry.registerTileEntity(VolcanoTileEntity.class, HardScience.prefixResource("volcano_tile"));
        }

        GameRegistry.registerTileEntity(SuperTileEntity.class, HardScience.prefixResource("super_tile"));
        GameRegistry.registerTileEntity(SuperModelTileEntity.class, HardScience.prefixResource("super_model_tile"));
        GameRegistry.registerTileEntity(VirtualTileEntity.class, HardScience.prefixResource("virtual_tile"));
        
        GameRegistry.registerTileEntity(MachineTileEntity.class, HardScience.prefixResource("machine_tile"));
        GameRegistry.registerTileEntity(MachineTileEntityTickable.class, HardScience.prefixResource("machine_tickable_tile"));
        GameRegistry.registerTileEntity(BlockFabricatorTileEntity.class, HardScience.prefixResource("block_fabricator_tile"));
        GameRegistry.registerTileEntity(ChemicalBatteryTileEntity.class, HardScience.prefixResource("chemical_battery_tile"));
        GameRegistry.registerTileEntity(MicronizerTileEntity.class, HardScience.prefixResource("crushinator_tile"));
        GameRegistry.registerTileEntity(DigesterTileEntity.class, HardScience.prefixResource("digester_tile"));
        
        if(event.getSide() == Side.CLIENT)
        {
            //
        }
    }
}
