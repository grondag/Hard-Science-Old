package grondag.adversity.niceblock.support;

import grondag.adversity.niceblock.NiceBlock;
import grondag.adversity.niceblock.NiceBlockRegistrar;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;

import com.google.common.collect.Maps;

/**
 * Niceblocks generally only have Substance as part of block state.
 * Each different substance state is mapped the model instance for
 * that block style and substance.
 * 
 */
public class NiceBlockStateMapper extends DefaultStateMapper {

	public final static NiceBlockStateMapper instance = new NiceBlockStateMapper();

	@Override
	public Map putStateModelLocations(Block block) {
		Map mapLocations = Maps.newLinkedHashMap();

		if (block instanceof NiceBlock) {
			NiceBlock niceBlock = (NiceBlock) block;

			for (int i = 0; i < niceBlock.substances.length; i++) {
				IBlockState state = niceBlock.getDefaultState().withProperty(NiceBlock.SUBSTANCE_INDEX, i);
				mapLocations.put(state,
						new ModelResourceLocation(NiceBlockRegistrar.getModelResourceNameFromMeta(niceBlock, i)));
			}
		}
		return mapLocations;
	}

}