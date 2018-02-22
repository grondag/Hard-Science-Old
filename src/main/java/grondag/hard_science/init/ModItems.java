package grondag.hard_science.init;

import java.util.Map;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.support.MachineItemBlock;
import grondag.hard_science.matter.BulkItem;
import grondag.hard_science.matter.MassUnits;
import grondag.hard_science.matter.MatterPackaging;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.BlockAdjuster;
import grondag.hard_science.superblock.items.ExcavationMarker;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.terrain.TerrainWand;
import grondag.hard_science.superblock.virtual.VirtualBlock;
import grondag.hard_science.superblock.virtual.VirtualItemBlock;
import grondag.hard_science.volcano.lava.LavaBlobItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
public class ModItems
{
    public static final Item basalt_rubble = null;
    
    // item blocks
    public static final Item basalt_cobble = null;
    public static final Item basalt_cut = null;
    
    public static final Item smart_chest = null;
    public static final Item solar_cell = null;

    public static final Item crushed_stone_1kL = null;
    public static final Item crushed_basalt_1kL = null;
    public static final Item gold_10g = null;
    public static final Item gold_1kg = null;
    public static final Item gold_100kg = null;
    public static final Item ammonia_1000L = null;
    public static final Item flex_resin_1L = null;
    public static final Item flex_resin_1kL = null;
    
    // tools
//    public static final Item obj_test_model = null;
    public static final Item excavation_marker = null;
      
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) 
    {
        IForgeRegistry<Item> itemReg = event.getRegistry();
        registerItemBlocks(itemReg);
 
        itemReg.register(new Item().setRegistryName("basalt_rubble").setUnlocalizedName("basalt_rubble").setCreativeTab(HardScience.tabMod));

        if(Configurator.VOLCANO.enableVolcano)
        {
            itemReg.register(new LavaBlobItem().setRegistryName("lava_blob").setUnlocalizedName("lava_blob").setCreativeTab(HardScience.tabMod));
            //Disabled until volcano world gen / activation logic revisited
            //itemReg.register(new VolcanoWand().setCreativeTab(HardScience.tabMod));
            itemReg.register(new TerrainWand().setCreativeTab(HardScience.tabMod));
        }
        
        itemReg.register(new BlockAdjuster().setCreativeTab(HardScience.tabMod));
        itemReg.register(new ExcavationMarker().setCreativeTab(HardScience.tabMod));
        
        itemReg.register(new BulkItem("gold_10g", ModBulkResources.GOLD, MassUnits.GRAM.withQuantity(10)));
        itemReg.register(new BulkItem("gold_1kg", ModBulkResources.GOLD, MassUnits.KILOGRAM.withQuantity(1)));
        itemReg.register(new BulkItem("gold_100kg", ModBulkResources.GOLD, MassUnits.KILOGRAM.withQuantity(100)));

        itemReg.register(new BulkItem("ammonia_1000L", ModBulkResources.AMMONIA_GAS, VolumeUnits.KILOLITER.nL));

        itemReg.register(new BulkItem("flex_resin_1L", ModBulkResources.FLEX_RESIN, VolumeUnits.LITER.nL * 10));
        itemReg.register(new BulkItem("flex_resin_1kL", ModBulkResources.FLEX_RESIN, VolumeUnits.KILOLITER.nL));
        
        itemReg.register(new BulkItem("crushed_stone_1kL", ModBulkResources.CRUSHED_STONE, VolumeUnits.KILOLITER.nL));
        itemReg.register(new BulkItem("crushed_basalt_1kL", ModBulkResources.CRUSHED_BASALT, VolumeUnits.KILOLITER.nL));
        
        for(MatterPackaging matter : MatterPackaging.values())
        {
            matter.register(itemReg);
        }       
//        itemReg.register(new Item().setRegistryName("obj_test_model").setUnlocalizedName("obj_test_model").setCreativeTab(HardScience.tabMod));
    }

    private static void registerItemBlocks(IForgeRegistry<Item> itemReg)
    {
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                ItemBlock itemBlock;
                Block block = entry.getValue();
                if(block instanceof MachineBlock)
                {
                    itemBlock = new MachineItemBlock((MachineBlock)block);
                }
                else if(block instanceof VirtualBlock)
                {
                    itemBlock = new VirtualItemBlock((VirtualBlock)block);
                }
                else if(block instanceof SuperBlock)
                {
                    itemBlock = new SuperItemBlock((SuperBlock)block);
                }
                else
                {
                    itemBlock = new ItemBlock(block);
                }
                itemBlock.setRegistryName(block.getRegistryName());
                itemReg.register(itemBlock);
            }
        }
    }
}
