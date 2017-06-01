package grondag.adversity.init;

import java.util.Map;

import grondag.adversity.Adversity;
import grondag.adversity.feature.volcano.BlockVolcano;
import grondag.adversity.niceblock.DummyColorHandler;
import grondag.adversity.niceblock.color.BlockColorMapProvider;
import grondag.adversity.niceblock.color.HueSet.Chroma;
import grondag.adversity.niceblock.color.HueSet.Luminance;
import grondag.adversity.niceblock.color.NiceHues.Hue;
import grondag.adversity.niceblock.support.BlockSubstance;
import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperSimpleBlock;
import grondag.adversity.superblock.block.SuperStateMapper;
import grondag.adversity.superblock.model.layout.PaintLayer;
import grondag.adversity.superblock.model.shape.ModelShape;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.texture.Textures;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
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
    public static final Block basalt_cobble = null;
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        event.getRegistry().register(new BlockVolcano());
        
        ModelState workingModel = new ModelState();
        workingModel.setShape(ModelShape.CUBE);
        workingModel.setTexture(PaintLayer.BASE, Textures.BLOCK_COBBLE);
        workingModel.setColorMap(PaintLayer.BASE, BlockColorMapProvider.INSTANCE.getColorMap(Hue.COBALT, Chroma.NEUTRAL, Luminance.MEDIUM_DARK));
        event.getRegistry().register(new SuperSimpleBlock("basalt_cobble", BlockSubstance.BASALT, workingModel));
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
