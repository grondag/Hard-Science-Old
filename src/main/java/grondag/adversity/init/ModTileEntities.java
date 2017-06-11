package grondag.adversity.init;

import grondag.adversity.feature.volcano.VolcanoTileEntity;
import grondag.adversity.superblock.block.SuperModelTileEntity;
import grondag.adversity.superblock.block.SuperTileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;


public class ModTileEntities
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        GameRegistry.registerTileEntity(VolcanoTileEntity.class, "TileVolcano");
        GameRegistry.registerTileEntity(SuperTileEntity.class, "SuperTileEntity");
        GameRegistry.registerTileEntity(SuperModelTileEntity.class, "SuperModelTileEntity");
        
        if(event.getSide() == Side.CLIENT)
        {
            //
        }
    }
}
