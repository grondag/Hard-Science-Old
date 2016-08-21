package grondag.adversity.niceblock;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.color.IColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelState;

import java.util.List;

import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class BigTexModelFactory extends ModelFactory
{

    /** typed convenience reference */
    private final BigTexController myController;
	
    private final TIntObjectHashMap<List<BakedQuad>> faceCache = new TIntObjectHashMap<List<BakedQuad>>(4096);
	
	/** Tells us which face to select for each block within a 16x16x16 
	 * space for up to 16 different meta values.
	 * 
	 * See also BigTexController.getClientShapeIndex.
	 */
	protected final static FaceSelector[] FACE_SELECTORS = new FaceSelector[16 * 4096];

	public BigTexModelFactory(ModelController controller)
	{
		super(controller);
		myController = (BigTexController)controller;
	}

    private int makeCacheKey(EnumFacing face, int faceIndex, int colorIndex)
    {
    	int key = face.ordinal();
    	int offset = EnumFacing.values().length;
    	key += faceIndex * offset;
    	// a big texture has 16x16=256 faces.  
    	// If meta variants enabled, then we have 16 versions of the 256.
    	offset *= myController.hasMetaVariants ? 256 * 16 : 256;
    	key += colorIndex * offset;
    	return key;
    }
	
	@Override
	public List<BakedQuad> getFaceQuads(ModelState modelState, IColorMapProvider colorProvider, EnumFacing face) 
	{
		if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        int clientShapeIndex = (int) modelState.getShapeIndex(controller.getRenderLayer());
        int faceIndex = FACE_SELECTORS[clientShapeIndex].selectors[face.ordinal()];
        int cacheKey = makeCacheKey(face, faceIndex, modelState.getColorIndex());
        
        List<BakedQuad> retVal = faceCache.get(cacheKey);
        
        if(retVal == null)
        {
			retVal = makeBigTexFace(colorProvider.getColorMap(modelState.getColorIndex()).getColor(EnumColorMap.BASE), faceIndex, face);
            synchronized(faceCache)
            {
                faceCache.put(cacheKey, retVal);
            }
        }
	
		return retVal;
	}

	@Override
	public List<BakedQuad> getItemQuads(ModelState modelState, IColorMapProvider colorProvider)
	{
		CubeInputs cubeInputs = new CubeInputs();
		cubeInputs.u0 = 0;
		cubeInputs.v0 = 0;
		cubeInputs.u1 = 1;
		cubeInputs.v1 = 1;
		cubeInputs.isItem = true;
		cubeInputs.isOverlay = controller.getRenderLayer() != BlockRenderLayer.SOLID;
		cubeInputs.color = colorProvider.getColorMap(modelState.getColorIndex()).getColor(EnumColorMap.BASE);
		cubeInputs.textureSprite = 
				Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(0));

		ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

		itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
		itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
		itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
		itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
		itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
		itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
		return itemBuilder.build();     
	}

	private List<BakedQuad> makeBigTexFace(int color, int faceIndex, EnumFacing face){

		int i = (faceIndex >> 4) & 15;
		int j = faceIndex & 15;

		CubeInputs cubeInputs = new CubeInputs();
		cubeInputs.color = color;
		cubeInputs.textureRotation = Rotation.values()[(faceIndex >> 10) & 3];

		boolean flipU = ((faceIndex >> 8) & 1) == 1;
		boolean flipV = ((faceIndex >> 9) & 1) == 1;
		cubeInputs.u0 = flipU ? 16 - i : i;
		cubeInputs.v0 = flipV ? 16 - j : j;
		cubeInputs.u1 = cubeInputs.u0 + (flipU ? -1 : 1);
		cubeInputs.v1 = cubeInputs.v0 + (flipV ? -1 : 1);

		cubeInputs.textureSprite = 
				Minecraft.getMinecraft().getTextureMapBlocks()
				.getAtlasSprite(controller.getTextureName(0));

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

}
