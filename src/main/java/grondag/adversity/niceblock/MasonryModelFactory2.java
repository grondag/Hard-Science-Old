package grondag.adversity.niceblock;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.CornerJoinFaceState;
import grondag.adversity.library.joinstate.SimpleJoinFaceState;
import grondag.adversity.library.model.FaceQuadInputs;
import grondag.adversity.library.model.QuadContainer2;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory2;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;

import java.util.List;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class MasonryModelFactory2 extends ModelFactory2<ModelFactory2.ModelInputs>
{
    //TODO: use SimpleLoadingCache
    private final TIntObjectHashMap<List<BakedQuad>> faceCache = new TIntObjectHashMap<List<BakedQuad>>(4096);
    
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.values().length][SimpleJoinFaceState.values().length];

	public MasonryModelFactory2(ModelInputs modelInputs, ModelStateComponent<?,?>... components)
    {
        super(modelInputs, components);
    }

	   @Override
	    protected int getTextureCount()
	    {
	        return (int) this.textureComponent.getValueCount() * TEXTURE_COUNT;
	    }

	    
	    /**
	     * Textures are generated in blocks of 8, but only some are used in each block.
	     * Skip the unused ones so that we don't waste texture memory.
	     * 
	     * TODO: put this into parent?
	     */
	    @Override
	    public String[] getAllTextureNames()
	    {
	        String[] textures = new String[getTextureCount()];
	        
	        for(int i = 0; i < this.textureComponent.getValueCount(); i++)
	        {
	            for(int j = 0; j < TEXTURE_COUNT; j++)
	            {
	                textures[i * TEXTURE_COUNT + j] = this.buildTextureName(modelInputs.textureName, i * TEXTURE_BLOCK_SIZE + j);
	            }
	        }
	        return textures;
	    }

    private int makeCacheKey(EnumFacing face, SimpleJoinFaceState fjs, int colorIndex, int textureIndex)
    {
         int key = face.ordinal();
        int offset = EnumFacing.values().length;
        key += fjs.ordinal() * offset;
        offset *= CornerJoinFaceState.values().length;
        key += textureIndex * offset;
        offset *= this.textureComponent.getValueCount();
        key += colorIndex * offset;
        return key;
    }
    

    private List<BakedQuad> makeFaceQuads(ModelStateSetValue state, EnumFacing face) 
    {
    	
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
        
        SimpleJoinFaceState fjs = SimpleJoinFaceState.find(face, state.getValue(ModelStateComponents.MASONRY_JOIN));
        int altTextureIndex = state.getValue(this.textureComponent);
        int cacheKey = makeCacheKey(face, fjs, state.getValue(this.colorComponent).ordinal, altTextureIndex);
        List<BakedQuad> retVal = faceCache.get(cacheKey);

        if(retVal == null)
        {
            retVal = makeFace(face, fjs, state.getValue(this.colorComponent), altTextureIndex, false);
            synchronized(faceCache)
            {
                faceCache.put(cacheKey, retVal);
            }
        }
   
        return retVal;
    }

	private List<BakedQuad> makeFace(EnumFacing face, SimpleJoinFaceState fjs, ColorMap colorMap, int altTextureIndex, boolean isItem)
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
               .getAtlasSprite(buildTextureName(modelInputs.textureName, altTextureIndex * TEXTURE_BLOCK_SIZE + inputs.textureOffset));
       
       return cubeInputs.makeFace(face);
	}
	
    @Override
    public QuadContainer2 getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer2.EMPTY_CONTAINER;
        QuadContainer2.QuadContainerBuilder builder = new QuadContainer2.QuadContainerBuilder();
        builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
        for(EnumFacing face : EnumFacing.values())
        {
            builder.setQuads(face, makeFaceQuads(state, face));
        }
        return builder.build();
    }
    
    @Override
    public List<BakedQuad> getItemQuads(ModelStateSetValue state)
    {
        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();
        
        for(EnumFacing face : EnumFacing.values())
        {
        	itemBuilder.addAll(makeFace(face, SimpleJoinFaceState.ALL, state.getValue(this.colorComponent), 0, true));
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
    
    private static int TEXTURE_COUNT = Textures.values().length;
    private static int TEXTURE_BLOCK_SIZE = 8;
    
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
