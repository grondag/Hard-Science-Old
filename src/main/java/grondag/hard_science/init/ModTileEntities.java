package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.BlockFabricatorTileEntity;
import grondag.hard_science.machines.SmartChestTileEntity;
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
            GameRegistry.registerTileEntity(VolcanoTileEntity.class, HardScience.MODID + ":volcano_tile");
        }

        GameRegistry.registerTileEntity(SuperTileEntity.class, HardScience.MODID + ":super_tile");
        GameRegistry.registerTileEntity(SuperModelTileEntity.class, HardScience.MODID + ":super_model_tile");
        GameRegistry.registerTileEntity(VirtualTileEntity.class, HardScience.MODID + ":virtual_tile");
        
        GameRegistry.registerTileEntity(SmartChestTileEntity.class, HardScience.MODID + ":smart_chest_tile");
        GameRegistry.registerTileEntity(BlockFabricatorTileEntity.class, HardScience.MODID + ":block_fabricator_tile");
        
        if(event.getSide() == Side.CLIENT)
        {
            //
        }
    }
}
