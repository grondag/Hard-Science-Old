package grondag.adversity.niceblock.model;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.QuadContainer2;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory2;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;

import java.util.List;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class BigTexModelFactory2 extends ModelFactory2<ModelFactory2.ModelInputs>
{
    //TODO: use SimpleLoadingCache
    private final TIntObjectHashMap<List<BakedQuad>> faceCache = new TIntObjectHashMap<List<BakedQuad>>(4096);
    
    private final boolean hasMetaVariants;
	
	/** Tells us which face to select for each block within a 16x16x16 
	 * space for up to 16 different meta values.
	 * 
	 * See also BigTexController.getClientShapeIndex.
	 */
	protected final static FaceSelector[] FACE_SELECTORS = new FaceSelector[16 * 4096];

	public BigTexModelFactory2(ModelInputs modelInputs, ModelStateComponent<?,?>... components)
	{
		super(modelInputs, components);
		this.hasMetaVariants = this.bigTexComponent.useMetaVariants;
	}

	private List<BakedQuad> makeBigTexFace(ModelStateSetValue state, int faceIndex, EnumFacing face){

		int i = (faceIndex >> 4) & 15;
		int j = faceIndex & 15;
		
		CubeInputs cubeInputs = new CubeInputs();
		cubeInputs.color = state.getValue(colorComponent).getColor(EnumColorMap.BASE);
		cubeInputs.textureRotation = Rotation.values()[(faceIndex >> 10) & 3];

		boolean flipU = ((faceIndex >> 8) & 1) == 1;
		boolean flipV = ((faceIndex >> 9) & 1) == 1;
		cubeInputs.u0 = flipU ? 16 - i : i;
		cubeInputs.v0 = flipV ? 16 - j : j;
		cubeInputs.u1 = cubeInputs.u0 + (flipU ? -1 : 1);
		cubeInputs.v1 = cubeInputs.v0 + (flipV ? -1 : 1);

		cubeInputs.textureSprite = 
				Minecraft.getMinecraft().getTextureMapBlocks()
				.getAtlasSprite(buildTextureName(modelInputs.textureName, 0));

		return cubeInputs.makeFace(face);
	}

	static
	{
		int xOff = 0;
		for (int x = 0; x < 16; x++)
		{
			int yOff = 0;
			for (int y = 0; y < 16; y++)
			{
				int zOff = 0;
				for (int z = 0; z < 16; z++)
				{

					for (int meta = 0; meta < 16; meta++)
					{

						int facadeIndex = meta << 12 | x << 8 | y << 4 | z;
						int faceIndexOffset = meta * 256;

						switch (Rotation.values()[(meta >> 2) & 3])
						{
						case ROTATE_NONE:
							FACE_SELECTORS[facadeIndex] = new FaceSelector((x + yOff & 0xF) << 4 | z + yOff & 0xF, (~(x + yOff) & 0xF) << 4 | z + yOff & 0xF, ~(y + xOff) & 0xF
									| (~(z + xOff) & 0xF) << 4, ~(y + xOff) & 0xF | (z + xOff & 0xF) << 4, (~(x + zOff) & 0xF) << 4 | ~(y + zOff) & 0xF,
									(x + zOff & 0xF) << 4 | ~(y + zOff) & 0xF, faceIndexOffset);
							break;
						case ROTATE_90:
							FACE_SELECTORS[facadeIndex] = new FaceSelector((z + yOff & 0xF) << 4 | ~(x + yOff) & 0xF, (z + yOff & 0xF) << 4 | x + yOff & 0xF, z + xOff & 0xF
									| (~(y + xOff) & 0xF) << 4, ~(z + xOff) & 0xF | (~(y + xOff) & 0xF) << 4, (~(y + zOff) & 0xF) << 4 | x + zOff & 0xF,
									(~(y + zOff) & 0xF) << 4 | ~(x + zOff) & 0xF, faceIndexOffset);
							break;
						case ROTATE_180:
							FACE_SELECTORS[facadeIndex] = new FaceSelector((~(x + yOff) & 0xF) << 4 | ~(z + yOff) & 0xF, (x + yOff & 0xF) << 4 | ~(z + yOff) & 0xF, y + xOff & 0xF
									| (z + xOff & 0xF) << 4, y + xOff & 0xF | (~(z + xOff) & 0xF) << 4, (x + zOff & 0xF) << 4 | y + zOff & 0xF,
									(~(x + zOff) & 0xF) << 4 | y + zOff & 0xF, faceIndexOffset);
							break;
						case ROTATE_270:
							FACE_SELECTORS[facadeIndex] = new FaceSelector((~(z + yOff) & 0xF) << 4 | x + yOff & 0xF, (~(z + yOff) & 0xF) << 4 | ~(x + yOff) & 0xF, ~(z + xOff) & 0xF
									| (y + xOff & 0xF) << 4, z + xOff & 0xF | (y + xOff & 0xF) << 4, (y + zOff & 0xF) << 4 | ~(x + zOff) & 0xF,
									(y + zOff & 0xF) << 4 | x + zOff & 0xF, faceIndexOffset);
							break;
						}
					}

					zOff += 7;
				}
				yOff += 7;
			}
			xOff += 7;
		}
	}
	
	private static class FaceSelector
	{
	    public final short[] selectors = new short[EnumFacing.values().length];
	    
	    private FaceSelector(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace, int faceOffset) {
	        selectors[EnumFacing.UP.ordinal()] = (short) (upFace + faceOffset);
	        selectors[EnumFacing.DOWN.ordinal()] = (short) (downFace + faceOffset);
	        selectors[EnumFacing.EAST.ordinal()] = (short) (eastFace + faceOffset);
	        selectors[EnumFacing.WEST.ordinal()] = (short) (westFace + faceOffset);
	        selectors[EnumFacing.NORTH.ordinal()] = (short) (northFace + faceOffset);
	        selectors[EnumFacing.SOUTH.ordinal()] = (short) (southFace + faceOffset);
	    }
	    
	    private FaceSelector(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
	        this(upFace, downFace, eastFace, westFace, northFace, southFace, 0);
	    }

	}

    private int makeCacheKey(ModelStateSetValue state, int faceIndex, EnumFacing face)
    {
        int key = face.ordinal();
        
        key |= faceIndex << 3;
        
        // a big texture has 16x16=256 faces.  
        // If meta variants enabled, then we have 16 versions of the 256.
        key |= state.getValue(this.colorComponent).ordinal << (this.hasMetaVariants ? 11 : 7);

        return key;
    }
    
    @Override
    public QuadContainer2 getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer2.EMPTY_CONTAINER;
        QuadContainer2.QuadContainerBuilder builder = new QuadContainer2.QuadContainerBuilder();
        builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
        for(EnumFacing face : EnumFacing.values())
        {
            int faceIndex = FACE_SELECTORS[state.getValue(this.bigTexComponent)].selectors[face.ordinal()];
            int cacheKey = makeCacheKey(state, faceIndex, face);
            List<BakedQuad> faceQuads = faceCache.get(cacheKey);
            
            if(faceQuads == null)
            {
                faceQuads = makeBigTexFace(state, faceIndex, face);
                synchronized(faceCache)
                {
                    faceCache.put(cacheKey, faceQuads);
                }
            }
            builder.setQuads(face, faceQuads);
        }
        return builder.build();
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelStateSetValue state)
    {
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 1;
        cubeInputs.v1 = 1;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = modelInputs.renderLayer != BlockRenderLayer.SOLID;
        cubeInputs.color = state.getValue(colorComponent).getColor(EnumColorMap.BASE);
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(buildTextureName(modelInputs.textureName, 0));

        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
        return itemBuilder.build();     
    }

    @Override
    public String buildTextureName(String baseName, int offset)
    {
        return "adversity:blocks/" + baseName;
    }
}
