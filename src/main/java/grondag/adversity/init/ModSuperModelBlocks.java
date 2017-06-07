package grondag.adversity.init;

import grondag.adversity.superblock.block.BlockRenderLayerSet;
import grondag.adversity.superblock.block.SuperModelBlock;
import grondag.adversity.superblock.block.WorldLightOpacity;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.support.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModSuperModelBlocks
{
    /** dimensions are BlockRenderLayerSet, worldOpacity, hypermatter (y = 1 /n = 0), cube (y = 1 /n = 0) */
    public static final SuperModelBlock[][][][] superModelBlocks = new SuperModelBlock[BlockRenderLayerSet.values().length][WorldLightOpacity.values().length][2][2];

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        int superModelIndex = 0;
        for(BlockRenderLayerSet layerSet : BlockRenderLayerSet.values())
        {
            for(WorldLightOpacity opacity : WorldLightOpacity.values())
            {
                // mundane non-cube
                superModelBlocks[layerSet.ordinal()][opacity.ordinal()][0][0]
                        = new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, layerSet, opacity, false, false);
                event.getRegistry().register(superModelBlocks[layerSet.ordinal()][opacity.ordinal()][0][0]);
                
                // mundane cube
                superModelBlocks[layerSet.ordinal()][opacity.ordinal()][0][1]
                        = new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, layerSet, opacity, false, true);
                event.getRegistry().register(superModelBlocks[layerSet.ordinal()][opacity.ordinal()][0][1]);
                
                // hypermatter non-cube
                superModelBlocks[layerSet.ordinal()][opacity.ordinal()][1][0]
                        = new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, layerSet, opacity, true, false);
                event.getRegistry().register(superModelBlocks[layerSet.ordinal()][opacity.ordinal()][1][0]);
                
                // hypermatter cube
                superModelBlocks[layerSet.ordinal()][opacity.ordinal()][1][1]
                        = new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, layerSet, opacity, true, true);
                event.getRegistry().register(superModelBlocks[layerSet.ordinal()][opacity.ordinal()][1][1]);
                
            }
        }
    }
    public static SuperModelBlock findAppropriateSuperModelBlock(BlockSubstance substance, ModelState modelState)
    {
        WorldLightOpacity opacity = WorldLightOpacity.getClosest(substance, modelState);
        BlockRenderLayerSet layerSet = BlockRenderLayerSet.findSmallestInclusiveSet(modelState);
        int hypermaterIndex = substance.isHyperMaterial ? 1 : 0;
        int cubeIndex = modelState.isCube() ? 1 : 0;
        return superModelBlocks[layerSet.ordinal()][opacity.ordinal()][hypermaterIndex][cubeIndex];
    }
}
