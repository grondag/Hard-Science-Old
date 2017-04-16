package grondag.adversity.init;

import grondag.adversity.feature.volcano.TileVolcano;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;


public class ModTileEntities
{
    public static void preInit(FMLPreInitializationEvent event) 
    {
        GameRegistry.registerTileEntity(TileVolcano.class, "TileVolcano");
        
        if(event.getSide() == Side.CLIENT)
        {
            //
        }
    }
}
