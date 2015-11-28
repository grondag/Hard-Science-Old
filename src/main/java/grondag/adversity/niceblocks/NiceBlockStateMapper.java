package grondag.adversity.niceblocks;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class NiceBlockStateMapper extends DefaultStateMapper{
	
	public final static NiceBlockStateMapper instance = new NiceBlockStateMapper();
	
	@Override  
	public Map putStateModelLocations(Block block)
	{
		Map mapLocations = Maps.newLinkedHashMap();
		
    	if(block instanceof NiceBlock){
    		NiceBlock niceBlock = (NiceBlock) block;
    		
    		for (int i = 0; i < niceBlock.substances.length; i++){
				IBlockState state = niceBlock.getDefaultState().withProperty(NiceBlock.PROP_SUBSTANCE_INDEX, i);
				mapLocations.put(state, new ModelResourceLocation(niceBlock.style.getResourceLocationForSubstance(niceBlock.substances[i])));
    		}
    	}
    	return mapLocations;	
	}

}