package grondag.adversity.niceblock.support;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
//import net.minecraft.client.resources.model.ModelResourceLocation;

import com.google.common.collect.Maps;

import grondag.adversity.niceblock.base.NiceBlock2;

/**
 * Niceblocks generally only have Substance as part of block state.
 * Each different substance state is mapped the model instance for
 * that block style and substance.
 * 
 */
public class NiceBlockStateMapper2 extends DefaultStateMapper {

	public final static NiceBlockStateMapper2 instance = new NiceBlockStateMapper2();

	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
		Map<IBlockState, ModelResourceLocation> mapLocations = Maps.newLinkedHashMap();

		if (block instanceof NiceBlock2) {
			NiceBlock2 niceBlock = (NiceBlock2) block;
			for (int i = 0; i < 16; i++) {
				IBlockState state = niceBlock.getDefaultState().withProperty(NiceBlock2.META, i);
				mapLocations.put(state,
						new ModelResourceLocation(niceBlock.dispatcher.getModelResourceString()));
			}
		}
		return mapLocations;
	}

}