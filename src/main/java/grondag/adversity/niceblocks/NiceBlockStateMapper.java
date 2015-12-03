package grondag.adversity.niceblocks;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class NiceBlockStateMapper extends DefaultStateMapper{
	
	public final static NiceBlockStateMapper instance = new NiceBlockStateMapper();


//	@Override
//	protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState)
//	{
//		return ((NiceBlock)iBlockState.getBlock()).getModelResourceLocationForState(iBlockState);
//	}

	// public for use in NiceBlockRegistrar
	public ModelResourceLocation getModelResourceLocation(IBlockState blockState){
		return new ModelResourceLocation(  ((NiceBlock)blockState.getBlock()).name, "normal");
	}
	
	@Override  
	public Map putStateModelLocations(Block block)
	{
		Map mapLocations = Maps.newLinkedHashMap();
		
    	if(block instanceof NiceBlock){
    		NiceBlock niceBlock = (NiceBlock) block;
    		
    		for (int i = 0; i < niceBlock.substances.length; i++){
				IBlockState state = niceBlock.getDefaultState().withProperty(NiceBlock.PROP_SUBSTANCE_INDEX, i);
				mapLocations.put(state, this.getModelResourceLocation(state));
    		}
    	}
    	return mapLocations;	
	}

}