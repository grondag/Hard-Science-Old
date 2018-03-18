package grondag.hard_science.superblock.varia;

import java.util.Map;

import com.google.common.collect.Maps;

import grondag.exotic_matter.model.BlockSubstance;
import grondag.exotic_matter.model.ISuperBlock;
import grondag.hard_science.superblock.block.SuperBlock;
import grondag.hard_science.superblock.block.SuperModelBlock;
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

       if (block instanceof ISuperBlock) {
           SuperBlock superBlock = (SuperBlock) block;
           for (int i = 0; i < 16; i++) {
               IBlockState state = superBlock.getDefaultState().withProperty(ISuperBlock.META, i);
                   
               if(block instanceof SuperModelBlock)
               {
                   for(BlockSubstance substance : BlockSubstance.all())
                   {
                       mapLocations.put(state.withProperty(SuperModelBlock.SUBSTANCE, substance.ordinal),
                               new ModelResourceLocation(dispatcher.getDelegate(superBlock).getModelResourceString()));
                   }
               }
               else
               {
                   mapLocations.put(state,
                           new ModelResourceLocation(dispatcher.getDelegate(superBlock).getModelResourceString()));
               }
           }
       }
       return mapLocations;
   }
}