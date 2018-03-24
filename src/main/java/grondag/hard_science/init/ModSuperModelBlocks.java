package grondag.hard_science.init;

import grondag.exotic_matter.block.SuperModelBlock;
import grondag.exotic_matter.model.BlockRenderMode;
import grondag.exotic_matter.model.BlockSubstance;
import grondag.exotic_matter.model.ISuperModelState;
import grondag.exotic_matter.model.WorldLightOpacity;
import grondag.hard_science.HardScience;
import grondag.hard_science.superblock.virtual.VirtualBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModSuperModelBlocks
{
    /** dimensions are BlockRenderMode, worldOpacity, hypermatter (y = 1 /n = 0), cube (y = 1 /n = 0) */
    public static final SuperModelBlock[][][][] superModelBlocks = new SuperModelBlock[BlockRenderMode.values().length][WorldLightOpacity.values().length][2][2];

    /**
     * Virtual blocks only vary by render mode
     */
    public static final VirtualBlock[] virtualBlocks = new VirtualBlock[BlockRenderMode.values().length];

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) 
    {
        int superModelIndex = 0;
        int virualBlockIndex = 0;
        
        for(BlockRenderMode blockRenderMode : BlockRenderMode.values())
        {
            virtualBlocks[blockRenderMode.ordinal()]
                    = (VirtualBlock) new VirtualBlock("virtual_block" + virualBlockIndex++, blockRenderMode)
                        .setUnlocalizedName("virtual_block").setCreativeTab(HardScience.tabMod); //all virtual blocks have same display name
            event.getRegistry().register(virtualBlocks[blockRenderMode.ordinal()]);
            
            for(WorldLightOpacity opacity : WorldLightOpacity.values())
            {
                // mundane non-cube
                superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][0][0]
                        = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, blockRenderMode, opacity, false, false)
                            .setUnlocalizedName("super_model_block").setCreativeTab(HardScience.tabMod); //all superblocks have same display name
                event.getRegistry().register(superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][0][0]);
                
                // mundane cube
                superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][0][1]
                        = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, blockRenderMode, opacity, false, true)
                            .setUnlocalizedName("super_model_block").setCreativeTab(HardScience.tabMod); //all superblocks have same display name
                event.getRegistry().register(superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][0][1]);
                
                // hypermatter non-cube
                superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][1][0]
                        = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, blockRenderMode, opacity, true, false)
                            .setUnlocalizedName("super_model_block").setCreativeTab(HardScience.tabMod); //all superblocks have same display name
                event.getRegistry().register(superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][1][0]);
                
                // hypermatter cube
                superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][1][1]
                        = (SuperModelBlock) new SuperModelBlock("supermodel" + superModelIndex++, Material.ROCK, blockRenderMode, opacity, true, true)
                            .setUnlocalizedName("super_model_block").setCreativeTab(HardScience.tabMod); //all superblocks have same display name
                event.getRegistry().register(superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][1][1]);
                
            }
        }
    }
    
    public static SuperModelBlock findAppropriateSuperModelBlock(BlockSubstance substance, ISuperModelState modelState)
    {
        WorldLightOpacity opacity = WorldLightOpacity.getClosest(substance, modelState);
        BlockRenderMode blockRenderMode = modelState.getRenderPassSet().blockRenderMode;
        int hypermaterIndex = substance.isHyperMaterial ? 1 : 0;
        int cubeIndex = modelState.isCube() ? 1 : 0;
        return superModelBlocks[blockRenderMode.ordinal()][opacity.ordinal()][hypermaterIndex][cubeIndex];
    }
    
    public static VirtualBlock findAppropriateVirtualBlock(ISuperModelState modelState)
    {
        BlockRenderMode blockRenderMode = modelState.getRenderPassSet().blockRenderMode;
        return virtualBlocks[blockRenderMode.ordinal()];
    }
}
