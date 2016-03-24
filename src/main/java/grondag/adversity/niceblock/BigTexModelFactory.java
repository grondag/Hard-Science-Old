package grondag.adversity.niceblock;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.joinstate.FacadeFaceSelector;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelController;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;

public class BigTexModelFactory extends ModelFactory
{

	/**
	 * Dimensions are color index, facing (up, down, etc.), and baked face index.
	 * The face index bits in HSB order are:
	 *      2 bits texture rotation, meaning coordinates in 8 LSB are on a rotated texture
	 *      2 bits uv/flip indicators, meaning coordinates in 8 LSB are on a flipped texture
	 *      8 bit selector corresponding to uv texture coordinates
	 * All face quads are instantiated lazily.
	 */
	protected final List<BakedQuad>[][][] faceQuads;

	/** Dimensions are rotation index and facade index */
	protected final static FacadeFaceSelector[] FACADE_FACE_SELECTORS = new FacadeFaceSelector[16 * 4096];

	@SuppressWarnings("unchecked")
	public BigTexModelFactory(ModelController controller)
	{
		super(controller);
		faceQuads = (List<BakedQuad>[][][]) new List[ModelState.MAX_COLOR_INDEX][][];
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face) 
	{
		if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

		int colorIndex = modelState.getColorIndex();

		// allocate face quads for this color if not already done
		if(faceQuads[colorIndex] == null)
		{
			synchronized(faceQuads)
			{
				if(((BigTexController)controller).hasMetaVariants)
				{
					faceQuads[colorIndex] = (List<BakedQuad>[][]) new List[6][256 * 16];
				}
				else
				{
					faceQuads[colorIndex] = (List<BakedQuad>[][]) new List[6][256];
				}
			}
		}

		int facadeIndex = modelState.getClientShapeIndex(controller.getRenderLayer().ordinal());

		// ensure face is baked

		int faceIndex = FACADE_FACE_SELECTORS[facadeIndex].selectors[face.ordinal()];
		List<BakedQuad> retVal = faceQuads[colorIndex][face.ordinal()][faceIndex];
		
		if(retVal == null)
		{
			retVal = makeBigTexFace(colorProvider.getColor(colorIndex).getColorMap(EnumColorMap.BASE), faceIndex, face);
			synchronized(faceQuads)
			{
				faceQuads[colorIndex][face.ordinal()][faceIndex] = retVal;
			}
		}
		return retVal;
	}

	@Override
	public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
	{
		CubeInputs cubeInputs = new CubeInputs();
		cubeInputs.u0 = 0;
		cubeInputs.v0 = 0;
		cubeInputs.u1 = 1;
		cubeInputs.v1 = 1;
		cubeInputs.isItem = true;
		cubeInputs.isOverlay = controller.getRenderLayer() != BlockRenderLayer.SOLID;
		cubeInputs.color = colorProvider.getColor(modelState.getColorIndex()).getColorMap(EnumColorMap.BASE);
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
							FACADE_FACE_SELECTORS[facadeIndex] = new FacadeFaceSelector((x + yOff & 0xF) << 4 | z + yOff & 0xF, (~(x + yOff) & 0xF) << 4 | z + yOff & 0xF, ~(y + xOff) & 0xF
									| (~(z + xOff) & 0xF) << 4, ~(y + xOff) & 0xF | (z + xOff & 0xF) << 4, (~(x + zOff) & 0xF) << 4 | ~(y + zOff) & 0xF,
									(x + zOff & 0xF) << 4 | ~(y + zOff) & 0xF, faceIndexOffset);
							break;
						case ROTATE_90:
							FACADE_FACE_SELECTORS[facadeIndex] = new FacadeFaceSelector((z + yOff & 0xF) << 4 | ~(x + yOff) & 0xF, (z + yOff & 0xF) << 4 | x + yOff & 0xF, z + xOff & 0xF
									| (~(y + xOff) & 0xF) << 4, ~(z + xOff) & 0xF | (~(y + xOff) & 0xF) << 4, (~(y + zOff) & 0xF) << 4 | x + zOff & 0xF,
									(~(y + zOff) & 0xF) << 4 | ~(x + zOff) & 0xF, faceIndexOffset);
							break;
						case ROTATE_180:
							FACADE_FACE_SELECTORS[facadeIndex] = new FacadeFaceSelector((~(x + yOff) & 0xF) << 4 | ~(z + yOff) & 0xF, (x + yOff & 0xF) << 4 | ~(z + yOff) & 0xF, y + xOff & 0xF
									| (z + xOff & 0xF) << 4, y + xOff & 0xF | (~(z + xOff) & 0xF) << 4, (x + zOff & 0xF) << 4 | y + zOff & 0xF,
									(~(x + zOff) & 0xF) << 4 | y + zOff & 0xF, faceIndexOffset);
							break;
						case ROTATE_270:
							FACADE_FACE_SELECTORS[facadeIndex] = new FacadeFaceSelector((~(z + yOff) & 0xF) << 4 | x + yOff & 0xF, (~(z + yOff) & 0xF) << 4 | ~(x + yOff) & 0xF, ~(z + xOff) & 0xF
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

}
