package grondag.adversity.niceblocks.client;

import grondag.adversity.niceblocks.NiceBlockStyle;
import grondag.adversity.niceblocks.NiceSubstance;


import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;

public class NiceModelCubeOne extends NiceModel {


	
	public NiceModelCubeOne(NiceBlockStyle style, NiceSubstance substance) {
		super(style, substance);
	}

	@Override
	public void handleBakeEvent(ModelBakeEvent event) throws IOException {

		int rotationCount = style.useRotationsAsAlternates ? 4 : 0;

		for(int alt = 0; alt < style.alternateCount; alt++){
	
			for(NiceBlockData.Rotation rotation: NiceBlockData.Rotation.values()){
				
				if(rotation.index < rotationCount){

					IRetexturableModel template = (IRetexturableModel)event.modelLoader.getModel(new ModelResourceLocation("adversity:block/cube_rotate_all_" + rotation.degrees ));
	
					Map<String, String> textures = Maps.newHashMap();
					textures.put("all", buildTextureName(this.textureBaseName, alt + style.textureIndex));
	
					IModel model=template.retexture(ImmutableMap.copyOf(textures));
	
					this.models[(alt * rotationCount) + rotation.index ][0]=model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, textureGetter);
				}
			}
		}

		
		super.handleBakeEvent(event);
	}

}
