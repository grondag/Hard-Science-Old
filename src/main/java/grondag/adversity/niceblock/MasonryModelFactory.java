package grondag.adversity.niceblock;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.SimpleJoin;
import grondag.adversity.library.joinstate.SimpleJoinFaceState;
import grondag.adversity.library.model.FaceQuadInputs;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

import java.util.List;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

public class MasonryModelFactory extends ModelFactory
{
    /** typed convenience reference */
    private final MasonryController myController;
    
    private final TIntObjectHashMap<List<BakedQuad>> faceCache = new TIntObjectHashMap<List<BakedQuad>>(4096);
    
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.values().length][SimpleJoinFaceState.values().length];

	public MasonryModelFactory(ModelController controller)
    {
        super(controller);
        this.myController = (MasonryController)controller;
    }

    private int makeCacheKey(EnumFacing face, SimpleJoinFaceState fjs, int colorIndex, int textureIndex)
    {
    	int key = face.ordinal();
    	int offset = EnumFacing.values().length;
    	key += fjs.ordinal() * offset;
    	offset *= SimpleJoinFaceState.values().length;
    	key += textureIndex * offset;
    	offset *= controller.getAlternateTextureCount();
    	key += colorIndex * offset;
    	return key;
    }
    
	@Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face) 
    {
    	if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
    	
        int clientShapeIndex = (int) modelState.getClientShapeIndex(controller.getRenderLayer().ordinal());
        int textureIndex = myController.getAltTextureFromModelIndex(clientShapeIndex);
        SimpleJoinFaceState fjs = SimpleJoinFaceState.find(face, new SimpleJoin(myController.getShapeFromModelIndex(clientShapeIndex)));
        
        int cacheKey = makeCacheKey(face, fjs, modelState.getColorIndex(), textureIndex);
        
        List<BakedQuad> retVal = faceCache.get(cacheKey);
        
        if(retVal == null)
        {
        	retVal = makeFace(face, fjs, colorProvider.getColor(modelState.getColorIndex()), textureIndex, false);
        	
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
       cubeInputs.color = colorMap.getColorMap(EnumColorMap.BORDER);
       cubeInputs.textureRotation = inputs.rotation;
       cubeInputs.rotateBottom = true;
       cubeInputs.u0 = inputs.flipU ? 16 : 0;
       cubeInputs.v0 = inputs.flipV ? 16 : 0;
       cubeInputs.u1 = inputs.flipU ? 0 : 16;
       cubeInputs.v1 = inputs.flipV ? 0 : 16;
       cubeInputs.isItem = isItem;
       cubeInputs.textureSprite = 
               Minecraft.getMinecraft().getTextureMapBlocks()
                   .getAtlasSprite(controller.getTextureName(altTextureIndex * 16 + inputs.textureOffset));
       
       return cubeInputs.makeFace(face);
	}
	
    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();
        
        for(EnumFacing face : EnumFacing.values())
        {
        	itemBuilder.addAll(makeFace(face, SimpleJoinFaceState.ALL, colorProvider.getColor(modelState.getColorIndex()), 0, true));
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
