package grondag.hard_science.init;

import grondag.hard_science.superblock.block.SuperModelBlock;
import grondag.hard_science.superblock.model.state.RenderModeSet;
import grondag.hard_science.superblock.model.state.WorldLightOpacity;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import grondag.hard_science.superblock.varia.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModSuperModelBlocks
{
    /** dimensions are RenderModeSet, worldOpacity, hypermatter (y = 1 /n = 0), cube (y = 1 /n = 0) */
    public static final SuperModelBlock[][][][] superModelBlocks = new SuperModelBlock[RenderModeSet.values().length][WorldLightOpacity.values().length][2][2];

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        int superModelIndex = 0;
        for(RenderModeSet layerSet : RenderModeSet.values())
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
        RenderModeSet layerSet = RenderModeSet.findSmallestInclusiveSet(modelState);
        int hypermaterIndex = substance.isHyperMaterial ? 1 : 0;
        int cubeIndex = modelState.isCube() ? 1 : 0;
        return superModelBlocks[layerSet.ordinal()][opacity.ordinal()][hypermaterIndex][cubeIndex];
    }
}
