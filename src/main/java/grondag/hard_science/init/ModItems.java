package grondag.hard_science.init;

import grondag.hard_science.HardScience;
import grondag.hard_science.matter.MatterPackaging;
import grondag.hard_science.superblock.block.ExcavationMarker;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
@SuppressWarnings("null")
public class ModItems
{
    public static final Item smart_chest = null;
    public static final Item smart_bin = null;
    public static final Item solar_cell = null;

//    public static final Item crushed_stone_1kL = null;
//    public static final Item crushed_basalt_1kL = null;
//    public static final Item gold_10g = null;
//    public static final Item gold_1kg = null;
//    public static final Item gold_100kg = null;
//    public static final Item ammonia_1000L = null;
//    public static final Item flex_resin_1L = null;
//    public static final Item flex_resin_1kL = null;
    
    // tools
//    public static final Item obj_test_model = null;
    public static final Item excavation_marker = null;

      
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) 
    {
        IForgeRegistry<Item> itemReg = event.getRegistry();
        
        itemReg.register(new ExcavationMarker("excavation_marker").setCreativeTab(HardScience.tabMod));
        
//        itemReg.register(new BulkItem("gold_10g", ModBulkResources.GOLD, MassUnits.GRAM.withQuantity(10)));
//        itemReg.register(new BulkItem("gold_1kg", ModBulkResources.GOLD, MassUnits.KILOGRAM.withQuantity(1)));
//        itemReg.register(new BulkItem("gold_100kg", ModBulkResources.GOLD, MassUnits.KILOGRAM.withQuantity(100)));
//
//        itemReg.register(new BulkItem("ammonia_1000L", ModBulkResources.AMMONIA_GAS, VolumeUnits.KILOLITER.nL));
//
//        itemReg.register(new BulkItem("flex_resin_1L", ModBulkResources.FLEX_RESIN, VolumeUnits.LITER.nL * 10));
//        itemReg.register(new BulkItem("flex_resin_1kL", ModBulkResources.FLEX_RESIN, VolumeUnits.KILOLITER.nL));
//        
//        itemReg.register(new BulkItem("crushed_stone_1kL", ModBulkResources.MICRONIZED_STONE, VolumeUnits.KILOLITER.nL));
//        itemReg.register(new BulkItem("crushed_basalt_1kL", ModBulkResources.MICRONIZED_BASALT, VolumeUnits.KILOLITER.nL));
        
        for(MatterPackaging matter : MatterPackaging.values())
        {
            matter.register(itemReg);
        }       
//        itemReg.register(new Item().setRegistryName("obj_test_model").setUnlocalizedName("obj_test_model").setCreativeTab(HardScience.tabMod));
    }

}
