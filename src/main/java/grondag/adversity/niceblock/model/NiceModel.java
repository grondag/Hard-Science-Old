package grondag.adversity.niceblock.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.ModelLoader.UVLock;
import net.minecraftforge.client.model.b3d.B3DModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.obj.OBJModel.TextureCoordinate;
import net.minecraftforge.client.model.obj.OBJModel.Vertex;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Container for NiceBlock model variants retrieved by the block render
 * dispatcher. Places itself in the model registry during model bake and returns
 * appropriate baked models via handleBlockState and handleItemState
 *
 * All models are pre-baked during handleBakeEvent and kept in an array for
 * retrieval at run time. While this can mean many baked models are kept in
 * memory, they don't take up that much space and this ensures minimal
 * processing during the render loop.
 *
 * Each NiceBlock will have a separate NiceModel for each meta and each
 * NiceModel can contain one or hundreds of variants selected via
 * handleBlockState.
 *
 * NiceModel doesn't understand how extended state is determined, it just
 * expects to get a model recipe index and alternate index so that it can find
 * the correct array element.
 *
 * Does not subscribe to any events directly. Event handlers for texture stitch
 * and model bake need get/retain a reference to this instance and pass in the
 * events.
 *
 * Originally intended to have multiple NiceModel types but so far have only
 * needed this one because most of the functionality is delegated to the model
 * cook book.
 */
public abstract class NiceModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {


    /**
	 * Provides texture parameters.
	 */
	protected final int meta;

	protected TextureAtlasSprite particleTexture;

	public abstract IModelController getController();
	
	/**
	 * Create a model for this style/meta combination. Caller will
	 * typically create 16 of these per NiceBlock instance if all 16 
	 * metadata values are used.
	 *
	 * See class header and member descriptions for more info on what things do.
	 */
	protected NiceModel(int meta) {
		this.meta = meta;
	}

	private IBakedModel itemModel = null;
	
	/**
	 * The function parameter to bake method presumably provides a way to do
	 * fancy texture management via inversion of control, but we don't need that
	 * because we register all our textures in texture bake. This function
	 * simply provides access to the vanilla texture maps.
	 */
	protected Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>(){
		@Override
		public TextureAtlasSprite apply(ResourceLocation location)
		{
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
		}
	};
	
	/**
	 * Bakes all the models for this style/meta and caches them in array.
	 * Should happen after texture stitch and before any models are retrieved
	 * with handleBlockState.
	 */
	public abstract void handleBakeEvent(ModelBakeEvent event);

	/**
	 * Registers all textures that will be needed for this style/meta.
	 * Happens before model bake.
	 */
	public void handleTexturePreStitch(Pre event){
		for(String tex : getController().getAllTextures(meta)){
			event.map.registerSprite(new ResourceLocation(tex));
		}
	}
	
	/**
	 * Override to lookup retained references to texture sprites.
	 * Happens before model bake but after texture atlas is created.
	 */
	public void handleTexturePostStitch(Post event){
		particleTexture = event.map.getAtlasSprite(getController().getFirstTextureName(meta));
	}
	
	/**
	 * Does the heavy lifting for the ISmartBlockModel interface. Determines
	 * block state and returns the appropriate baked model. See class header for
	 * more info.
	 */
	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		IBakedModel retVal = null;

		// Really should ALWAYS be a NiceBlock instance but if someone goes
		// mucking about with the model registry crazy stuff could happen.
		if (state instanceof IExtendedBlockState && state.getBlock() instanceof NiceBlock) {
			
			IExtendedBlockState exState = (IExtendedBlockState) state;
			EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
			ModelRenderState renderState = exState.getValue(NiceBlock.MODEL_RENDER_STATE);

			if(getController().canRenderInLayer(layer)){
				retVal = getModelVariant(renderState.variant1);
			}
		}
	
	      // Provide a default to contain the damage if we derp it up.
        if(retVal == null) retVal = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();

		// May not be strictly needed, but doing in case something important happens in some model types.
		if (retVal instanceof ISmartBlockModel) {
			return ((ISmartBlockModel) retVal).handleBlockState(state);
		}

		return retVal;
	}
	
	/**
	 * The logic for handleBlockState.  
	 * Separate and public to enable composite models.
	 */
	public abstract IBakedModel getModelVariant(int variantID);
	
	/**
	 * Used for block-breaking particles.
	 */
	@Override
	public TextureAtlasSprite getParticleTexture(){
		return particleTexture;
	}
	
	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return null;
	}

	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public List getGeneralQuads() {
		return null;
	}

	
	/**
	 * Only used for item model because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	/**
	 * Only used for item model because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public boolean isGui3d() { 
		return true;
	}


	/**
	 * Should never be called because we provide a separate IFlexibleBakedModel instance in handleBlockState
	 */
	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

   @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

	
	////////////////////////////////////////////////////
    //  ITEM STUFFS
    ////////////////////////////////////////////////////
    
    /**
     * All subclass should provide vanilla baked quads for their item models.  
     * Colored quads are supported.  
     */
    protected abstract List<BakedQuad> getItemQuads();
    
   @Override
    public final IBakedModel handleItemState(ItemStack stack) {
       
       if(itemModel == null){
           
           /**
            * Enable perspective handling.
            */
               
           TRSRTransformation thirdperson = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                   new Vector3f(0, 1.5f / 16, -2.75f / 16),
                   TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
                   new Vector3f(0.375f, 0.375f, 0.375f),
                   null));

           IModelState state = new SimpleModelState(ImmutableMap.of(TransformType.THIRD_PERSON, thirdperson), Optional.of(TRSRTransformation.identity()));
           
           itemModel = 
               new IPerspectiveAwareModel.MapWrapper(
                   new IFlexibleBakedModel.Wrapper(
                       new SimpleBakedModel(
                       getItemQuads(), 
                       // face quads have to be present in list even in empty
                       new ImmutableList.Builder()
                           .add(new ImmutableList.Builder<BakedQuad>().build())
                           .add(new ImmutableList.Builder<BakedQuad>().build())
                           .add(new ImmutableList.Builder<BakedQuad>().build())
                           .add(new ImmutableList.Builder<BakedQuad>().build())
                           .add(new ImmutableList.Builder<BakedQuad>().build())
                           .add(new ImmutableList.Builder<BakedQuad>().build())
                           .build(),
                       NiceModel.this.isAmbientOcclusion(), 
                       NiceModel.this.isGui3d(),
                       NiceModel.this.particleTexture, 
                       NiceModel.this.getItemCameraTransforms()),
                       DefaultVertexFormats.ITEM),
                   state);
       }
       
       return itemModel;
    }
       
    /**
     * Helper method for sub-classes that don't generate their own item quads.
     */
    public static List getItemQuadsFromModel(IBakedModel model){
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face: EnumFacing.VALUES)
        {
            builder.addAll(model.getFaceQuads(face));
        }
        builder.addAll(model.getGeneralQuads());
        return builder.build();
    }
}
