package grondag.adversity.superblock.varia;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.adversity.superblock.block.SuperBlock;
import grondag.adversity.superblock.block.SuperModelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
* SuperBlocks all use the same underlying dispatcher.  
*/
@SideOnly(Side.CLIENT)
public class SuperStateMapper extends DefaultStateMapper 
{
    private final SuperDispatcher dispatcher;
    
    public SuperStateMapper(SuperDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

   @Override
   public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) 
   {
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
                               new ModelResourceLocation(dispatcher.getDelegate((SuperBlock)block).getModelResourceString()));
                   }
               }
               else
               {
                   mapLocations.put(state,
                           new ModelResourceLocation(dispatcher.getDelegate((SuperBlock)block).getModelResourceString()));
               }
           }
       }
       return mapLocations;
   }
}