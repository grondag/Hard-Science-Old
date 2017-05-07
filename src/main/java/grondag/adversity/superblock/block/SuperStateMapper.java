package grondag.adversity.superblock.block;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
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
               
               for(int j = 0; j < ModelState.BENUMSET_RENDER_LAYER.combinationCount(); j++)
               {
                   IBlockState innerState = state.withProperty(SuperBlock.SHADE_FLAGS, j);
                   
                   mapLocations.put(innerState,
                           new ModelResourceLocation(dispatcher.getDelegateForShadedFlags(j).getModelResourceString()));
               }
           }
       }
       return mapLocations;
   }

}