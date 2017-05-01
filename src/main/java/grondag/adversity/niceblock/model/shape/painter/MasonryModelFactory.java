package grondag.adversity.niceblock.model.shape.painter;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.SimpleJoinFaceState;
import grondag.adversity.library.model.FaceQuadInputs;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.model.shape.ModelShape;
import grondag.adversity.niceblock.model.texture.TextureProvider.Texture.TextureState;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class MasonryModelFactory extends ModelFactory
{
    public static int TEXTURE_COUNT = Textures.values().length;
    public static int TEXTURE_BLOCK_SIZE = 8;
    
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.values().length][SimpleJoinFaceState.values().length];

	public MasonryModelFactory(ModelStateComponent<?,?>... components)
    {
        super(ModelShape.CUBE, components);
    }

    private List<BakedQuad> makeFaceQuads(TextureState texState, ModelStateSetValue state, EnumFacing face) 
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
        
        SimpleJoinFaceState fjs = SimpleJoinFaceState.find(face, state.getValue(ModelStateComponents.MASONRY_JOIN));
        int altTextureIndex = state.getValue(this.textureComponent);
        return makeFace(texState, face, fjs, state.getValue(this.colorComponent), altTextureIndex, false);
    }

	private List<BakedQuad> makeFace(TextureState texState, EnumFacing face, SimpleJoinFaceState fjs, ColorMap colorMap, int altTextureIndex, boolean isItem)
	{
        /** bump out slightly on item models to avoid depth-fighting */

       FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][fjs.ordinal()];
       
       if(inputs == null) return QuadFactory.EMPTY_QUAD_LIST;
           
       CubeInputs cubeInputs = new CubeInputs();
       cubeInputs.color = colorMap.getColor(EnumColorMap.BORDER);
       cubeInputs.textureRotation = inputs.rotation;
       cubeInputs.rotateBottom = true;
       cubeInputs.u0 = inputs.flipU ? 16 : 0;
       cubeInputs.v0 = inputs.flipV ? 16 : 0;
       cubeInputs.u1 = inputs.flipU ? 0 : 16;
       cubeInputs.v1 = inputs.flipV ? 0 : 16;
       cubeInputs.isItem = isItem;
       cubeInputs.textureSprite = 
               Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite(texState.buildTextureName(altTextureIndex, inputs.textureOffset));
       
       return cubeInputs.makeFace(face);
	}
	
    @Override
    public QuadContainer getFaceQuads(TextureState texState, ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != texState.renderLayer) return QuadContainer.EMPTY_CONTAINER;
        QuadContainer.QuadContainerBuilder builder = new QuadContainer.QuadContainerBuilder();
        builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
        for(EnumFacing face : EnumFacing.values())
        {
            builder.setQuads(face, makeFaceQuads(texState, state, face));
        }
        return builder.build();
    }
    
    @Override
    public List<BakedQuad> getItemQuads(TextureState texState, ModelStateSetValue state)
    {
        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();
        
        for(EnumFacing face : EnumFacing.values())
        {
        	itemBuilder.addAll(makeFace(texState, face, SimpleJoinFaceState.ALL, state.getValue(this.colorComponent), 0, true));
        }
        return itemBuilder.build(); 
    }
    
    private static enum Textures
    {
    	BOTTOM_LEFT_RIGHT,
    	BOTTOM_LEFT,
    	LEFT_RIGHT,
    	BOTTOM,
    	ALL;
    }
    
    static
    {
    	// mapping is unusual in that a join indicates a border IS present on texture
        for(EnumFacing face: EnumFacing.values()){
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.NONE.ordinal()] = null; //new ImmutableList.Builder<BakedQuad>().build(); // NO BORDER
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.NO_FACE.ordinal()] = null; //new ImmutableList.Builder<BakedQuad>().build(); // NO BORDER
        	
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT_RIGHT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_NONE, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_LEFT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_90, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT_RIGHT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_180, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_270, false, false);

        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_NONE, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_90, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_RIGHT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_180, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_270, false, false);

        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs( Textures.LEFT_RIGHT.ordinal(), Rotation.ROTATE_NONE, false, false);
        	FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs( Textures.LEFT_RIGHT.ordinal(), Rotation.ROTATE_90, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs( Textures.BOTTOM.ordinal(), Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM.ordinal(), Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP.ordinal()] = new FaceQuadInputs( Textures.BOTTOM.ordinal(), Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs( Textures.BOTTOM.ordinal(), Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.ALL.ordinal()] = new FaceQuadInputs( Textures.ALL.ordinal(), Rotation.ROTATE_NONE, false, false);
        }
    }
}
