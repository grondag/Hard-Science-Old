package grondag.hard_science.init;

import grondag.hard_science.Configurator;
import grondag.hard_science.feature.volcano.VolcanoTileEntity;
import grondag.hard_science.superblock.block.SuperModelTileEntity;
import grondag.hard_science.superblock.block.SuperTileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;


public class ModTileEntities
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        if(Configurator.VOLCANO.enableVolcano)
        {
            GameRegistry.registerTileEntity(VolcanoTileEntity.class, "TileVolcano");
        }

        GameRegistry.registerTileEntity(SuperTileEntity.class, "SuperTileEntity");
        GameRegistry.registerTileEntity(SuperModelTileEntity.class, "SuperModelTileEntity");
        
        if(event.getSide() == Side.CLIENT)
        {
            //
        }
    }
}
