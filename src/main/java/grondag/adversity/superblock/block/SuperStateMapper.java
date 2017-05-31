package grondag.adversity.superblock.block;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.init.ModModels;
import grondag.adversity.niceblock.support.BlockSubstance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;

/**
* SuperBlocks all use the same model underlying dispatcher.  
* However, there are 16 delegates of it that are used for 
* different shading combinations of each render layer.
*/
public class SuperStateMapper extends DefaultStateMapper 
{
    
    private final SuperDispatcher dispatcher;
    
    public SuperStateMapper(SuperDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

   @Override
   public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
       Map<IBlockState, ModelResourceLocation> mapLocations = Maps.newLinkedHashMap();

       if (block instanceof SuperBlock) {
           SuperBlock superBlock = (SuperBlock) block;
           for (int i = 0; i < 16; i++) {
               IBlockState state = superBlock.getDefaultState().withProperty(SuperBlock.META, i);
                   
               if(block instanceof SuperModelBlock)
               {
                   for(BlockSubstance substance : BlockSubstance.values())
                   {
                       mapLocations.put(state.withProperty(SuperModelBlock.SUBSTANCE, substance.ordinal()),
                               new ModelResourceLocation(ModModels.MODEL_DISPATCH.getModelResourceString()));
                   }
               }
               else
               {
                   mapLocations.put(state,
                           new ModelResourceLocation(ModModels.MODEL_DISPATCH.getModelResourceString()));
               }

           }
       }
       return mapLocations;
   }

}