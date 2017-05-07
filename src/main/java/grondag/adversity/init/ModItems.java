package grondag.adversity.init;

import java.io.IOException;
import java.util.Map;
import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.TerrainWand;
import grondag.adversity.feature.volcano.lava.LavaBlobItem;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperItemBlock;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
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
@ObjectHolder("adversity")
public class ModItems
{
    public static final Item basalt_rubble = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) 
    {
        IForgeRegistry<Item> itemReg = event.getRegistry();
        registerItemBlocks(itemReg);
 
        itemReg.register(new Item().setRegistryName("basalt_rubble").setUnlocalizedName("basalt_rubble").setCreativeTab(Adversity.tabAdversity));
        itemReg.register(new LavaBlobItem().setRegistryName("lava_blob").setUnlocalizedName("lava_blob").setCreativeTab(Adversity.tabAdversity));
        //Disabled until volcano world gen / activation logic revisited
        //itemReg.register(new VolcanoWand().setCreativeTab(Adversity.tabAdversity));
        itemReg.register(new TerrainWand().setCreativeTab(Adversity.tabAdversity));
    }

    private static void registerItemBlocks(IForgeRegistry<Item> itemReg)
    {
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(Adversity.MODID))
            {
                Block block = entry.getValue();
                if(block instanceof NiceBlock)
                {
                    // SKIP
                }
                else if(block instanceof SuperBlock)
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
                if(entry.getKey().getResourceDomain().equals(Adversity.MODID))
                {
                    Item item = entry.getValue();
                    if(item instanceof NiceItemBlock)
                    {
                        //SKIP
                    }
                    else if(item instanceof SuperItemBlock)
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
    
    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        IForgeRegistry<Item> itemReg = GameRegistry.findRegistry(Item.class);
        
        for(Map.Entry<ResourceLocation, Item> entry: itemReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(Adversity.MODID))
            {
                Item item = entry.getValue();
                if(item instanceof NiceItemBlock)
                {
                    //SKIP
                }
                else if(item instanceof SuperItemBlock)
                {
                    for (ItemStack stack : ((SuperBlock)(((ItemBlock)item).getBlock())).getSubItems())
                    {
                        ModelState modelState = SuperItemBlock.getModelState(stack);
                    
                        event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName() + "." + stack.getMetadata(), "inventory"),
                                ModModels.MODEL_DISPATCH.getDelegateForShadedFlags(modelState.getRenderLayerShadedFlags()));
                    }
                }
                else
                {
                    // Not needed - will look for json files for normal items;
                }
            }
        }
    }
}
