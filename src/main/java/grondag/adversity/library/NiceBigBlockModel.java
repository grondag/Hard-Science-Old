package grondag.adversity.library;

import grondag.adversity.Adversity;
import grondag.adversity.ClientProxy;
import grondag.adversity.library.NiceBlockData.TextureOffsets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javafx.scene.shape.VertexFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NiceBigBlockModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {

	private final String baseName;	
	public final ModelResourceLocation modelResourceLocation;

	private IFlexibleBakedModel[][] bigBlocks = new IFlexibleBakedModel[12][386];

	public NiceBigBlockModel(String baseName){
		this.baseName = baseName;
		modelResourceLocation= new ModelResourceLocation("adversity:" + baseName + "_big_block");
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {

		Minecraft mc = Minecraft.getMinecraft();
		BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
		BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
		IBakedModel retVal = blockModelShapes.getModelForState(Blocks.bedrock.getDefaultState());

		if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock2) {
			IExtendedBlockState exState = (IExtendedBlockState) state;
			retVal = bigBlocks[exState.getValue(NiceBlock2.PROP_ALTERNATE)][exState.getValue(NiceBlock2.PROP_SCENARIO)];
		}
		return retVal;
	}

	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getGeneralQuads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGui3d() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		// TODO Auto-generated method stub
		return null;
	}

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) throws IOException
	{
		event.modelRegistry.putObject(modelResourceLocation, this);
		//event.modelRegistry.putObject(ClientProxy.itemLocation, customModel);

		Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
		{
			public TextureAtlasSprite apply(ResourceLocation location)
			{
				return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
			}
		};




		for(int alt = 0; alt < 3; alt++){

			int baseOffset = NiceBlockData.CornerJoins.alternateOffsets[alt];

			for(NiceBlockData.Rotation rotation: NiceBlockData.Rotation.values()){

				IRetexturableModel template = (IRetexturableModel)event.modelLoader.getModel(new ModelResourceLocation("adversity:block/cube_rotate_" + rotation.degrees ));

				for(int i =0 ; i < 386 ; i++){

					Map<String, String> textures = Maps.newHashMap();
					TextureOffsets scenarioOffsets = NiceBlockData.CornerJoins.scenarioOffsets[rotation.index][i];
					textures.put("up", TextureLoader.buildTextureName(baseName, baseOffset + scenarioOffsets.up));
					textures.put("down", TextureLoader.buildTextureName(baseName, baseOffset + scenarioOffsets.down));
					textures.put("east", TextureLoader.buildTextureName(baseName, baseOffset + scenarioOffsets.east));
					textures.put("west", TextureLoader.buildTextureName(baseName, baseOffset + scenarioOffsets.west));
					textures.put("north", TextureLoader.buildTextureName(baseName, baseOffset + scenarioOffsets.north));
					textures.put("south", TextureLoader.buildTextureName(baseName, baseOffset + scenarioOffsets.south));

					IModel model=template.retexture(ImmutableMap.copyOf(textures));

					bigBlocks[(alt * 4) + rotation.index ][i]=model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, textureGetter);

				}
			}
		}




	}


}
