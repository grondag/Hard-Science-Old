package grondag.adversity.init;

import java.io.IOException;
import java.util.Map;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.BlockVolcano;
import grondag.adversity.niceblock.DummyColorHandler;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperDispatcher;
import grondag.adversity.superblock.block.SuperStateMapper;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
@ObjectHolder("adversity")
public class ModBlocks
{
    private static final SuperStateMapper STATE_MAPPER = new SuperStateMapper(ModModels.MODEL_DISPATCH);
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        event.getRegistry().register(new BlockVolcano());
//        event.getRegistry().register(new SuperBlock(BaseMaterial.FLEXSTONE, "test"));
    }
    
    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) throws IOException
    {
        for (int i = 0; i < ModelState.BENUMSET_RENDER_LAYER.combinationCount(); i++)
        {
            SuperDispatcher.DispatcherDelegate delegate = ModModels.MODEL_DISPATCH.getDelegateForShadedFlags(i);
            event.getModelRegistry().putObject(new ModelResourceLocation(delegate.getModelResourceString()), delegate);
        }
    }
    
    public static void preInit(FMLPreInitializationEvent event) 
    {
        if (event.getSide() == Side.CLIENT)
        {
            IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
            for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
            {
                if(entry.getKey().getResourceDomain().equals(Adversity.MODID))
                {
                    Block block = entry.getValue();
                    if(block instanceof SuperBlock)
                    {
                        ModelLoader.setCustomStateMapper(block, STATE_MAPPER);
                    }
                }
            }
        }
    }
    
    public static void init(FMLInitializationEvent event)
    {
        IForgeRegistry<Block> blockReg = GameRegistry.findRegistry(Block.class);
        
        for(Map.Entry<ResourceLocation, Block> entry: blockReg.getEntries())
        {
            if(entry.getKey().getResourceDomain().equals(Adversity.MODID))
            {
                Block block = entry.getValue();
                if(block instanceof SuperBlock)
                {
                    // won't work in pre-init because BlockColors/ItemColors aren't instantiated yet
                    // Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(block.blockModelHelper.dispatcher, block);
                    Minecraft.getMinecraft().getItemColors().registerItemColorHandler(DummyColorHandler.INSTANCE, block);
                }
            }
        }
    }
}
