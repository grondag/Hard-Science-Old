package grondag.adversity.niceblock.model;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinFaceState;
import grondag.adversity.library.model.FaceQuadInputs;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateComponents;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class BorderModelFactory extends ModelFactory<ModelFactory.ModelInputs>
{
    //TODO: replace with SimpleLoadingCache
    private final TIntObjectHashMap<List<BakedQuad>> faceCache = new TIntObjectHashMap<List<BakedQuad>>(4096);
    
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.values().length][CornerJoinFaceState.values().length];
    
    /** Texture offsets */
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT = 0;
    private final static int TEXTURE_BOTTOM_LEFT = 1;
    private final static int TEXTURE_LEFT_RIGHT = 2;
    private final static int TEXTURE_BOTTOM = 3;
    private final static int TEXTURE_JOIN_NONE = 4;
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT_BR = 5;
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR = 6;    
    private final static int TEXTURE_BOTTOM_LEFT_BL = 7;
    private final static int TEXTURE_JOIN_ALL_TR = 8;
    private final static int TEXTURE_JOIN_ALL_TL_TR = 9;
    private final static int TEXTURE_JOIN_ALL_TR_BL = 10;
    private final static int TEXTURE_JOIN_ALL_TR_BL_BR = 11;
    private final static int TEXTURE_JOIN_ALL_ALL_CORNERS = 12;
    
    private final static int TEXTURE_COUNT = 13;
    private final static int TEXTURE_BLOCK_SIZE = 16;
 
    public BorderModelFactory(ModelInputs modelInputs, ModelStateComponent<?,?>... components)
    {
        super(modelInputs, components);
    }
    

    /**
     * Textures are generated in blocks of 16, but only 13 are used in each block.
     * Skip the unused ones so that we don't waste texture memory.
     */
    @Override
    public String[] getAllTextureNames()
    {
        String[] textures = new String[(int) this.textureComponent.getValueCount() * TEXTURE_COUNT];
        
        for(int i = 0; i < this.textureComponent.getValueCount(); i++)
        {
            for(int j = 0; j < TEXTURE_COUNT; j++)
            {
                textures[i * TEXTURE_COUNT + j] = this.buildTextureName(modelInputs.textureName, i * TEXTURE_BLOCK_SIZE + j);
            }
        }
        return textures;
    }

    //TODO: optimize
    private int makeCacheKey(EnumFacing face, CornerJoinFaceState fjs, int colorIndex, int textureIndex)
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
    
    public List<BakedQuad> makeFaceQuads(ModelStateSetValue state, EnumFacing face) 
    {
    	if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
    	
    	CornerJoinBlockState bjs = state.getValue(ModelStateComponents.CORNER_JOIN);
        int altTextureIndex = state.getValue(this.textureComponent);
        int cacheKey = makeCacheKey(face, bjs.getFaceJoinState(face), state.getValue(this.colorComponent).ordinal, altTextureIndex);
        List<BakedQuad> retVal = faceCache.get(cacheKey);

        if(retVal == null)
        {
            retVal = makeBorderFace(state.getValue(this.colorComponent).getColor(EnumColorMap.BORDER), altTextureIndex, bjs.getFaceJoinState(face), face);
            synchronized(faceCache)
            {
                faceCache.put(cacheKey, retVal);
            }
        }
   
        return retVal;
    }


    @Override
    public QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer.EMPTY_CONTAINER;
        QuadContainer.QuadContainerBuilder builder = new QuadContainer.QuadContainerBuilder();
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
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = modelInputs.renderLayer != BlockRenderLayer.SOLID;
        cubeInputs.color = state.getValue(this.colorComponent).getColor(EnumColorMap.BORDER);
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                buildTextureName(modelInputs.textureName, TEXTURE_JOIN_NONE));

        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
        return itemBuilder.build(); 
    }

    private List<BakedQuad> makeBorderFace(int color, int alternateTextureIndex, CornerJoinFaceState fjs, EnumFacing face){
        
        FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][fjs.ordinal()];
        
        if(inputs == null)
        {
            return Collections.emptyList();
        }
        
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.color = color;
        cubeInputs.textureRotation = inputs.rotation;
        cubeInputs.rotateBottom = false;
        cubeInputs.u0 = inputs.flipU ? 16 : 0;
        cubeInputs.v0 = inputs.flipV ? 16 : 0;
        cubeInputs.u1 = inputs.flipU ? 0 : 16;
        cubeInputs.v1 = inputs.flipV ? 0 : 16;
        cubeInputs.textureSprite = 
                Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(buildTextureName(modelInputs.textureName, alternateTextureIndex * TEXTURE_BLOCK_SIZE + inputs.textureOffset));
        
        return cubeInputs.makeFace(face);
    }
    
    static
    {
        for(EnumFacing face: EnumFacing.values()){
        	FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_NO_CORNERS.ordinal()] = null; // NO BORDER
        	FACE_INPUTS[face.ordinal()][CornerJoinFaceState.NO_FACE.ordinal()] = null; // NULL FACE
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.NONE.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_NONE, Rotation.ROTATE_NONE, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs( TEXTURE_LEFT_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs( TEXTURE_LEFT_RIGHT, Rotation.ROTATE_90, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_NONE, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_TL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_90, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_180, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_270, true, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_TL_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TL_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_TR_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_TL.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_RIGHT_TR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs( TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL, Rotation.ROTATE_90, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BL.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_270, false, false);
            
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BL_BR.ordinal()] = new FaceQuadInputs( TEXTURE_JOIN_ALL_ALL_CORNERS, Rotation.ROTATE_NONE, false, false);
        }
    }
}
