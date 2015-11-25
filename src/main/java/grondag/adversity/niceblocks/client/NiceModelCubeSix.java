package grondag.adversity.niceblocks.client;

import grondag.adversity.niceblocks.NiceBlockStyle;
import grondag.adversity.niceblocks.NiceSubstance;
import grondag.adversity.niceblocks.client.NiceBlockData.TextureOffset;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;

public class NiceModelCubeSix extends NiceModel {



	public NiceModelCubeSix(NiceBlockStyle style, NiceSubstance substance) {
		super(style, substance);
	}

	@Override
	public void handleBakeEvent(ModelBakeEvent event) throws IOException {

		int rotationCount = style.useRotationsAsAlternates ? 4 : 0;
		
		for(int alt = 0; alt < style.alternateCount; alt++){

			int baseOffset = (style.textureCount * alt) + style.textureIndex;

			for(NiceBlockData.Rotation rotation: NiceBlockData.Rotation.values()){
				
				if(rotation.index < rotationCount){

					IRetexturableModel template = (IRetexturableModel)event.modelLoader.getModel(new ModelResourceLocation("adversity:block/cube_rotate_" + rotation.degrees ));

					for(int i =0 ; i < style.cookbook.getRecipeCount() ; i++){

						Map<String, String> textures = Maps.newHashMap();
						TextureOffset offset = NiceBlockData.CornerJoins.textureOffsets[rotation.index][i];
						textures.put("up", buildTextureName(textureBaseName, baseOffset + offset.up));
						textures.put("down", buildTextureName(textureBaseName, baseOffset + offset.down));
						textures.put("east", buildTextureName(textureBaseName, baseOffset + offset.east));
						textures.put("west", buildTextureName(textureBaseName, baseOffset + offset.west));
						textures.put("north", buildTextureName(textureBaseName, baseOffset + offset.north));
						textures.put("south", buildTextureName(textureBaseName, baseOffset + offset.south));

						IModel model=template.retexture(ImmutableMap.copyOf(textures));

						this.models[(alt * rotationCount) + rotation.index ][i]=model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, textureGetter);

					}
				}
			}		
		}

		super.handleBakeEvent(event);

	}

}
