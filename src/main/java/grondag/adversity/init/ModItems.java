package grondag.adversity.init;

import java.util.Map;
import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.TerrainWand;
import grondag.adversity.feature.volcano.VolcanoWand;
import grondag.adversity.feature.volcano.lava.LavaBlobItem;
import grondag.adversity.niceblock.base.NiceBlock;
import grondag.adversity.niceblock.base.NiceItemBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
        itemReg.register(new VolcanoWand().setCreativeTab(Adversity.tabAdversity));
        itemReg.register(new TerrainWand().setCreativeTab(Adversity.tabAdversity));
    }

    private static void registerItemBlocks(IForgeRegistry<Item> itemReg)
    {
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(Adversity.MODID))
            {
                Block b = entry.getValue();
                if(b instanceof NiceBlock)
                {
                    //TODO
                }
                else
                {
                    ItemBlock i = new ItemBlock(b);
                    i.setRegistryName(b.getRegistryName());
                    itemReg.register(i);
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
                    Item i = entry.getValue();
                    if(i instanceof NiceItemBlock)
                    {
                        //TODO
                    }
                    else
                    {
                        ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory"));
                    }
                }
            }
        }
    }
}
