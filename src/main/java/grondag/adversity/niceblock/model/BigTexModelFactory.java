package grondag.adversity.niceblock.model;
import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.QuadContainer;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelStateComponent;
import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;

public class BigTexModelFactory extends ModelFactory<ModelFactory.ModelInputs>
{

    private final Int2ObjectOpenHashMap<List<BakedQuad>> faceCache = new Int2ObjectOpenHashMap<List<BakedQuad>>(4096);
    
    public static enum BigTexScale
    {
        /** 16x16 */
        LARGE(1),
        /** 8x8 */
        MEDIUM(2),
        /** 4x4 */
        SMALL(4),
        /** 2x2 */
        TINY(8);
        
        protected final int sliceIncrement;
        
        private BigTexScale(int sliceIncrement)
        {
            this.sliceIncrement = sliceIncrement;
        }
      
    }
    private final BigTexScale scale;
	
	/** Tells us which face to select for each block within a 16x16x16 
	 * space for up to 16 different meta values.
	 * 
	 * See also BigTexController.getClientShapeIndex.
	 */
	protected final static FaceSelector[] FACE_SELECTORS = new FaceSelector[16 * 4096];

	public BigTexModelFactory(ModelInputs modelInputs, BigTexScale scale, ModelStateComponent<?,?>... components)
	{
		super(modelInputs, components);
		this.scale = scale;
	}

	private List<BakedQuad> makeBigTexFace(ModelStateSetValue state, int faceIndex, EnumFacing face)
	{
		CubeInputs cube = new CubeInputs();
		cube.color = state.getValue(colorComponent).getColor(EnumColorMap.BASE);
		cube.textureSprite = 
            Minecraft.getMinecraft().getTextureMapBlocks()
            .getAtlasSprite(buildTextureName(modelInputs.textureName, 0));

        int i = ((faceIndex >> 4) * scale.sliceIncrement) & 15;
        int j = (faceIndex * scale.sliceIncrement) & 15;
        
        //top 4 bits of faceIndex are meta
        // lower meta bits are uv flip and upper bits are rotation
        int meta = (faceIndex >> 8) & 15;       
        cube.textureRotation = Rotation.values()[(meta >> 2) & 3];
		
        boolean flipU = (meta & 1) == 1;
        boolean flipV = ((meta >> 1) & 1) == 1;
        cube.u0 = flipU ? 16 - i : i;
        cube.v0 = flipV ? 16 - j : j;
        cube.u1 = cube.u0 + (flipU ? -1 : 1) * scale.sliceIncrement;
        cube.v1 = cube.v0 + (flipV ? -1 : 1) * scale.sliceIncrement;
        
		return cube.makeFace(face);

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
						int faceIndexOffset = meta << 8;

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
        key |= state.getValue(this.colorComponent).ordinal << 15;

        return key;
    }
    
    @Override
    public QuadContainer getFaceQuads(ModelStateSetValue state, BlockRenderLayer renderLayer)
    {
        if(renderLayer != modelInputs.renderLayer) return QuadContainer.EMPTY_CONTAINER;
        QuadContainer.QuadContainerBuilder builder = new QuadContainer.QuadContainerBuilder();
        builder.setQuads(null, QuadFactory.EMPTY_QUAD_LIST);
        for(EnumFacing face : EnumFacing.values())
        {
            int faceIndex = FACE_SELECTORS[state.getValue(this.bigTexComponent).getIndex()].selectors[face.ordinal()];
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
    
    public static class BigTexInfo
    {
        private final int bits;
        
        public BigTexInfo(int bits)
        {
            this.bits = bits;
        }
        
        public static int getBits(int meta, BlockPos pos)
        {
            return meta << 12 | ((pos.getX() & 15) << 8) | ((pos.getY() & 15) << 4) | (pos.getZ() & 15);
        }

        public int getIndex() { return bits; }
        
        public int getX() { return (bits >> 8) & 15; }
        public int getY() { return (bits >> 4) & 15; }
        public int getZ() { return bits & 15; }
        public int getMeta() { return (bits >> 12) & 15; }

    }
}
