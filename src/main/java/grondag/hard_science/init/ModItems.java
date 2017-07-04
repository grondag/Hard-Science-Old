package grondag.hard_science.init;

import java.util.Map;

import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.feature.volcano.lava.LavaBlobItem;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.items.BlockAdjuster;
import grondag.hard_science.superblock.items.SuperItemBlock;
import grondag.hard_science.superblock.terrain.TerrainWand;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
@ObjectHolder(HardScience.MODID)
public class ModItems
{
    public static final Item basalt_rubble = null;

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
    
    public static void preInit(FMLPreInitializationEvent event) 
    {
        if(event.getSide() == Side.CLIENT)
        {
            IForgeRegistry<Item> itemReg = GameRegistry.findRegistry(Item.class);
            
            for(Map.Entry<ResourceLocation, Item> entry: itemReg.getEntries())
            {
                if(entry.getKey().getResourceDomain().equals(HardScience.MODID))
                {
                    Item item = entry.getValue();
                    if(item instanceof SuperItemBlock)
                    {
                        for (ItemStack stack : ((SuperBlock)(((ItemBlock)item).getBlock())).getSubItems())
                        {
                            ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(item.getRegistryName() + "." + stack.getMetadata(), "inventory");
                            ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(), itemModelResourceLocation);
                        }
                    }
                    else
                    {
                        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
                    }
                }
            }
        }
    }
}
