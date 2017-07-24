package grondag.hard_science.init;

import java.util.Map;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.feature.volcano.lava.LavaBlobItem;
import grondag.hard_science.materials.PolyethyleneCube;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.BlockAdjuster;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.terrain.TerrainWand;
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

    // materials
    public static final Item hdpe_cube_1 = null;
    public static final Item hdpe_cube_2 = null;
    public static final Item hdpe_cube_3 = null;
    public static final Item hdpe_cube_4 = null;
    public static final Item hdpe_cube_5 = null;
    public static final Item hdpe_cube_6 = null;
    public static final Item hdpe_wafer_6 = null;
    
    // item blocks
    public static final Item basalt_cobble = null;
    public static final Item basalt_cut = null;
    
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) 
    {
        IForgeRegistry<Item> itemReg = event.getRegistry();
        registerItemBlocks(itemReg);
 
        if(Configurator.VOLCANO.enableVolcano)
        {
            itemReg.register(new Item().setRegistryName("basalt_rubble").setUnlocalizedName("basalt_rubble").setCreativeTab(HardScience.tabMod));
            itemReg.register(new LavaBlobItem().setRegistryName("lava_blob").setUnlocalizedName("lava_blob").setCreativeTab(HardScience.tabMod));
            //Disabled until volcano world gen / activation logic revisited
            //itemReg.register(new VolcanoWand().setCreativeTab(HardScience.tabMod));
            itemReg.register(new TerrainWand().setCreativeTab(HardScience.tabMod));
        }
        
        itemReg.register(new BlockAdjuster().setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(1).setRegistryName("hdpe_cube_1").setUnlocalizedName("hdpe_cube_1").setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(2).setRegistryName("hdpe_cube_2").setUnlocalizedName("hdpe_cube_2").setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(3).setRegistryName("hdpe_cube_3").setUnlocalizedName("hdpe_cube_3").setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(4).setRegistryName("hdpe_cube_4").setUnlocalizedName("hdpe_cube_4").setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(5).setRegistryName("hdpe_cube_5").setUnlocalizedName("hdpe_cube_5").setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(6).setRegistryName("hdpe_cube_6").setUnlocalizedName("hdpe_cube_6").setCreativeTab(HardScience.tabMod));
        itemReg.register(new PolyethyleneCube(6).setRegistryName("hdpe_wafer_6").setUnlocalizedName("hdpe_wafer_6").setCreativeTab(HardScience.tabMod));


    }

    private static void registerItemBlocks(IForgeRegistry<Item> itemReg)
    {
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
            {
                Block block = entry.getValue();
                if(block instanceof SuperBlock)
                {
                    SuperBlock superBlock = (SuperBlock)block;
                    SuperItemBlock itemBlock = new SuperItemBlock(superBlock);
                    itemBlock.setRegistryName(superBlock.getRegistryName());
                    itemReg.register(itemBlock);
                }
                else
                {
                    ItemBlock itemBlock = new ItemBlock(block);
                    itemBlock.setRegistryName(block.getRegistryName());
                    itemReg.register(itemBlock);
                }
            }
        }
    }
    
//    public static void preInit(FMLPreInitializationEvent event) 
//    {
//  
//    }
}