package grondag.adversity.niceblock.model;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.event.ModelBakeEvent;
import grondag.adversity.Adversity;
import grondag.adversity.niceblock.NiceColor;
import grondag.adversity.niceblock.model.IModelController.Rotation;
import grondag.adversity.niceblock.model.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.model.QuadFactory.Vertex;

public class NiceModelBorder extends NiceModel {
	
	protected final ModelControllerBorder controller;

	/**
	 * Dimensions are alternate and face ID.
	 */
	protected final List<BakedQuad>[][] faceQuads;

	/**
	 * Dimensions are alternate and face ID.
	 */
	protected final BorderFacade[] facadeModels;
	
    protected List<BakedQuad> itemQuads;
    
	protected final NiceColor color;

	protected NiceModelBorder(ModelControllerBorder controller, int meta, NiceColor color) {
		super(meta);
		this.color = color;
		this.controller = controller;
		faceQuads = new List[6][48];
		facadeModels = new BorderFacade[386];
	}

	@Override
	public IModelController getController() {
		return controller;
	}

	private List<BakedQuad> makeBorderFace(int textureOffset, Rotation rotation, boolean flipU, boolean flipV, EnumFacing face){
	
	    CubeInputs cubeInputs = new CubeInputs();
	    cubeInputs.color = color.border;
	    cubeInputs.textureRotation = rotation;
	    cubeInputs.rotateBottom = true;
	    cubeInputs.u0 = flipU ? 16 : 0;
	    cubeInputs.v0 = flipV ? 16 : 0;
	    cubeInputs.u1 = flipU ? 0 : 16;
	    cubeInputs.v1 = flipV ? 0 : 16;
		cubeInputs.textureSprite = 
				Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(meta, textureOffset));
		
		return cubeInputs.makeFace(face);
	}
	
	private void makeItemQuads(){
	    
        CubeInputs cubeInputs = new CubeInputs();
        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = controller.renderLayer != EnumWorldBlockLayer.SOLID;
        cubeInputs.color = color.border;
        // offset 4 is all borders
        cubeInputs.textureSprite = 
                Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(meta, 4));

        ImmutableList.Builder<BakedQuad> itemBuilder = new ImmutableList.Builder<BakedQuad>();

        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.UP));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.DOWN));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.EAST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.WEST));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.NORTH));
        itemBuilder.addAll(cubeInputs.makeFace(EnumFacing.SOUTH));
        itemQuads = itemBuilder.build();

	}
	
	@Override
	public void handleBakeEvent(ModelBakeEvent event) {

	    makeItemQuads();
	    
		for(EnumFacing face: EnumFacing.values()){
			faceQuads[face.ordinal()][0] = makeBorderFace( 4, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][1] = makeBorderFace( 3, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][2] = makeBorderFace( 3, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][3] = makeBorderFace( 1, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][4] = makeBorderFace( 3, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][5] = makeBorderFace( 2, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][6] = makeBorderFace( 1, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][7] = makeBorderFace( 0, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][8] = makeBorderFace( 3, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][9] = makeBorderFace( 1, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][10] = makeBorderFace( 2, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][11] = makeBorderFace( 0, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][12] = makeBorderFace( 1, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][13] = makeBorderFace( 0, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][14] = makeBorderFace( 0, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][15] = new ImmutableList.Builder<BakedQuad>().build(); // NO BORDER
			faceQuads[face.ordinal()][16] = makeBorderFace( 7, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][17] = makeBorderFace( 7, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][18] = makeBorderFace( 7, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][19] = makeBorderFace( 7, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][20] = makeBorderFace( 5, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][21] = makeBorderFace( 5, Rotation.ROTATE_270, true, false, face);
			faceQuads[face.ordinal()][22] = makeBorderFace( 6, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][23] = makeBorderFace( 5, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][24] = makeBorderFace( 5, Rotation.ROTATE_180, true, false, face);
			faceQuads[face.ordinal()][25] = makeBorderFace( 6, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][26] = makeBorderFace( 5, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][27] = makeBorderFace( 5, Rotation.ROTATE_90, true, false, face);
			faceQuads[face.ordinal()][28] = makeBorderFace( 6, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][29] = makeBorderFace( 5, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][30] = makeBorderFace( 5, Rotation.ROTATE_NONE, true, false, face);
			faceQuads[face.ordinal()][31] = makeBorderFace( 6, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][32] = new ImmutableList.Builder<BakedQuad>().build(); // NULL FACE
			faceQuads[face.ordinal()][33] = makeBorderFace( 8, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][34] = makeBorderFace( 8, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][35] = makeBorderFace( 9, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][36] = makeBorderFace( 8, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][37] = makeBorderFace( 10, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][38] = makeBorderFace( 9, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][39] = makeBorderFace( 11, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][40] = makeBorderFace( 8, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][41] = makeBorderFace( 9, Rotation.ROTATE_270, false, false, face);
			faceQuads[face.ordinal()][42] = makeBorderFace( 10, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][43] = makeBorderFace( 11, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][44] = makeBorderFace( 9, Rotation.ROTATE_180, false, false, face);
			faceQuads[face.ordinal()][45] = makeBorderFace( 11, Rotation.ROTATE_90, false, false, face);
			faceQuads[face.ordinal()][46] = makeBorderFace( 11, Rotation.ROTATE_NONE, false, false, face);
			faceQuads[face.ordinal()][47] = makeBorderFace( 12, Rotation.ROTATE_NONE, false, false, face);
		}
		
		facadeModels[0] = new BorderFacade(0, 0, 0, 0, 0, 0);
		facadeModels[1] = new BorderFacade(32, 0, 1, 1, 1, 1);
		facadeModels[2] = new BorderFacade(0, 32, 4, 4, 4, 4);
		facadeModels[3] = new BorderFacade(2, 2, 32, 0, 8, 2);
		facadeModels[4] = new BorderFacade(8, 8, 0, 32, 2, 8);
		facadeModels[5] = new BorderFacade(1, 4, 2, 8, 32, 0);
		facadeModels[6] = new BorderFacade(4, 1, 8, 2, 0, 32);
		facadeModels[7] = new BorderFacade(32, 32, 5, 5, 5, 5);
		facadeModels[8] = new BorderFacade(10, 10, 32, 32, 10, 10);
		facadeModels[9] = new BorderFacade(5, 5, 10, 10, 32, 32);
		facadeModels[10] = new BorderFacade(32, 2, 32, 1, 9, 3);
		facadeModels[11] = new BorderFacade(32, 2, 32, 1, 19, 16);
		facadeModels[12] = new BorderFacade(32, 8, 1, 32, 3, 9);
		facadeModels[13] = new BorderFacade(32, 8, 1, 32, 16, 19);
		facadeModels[14] = new BorderFacade(32, 4, 3, 9, 32, 1);
		facadeModels[15] = new BorderFacade(32, 4, 16, 19, 32, 1);
		facadeModels[16] = new BorderFacade(32, 1, 9, 3, 1, 32);
		facadeModels[17] = new BorderFacade(32, 1, 19, 16, 1, 32);
		facadeModels[18] = new BorderFacade(2, 32, 32, 4, 12, 6);
		facadeModels[19] = new BorderFacade(2, 32, 32, 4, 18, 17);
		facadeModels[20] = new BorderFacade(8, 32, 4, 32, 6, 12);
		facadeModels[21] = new BorderFacade(8, 32, 4, 32, 17, 18);
		facadeModels[22] = new BorderFacade(1, 32, 6, 12, 32, 4);
		facadeModels[23] = new BorderFacade(1, 32, 17, 18, 32, 4);
		facadeModels[24] = new BorderFacade(4, 32, 12, 6, 4, 32);
		facadeModels[25] = new BorderFacade(4, 32, 18, 17, 4, 32);
		facadeModels[26] = new BorderFacade(3, 6, 32, 8, 32, 2);
		facadeModels[27] = new BorderFacade(16, 17, 32, 8, 32, 2);
		facadeModels[28] = new BorderFacade(6, 3, 32, 2, 8, 32);
		facadeModels[29] = new BorderFacade(17, 16, 32, 2, 8, 32);
		facadeModels[30] = new BorderFacade(9, 12, 2, 32, 32, 8);
		facadeModels[31] = new BorderFacade(19, 18, 2, 32, 32, 8);
		facadeModels[32] = new BorderFacade(12, 9, 8, 32, 2, 32);
		facadeModels[33] = new BorderFacade(18, 19, 8, 32, 2, 32);
		facadeModels[34] = new BorderFacade(32, 10, 32, 32, 11, 11);
		facadeModels[35] = new BorderFacade(32, 10, 32, 32, 23, 24);
		facadeModels[36] = new BorderFacade(32, 10, 32, 32, 24, 23);
		facadeModels[37] = new BorderFacade(32, 10, 32, 32, 25, 25);
		facadeModels[38] = new BorderFacade(32, 5, 11, 11, 32, 32);
		facadeModels[39] = new BorderFacade(32, 5, 24, 23, 32, 32);
		facadeModels[40] = new BorderFacade(32, 5, 23, 24, 32, 32);
		facadeModels[41] = new BorderFacade(32, 5, 25, 25, 32, 32);
		facadeModels[42] = new BorderFacade(10, 32, 32, 32, 14, 14);
		facadeModels[43] = new BorderFacade(10, 32, 32, 32, 30, 29);
		facadeModels[44] = new BorderFacade(10, 32, 32, 32, 29, 30);
		facadeModels[45] = new BorderFacade(10, 32, 32, 32, 31, 31);
		facadeModels[46] = new BorderFacade(5, 32, 14, 14, 32, 32);
		facadeModels[47] = new BorderFacade(5, 32, 29, 30, 32, 32);
		facadeModels[48] = new BorderFacade(5, 32, 30, 29, 32, 32);
		facadeModels[49] = new BorderFacade(5, 32, 31, 31, 32, 32);
		facadeModels[50] = new BorderFacade(11, 14, 32, 32, 32, 10);
		facadeModels[51] = new BorderFacade(24, 29, 32, 32, 32, 10);
		facadeModels[52] = new BorderFacade(23, 30, 32, 32, 32, 10);
		facadeModels[53] = new BorderFacade(25, 31, 32, 32, 32, 10);
		facadeModels[54] = new BorderFacade(32, 32, 7, 13, 32, 5);
		facadeModels[55] = new BorderFacade(32, 32, 20, 27, 32, 5);
		facadeModels[56] = new BorderFacade(32, 32, 21, 26, 32, 5);
		facadeModels[57] = new BorderFacade(32, 32, 22, 28, 32, 5);
		facadeModels[58] = new BorderFacade(14, 11, 32, 32, 10, 32);
		facadeModels[59] = new BorderFacade(29, 24, 32, 32, 10, 32);
		facadeModels[60] = new BorderFacade(30, 23, 32, 32, 10, 32);
		facadeModels[61] = new BorderFacade(31, 25, 32, 32, 10, 32);
		facadeModels[62] = new BorderFacade(32, 32, 13, 7, 5, 32);
		facadeModels[63] = new BorderFacade(32, 32, 27, 20, 5, 32);
		facadeModels[64] = new BorderFacade(32, 32, 26, 21, 5, 32);
		facadeModels[65] = new BorderFacade(32, 32, 28, 22, 5, 32);
		facadeModels[66] = new BorderFacade(32, 32, 32, 5, 13, 7);
		facadeModels[67] = new BorderFacade(32, 32, 32, 5, 27, 20);
		facadeModels[68] = new BorderFacade(32, 32, 32, 5, 26, 21);
		facadeModels[69] = new BorderFacade(32, 32, 32, 5, 28, 22);
		facadeModels[70] = new BorderFacade(7, 7, 32, 10, 32, 32);
		facadeModels[71] = new BorderFacade(20, 21, 32, 10, 32, 32);
		facadeModels[72] = new BorderFacade(21, 20, 32, 10, 32, 32);
		facadeModels[73] = new BorderFacade(22, 22, 32, 10, 32, 32);
		facadeModels[74] = new BorderFacade(32, 32, 5, 32, 7, 13);
		facadeModels[75] = new BorderFacade(32, 32, 5, 32, 20, 27);
		facadeModels[76] = new BorderFacade(32, 32, 5, 32, 21, 26);
		facadeModels[77] = new BorderFacade(32, 32, 5, 32, 22, 28);
		facadeModels[78] = new BorderFacade(13, 13, 10, 32, 32, 32);
		facadeModels[79] = new BorderFacade(26, 27, 10, 32, 32, 32);
		facadeModels[80] = new BorderFacade(27, 26, 10, 32, 32, 32);
		facadeModels[81] = new BorderFacade(28, 28, 10, 32, 32, 32);
		facadeModels[82] = new BorderFacade(32, 6, 32, 9, 32, 3);
		facadeModels[83] = new BorderFacade(32, 6, 32, 19, 32, 3);
		facadeModels[84] = new BorderFacade(32, 6, 32, 9, 32, 16);
		facadeModels[85] = new BorderFacade(32, 6, 32, 19, 32, 16);
		facadeModels[86] = new BorderFacade(32, 17, 32, 9, 32, 3);
		facadeModels[87] = new BorderFacade(32, 17, 32, 19, 32, 3);
		facadeModels[88] = new BorderFacade(32, 17, 32, 9, 32, 16);
		facadeModels[89] = new BorderFacade(32, 17, 32, 19, 32, 16);
		facadeModels[90] = new BorderFacade(32, 3, 32, 3, 9, 32);
		facadeModels[91] = new BorderFacade(32, 3, 32, 3, 19, 32);
		facadeModels[92] = new BorderFacade(32, 3, 32, 16, 9, 32);
		facadeModels[93] = new BorderFacade(32, 3, 32, 16, 19, 32);
		facadeModels[94] = new BorderFacade(32, 16, 32, 3, 9, 32);
		facadeModels[95] = new BorderFacade(32, 16, 32, 3, 19, 32);
		facadeModels[96] = new BorderFacade(32, 16, 32, 16, 9, 32);
		facadeModels[97] = new BorderFacade(32, 16, 32, 16, 19, 32);
		facadeModels[98] = new BorderFacade(32, 12, 3, 32, 32, 9);
		facadeModels[99] = new BorderFacade(32, 12, 16, 32, 32, 9);
		facadeModels[100] = new BorderFacade(32, 12, 3, 32, 32, 19);
		facadeModels[101] = new BorderFacade(32, 12, 16, 32, 32, 19);
		facadeModels[102] = new BorderFacade(32, 18, 3, 32, 32, 9);
		facadeModels[103] = new BorderFacade(32, 18, 16, 32, 32, 9);
		facadeModels[104] = new BorderFacade(32, 18, 3, 32, 32, 19);
		facadeModels[105] = new BorderFacade(32, 18, 16, 32, 32, 19);
		facadeModels[106] = new BorderFacade(32, 9, 9, 32, 3, 32);
		facadeModels[107] = new BorderFacade(32, 9, 19, 32, 3, 32);
		facadeModels[108] = new BorderFacade(32, 9, 9, 32, 16, 32);
		facadeModels[109] = new BorderFacade(32, 9, 19, 32, 16, 32);
		facadeModels[110] = new BorderFacade(32, 19, 9, 32, 3, 32);
		facadeModels[111] = new BorderFacade(32, 19, 19, 32, 3, 32);
		facadeModels[112] = new BorderFacade(32, 19, 9, 32, 16, 32);
		facadeModels[113] = new BorderFacade(32, 19, 19, 32, 16, 32);
		facadeModels[114] = new BorderFacade(3, 32, 32, 12, 32, 6);
		facadeModels[115] = new BorderFacade(3, 32, 32, 18, 32, 6);
		facadeModels[116] = new BorderFacade(3, 32, 32, 12, 32, 17);
		facadeModels[117] = new BorderFacade(3, 32, 32, 18, 32, 17);
		facadeModels[118] = new BorderFacade(16, 32, 32, 12, 32, 6);
		facadeModels[119] = new BorderFacade(16, 32, 32, 18, 32, 6);
		facadeModels[120] = new BorderFacade(16, 32, 32, 12, 32, 17);
		facadeModels[121] = new BorderFacade(16, 32, 32, 18, 32, 17);
		facadeModels[122] = new BorderFacade(6, 32, 32, 6, 12, 32);
		facadeModels[123] = new BorderFacade(6, 32, 32, 6, 18, 32);
		facadeModels[124] = new BorderFacade(6, 32, 32, 17, 12, 32);
		facadeModels[125] = new BorderFacade(6, 32, 32, 17, 18, 32);
		facadeModels[126] = new BorderFacade(17, 32, 32, 6, 12, 32);
		facadeModels[127] = new BorderFacade(17, 32, 32, 6, 18, 32);
		facadeModels[128] = new BorderFacade(17, 32, 32, 17, 12, 32);
		facadeModels[129] = new BorderFacade(17, 32, 32, 17, 18, 32);
		facadeModels[130] = new BorderFacade(9, 32, 6, 32, 32, 12);
		facadeModels[131] = new BorderFacade(9, 32, 17, 32, 32, 12);
		facadeModels[132] = new BorderFacade(9, 32, 6, 32, 32, 18);
		facadeModels[133] = new BorderFacade(9, 32, 17, 32, 32, 18);
		facadeModels[134] = new BorderFacade(19, 32, 6, 32, 32, 12);
		facadeModels[135] = new BorderFacade(19, 32, 17, 32, 32, 12);
		facadeModels[136] = new BorderFacade(19, 32, 6, 32, 32, 18);
		facadeModels[137] = new BorderFacade(19, 32, 17, 32, 32, 18);
		facadeModels[138] = new BorderFacade(12, 32, 12, 32, 6, 32);
		facadeModels[139] = new BorderFacade(12, 32, 18, 32, 6, 32);
		facadeModels[140] = new BorderFacade(12, 32, 12, 32, 17, 32);
		facadeModels[141] = new BorderFacade(12, 32, 18, 32, 17, 32);
		facadeModels[142] = new BorderFacade(18, 32, 12, 32, 6, 32);
		facadeModels[143] = new BorderFacade(18, 32, 18, 32, 6, 32);
		facadeModels[144] = new BorderFacade(18, 32, 12, 32, 17, 32);
		facadeModels[145] = new BorderFacade(18, 32, 18, 32, 17, 32);
		facadeModels[146] = new BorderFacade(15, 15, 32, 32, 32, 32);
		facadeModels[147] = new BorderFacade(34, 36, 32, 32, 32, 32);
		facadeModels[148] = new BorderFacade(36, 34, 32, 32, 32, 32);
		facadeModels[149] = new BorderFacade(38, 38, 32, 32, 32, 32);
		facadeModels[150] = new BorderFacade(40, 33, 32, 32, 32, 32);
		facadeModels[151] = new BorderFacade(42, 37, 32, 32, 32, 32);
		facadeModels[152] = new BorderFacade(44, 35, 32, 32, 32, 32);
		facadeModels[153] = new BorderFacade(46, 39, 32, 32, 32, 32);
		facadeModels[154] = new BorderFacade(33, 40, 32, 32, 32, 32);
		facadeModels[155] = new BorderFacade(35, 44, 32, 32, 32, 32);
		facadeModels[156] = new BorderFacade(37, 42, 32, 32, 32, 32);
		facadeModels[157] = new BorderFacade(39, 46, 32, 32, 32, 32);
		facadeModels[158] = new BorderFacade(41, 41, 32, 32, 32, 32);
		facadeModels[159] = new BorderFacade(43, 45, 32, 32, 32, 32);
		facadeModels[160] = new BorderFacade(45, 43, 32, 32, 32, 32);
		facadeModels[161] = new BorderFacade(47, 47, 32, 32, 32, 32);
		facadeModels[162] = new BorderFacade(32, 32, 32, 32, 15, 15);
		facadeModels[163] = new BorderFacade(32, 32, 32, 32, 33, 34);
		facadeModels[164] = new BorderFacade(32, 32, 32, 32, 34, 33);
		facadeModels[165] = new BorderFacade(32, 32, 32, 32, 35, 35);
		facadeModels[166] = new BorderFacade(32, 32, 32, 32, 40, 36);
		facadeModels[167] = new BorderFacade(32, 32, 32, 32, 41, 38);
		facadeModels[168] = new BorderFacade(32, 32, 32, 32, 42, 37);
		facadeModels[169] = new BorderFacade(32, 32, 32, 32, 43, 39);
		facadeModels[170] = new BorderFacade(32, 32, 32, 32, 36, 40);
		facadeModels[171] = new BorderFacade(32, 32, 32, 32, 37, 42);
		facadeModels[172] = new BorderFacade(32, 32, 32, 32, 38, 41);
		facadeModels[173] = new BorderFacade(32, 32, 32, 32, 39, 43);
		facadeModels[174] = new BorderFacade(32, 32, 32, 32, 44, 44);
		facadeModels[175] = new BorderFacade(32, 32, 32, 32, 45, 46);
		facadeModels[176] = new BorderFacade(32, 32, 32, 32, 46, 45);
		facadeModels[177] = new BorderFacade(32, 32, 32, 32, 47, 47);
		facadeModels[178] = new BorderFacade(32, 32, 15, 15, 32, 32);
		facadeModels[179] = new BorderFacade(32, 32, 34, 33, 32, 32);
		facadeModels[180] = new BorderFacade(32, 32, 33, 34, 32, 32);
		facadeModels[181] = new BorderFacade(32, 32, 35, 35, 32, 32);
		facadeModels[182] = new BorderFacade(32, 32, 36, 40, 32, 32);
		facadeModels[183] = new BorderFacade(32, 32, 38, 41, 32, 32);
		facadeModels[184] = new BorderFacade(32, 32, 37, 42, 32, 32);
		facadeModels[185] = new BorderFacade(32, 32, 39, 43, 32, 32);
		facadeModels[186] = new BorderFacade(32, 32, 40, 36, 32, 32);
		facadeModels[187] = new BorderFacade(32, 32, 42, 37, 32, 32);
		facadeModels[188] = new BorderFacade(32, 32, 41, 38, 32, 32);
		facadeModels[189] = new BorderFacade(32, 32, 43, 39, 32, 32);
		facadeModels[190] = new BorderFacade(32, 32, 44, 44, 32, 32);
		facadeModels[191] = new BorderFacade(32, 32, 46, 45, 32, 32);
		facadeModels[192] = new BorderFacade(32, 32, 45, 46, 32, 32);
		facadeModels[193] = new BorderFacade(32, 32, 47, 47, 32, 32);
		facadeModels[194] = new BorderFacade(32, 13, 11, 32, 32, 32);
		facadeModels[195] = new BorderFacade(32, 13, 24, 32, 32, 32);
		facadeModels[196] = new BorderFacade(32, 13, 23, 32, 32, 32);
		facadeModels[197] = new BorderFacade(32, 13, 25, 32, 32, 32);
		facadeModels[198] = new BorderFacade(32, 27, 11, 32, 32, 32);
		facadeModels[199] = new BorderFacade(32, 27, 24, 32, 32, 32);
		facadeModels[200] = new BorderFacade(32, 27, 23, 32, 32, 32);
		facadeModels[201] = new BorderFacade(32, 27, 25, 32, 32, 32);
		facadeModels[202] = new BorderFacade(32, 26, 11, 32, 32, 32);
		facadeModels[203] = new BorderFacade(32, 26, 24, 32, 32, 32);
		facadeModels[204] = new BorderFacade(32, 26, 23, 32, 32, 32);
		facadeModels[205] = new BorderFacade(32, 26, 25, 32, 32, 32);
		facadeModels[206] = new BorderFacade(32, 28, 11, 32, 32, 32);
		facadeModels[207] = new BorderFacade(32, 28, 24, 32, 32, 32);
		facadeModels[208] = new BorderFacade(32, 28, 23, 32, 32, 32);
		facadeModels[209] = new BorderFacade(32, 28, 25, 32, 32, 32);
		facadeModels[210] = new BorderFacade(32, 7, 32, 11, 32, 32);
		facadeModels[211] = new BorderFacade(32, 7, 32, 23, 32, 32);
		facadeModels[212] = new BorderFacade(32, 7, 32, 24, 32, 32);
		facadeModels[213] = new BorderFacade(32, 7, 32, 25, 32, 32);
		facadeModels[214] = new BorderFacade(32, 21, 32, 11, 32, 32);
		facadeModels[215] = new BorderFacade(32, 21, 32, 23, 32, 32);
		facadeModels[216] = new BorderFacade(32, 21, 32, 24, 32, 32);
		facadeModels[217] = new BorderFacade(32, 21, 32, 25, 32, 32);
		facadeModels[218] = new BorderFacade(32, 20, 32, 11, 32, 32);
		facadeModels[219] = new BorderFacade(32, 20, 32, 23, 32, 32);
		facadeModels[220] = new BorderFacade(32, 20, 32, 24, 32, 32);
		facadeModels[221] = new BorderFacade(32, 20, 32, 25, 32, 32);
		facadeModels[222] = new BorderFacade(32, 22, 32, 11, 32, 32);
		facadeModels[223] = new BorderFacade(32, 22, 32, 23, 32, 32);
		facadeModels[224] = new BorderFacade(32, 22, 32, 24, 32, 32);
		facadeModels[225] = new BorderFacade(32, 22, 32, 25, 32, 32);
		facadeModels[226] = new BorderFacade(32, 11, 32, 32, 11, 32);
		facadeModels[227] = new BorderFacade(32, 11, 32, 32, 23, 32);
		facadeModels[228] = new BorderFacade(32, 11, 32, 32, 24, 32);
		facadeModels[229] = new BorderFacade(32, 11, 32, 32, 25, 32);
		facadeModels[230] = new BorderFacade(32, 24, 32, 32, 11, 32);
		facadeModels[231] = new BorderFacade(32, 24, 32, 32, 23, 32);
		facadeModels[232] = new BorderFacade(32, 24, 32, 32, 24, 32);
		facadeModels[233] = new BorderFacade(32, 24, 32, 32, 25, 32);
		facadeModels[234] = new BorderFacade(32, 23, 32, 32, 11, 32);
		facadeModels[235] = new BorderFacade(32, 23, 32, 32, 23, 32);
		facadeModels[236] = new BorderFacade(32, 23, 32, 32, 24, 32);
		facadeModels[237] = new BorderFacade(32, 23, 32, 32, 25, 32);
		facadeModels[238] = new BorderFacade(32, 25, 32, 32, 11, 32);
		facadeModels[239] = new BorderFacade(32, 25, 32, 32, 23, 32);
		facadeModels[240] = new BorderFacade(32, 25, 32, 32, 24, 32);
		facadeModels[241] = new BorderFacade(32, 25, 32, 32, 25, 32);
		facadeModels[242] = new BorderFacade(32, 14, 32, 32, 32, 11);
		facadeModels[243] = new BorderFacade(32, 14, 32, 32, 32, 24);
		facadeModels[244] = new BorderFacade(32, 14, 32, 32, 32, 23);
		facadeModels[245] = new BorderFacade(32, 14, 32, 32, 32, 25);
		facadeModels[246] = new BorderFacade(32, 29, 32, 32, 32, 11);
		facadeModels[247] = new BorderFacade(32, 29, 32, 32, 32, 24);
		facadeModels[248] = new BorderFacade(32, 29, 32, 32, 32, 23);
		facadeModels[249] = new BorderFacade(32, 29, 32, 32, 32, 25);
		facadeModels[250] = new BorderFacade(32, 30, 32, 32, 32, 11);
		facadeModels[251] = new BorderFacade(32, 30, 32, 32, 32, 24);
		facadeModels[252] = new BorderFacade(32, 30, 32, 32, 32, 23);
		facadeModels[253] = new BorderFacade(32, 30, 32, 32, 32, 25);
		facadeModels[254] = new BorderFacade(32, 31, 32, 32, 32, 11);
		facadeModels[255] = new BorderFacade(32, 31, 32, 32, 32, 24);
		facadeModels[256] = new BorderFacade(32, 31, 32, 32, 32, 23);
		facadeModels[257] = new BorderFacade(32, 31, 32, 32, 32, 25);
		facadeModels[258] = new BorderFacade(13, 32, 14, 32, 32, 32);
		facadeModels[259] = new BorderFacade(13, 32, 29, 32, 32, 32);
		facadeModels[260] = new BorderFacade(13, 32, 30, 32, 32, 32);
		facadeModels[261] = new BorderFacade(13, 32, 31, 32, 32, 32);
		facadeModels[262] = new BorderFacade(26, 32, 14, 32, 32, 32);
		facadeModels[263] = new BorderFacade(26, 32, 29, 32, 32, 32);
		facadeModels[264] = new BorderFacade(26, 32, 30, 32, 32, 32);
		facadeModels[265] = new BorderFacade(26, 32, 31, 32, 32, 32);
		facadeModels[266] = new BorderFacade(27, 32, 14, 32, 32, 32);
		facadeModels[267] = new BorderFacade(27, 32, 29, 32, 32, 32);
		facadeModels[268] = new BorderFacade(27, 32, 30, 32, 32, 32);
		facadeModels[269] = new BorderFacade(27, 32, 31, 32, 32, 32);
		facadeModels[270] = new BorderFacade(28, 32, 14, 32, 32, 32);
		facadeModels[271] = new BorderFacade(28, 32, 29, 32, 32, 32);
		facadeModels[272] = new BorderFacade(28, 32, 30, 32, 32, 32);
		facadeModels[273] = new BorderFacade(28, 32, 31, 32, 32, 32);
		facadeModels[274] = new BorderFacade(7, 32, 32, 14, 32, 32);
		facadeModels[275] = new BorderFacade(7, 32, 32, 30, 32, 32);
		facadeModels[276] = new BorderFacade(7, 32, 32, 29, 32, 32);
		facadeModels[277] = new BorderFacade(7, 32, 32, 31, 32, 32);
		facadeModels[278] = new BorderFacade(20, 32, 32, 14, 32, 32);
		facadeModels[279] = new BorderFacade(20, 32, 32, 30, 32, 32);
		facadeModels[280] = new BorderFacade(20, 32, 32, 29, 32, 32);
		facadeModels[281] = new BorderFacade(20, 32, 32, 31, 32, 32);
		facadeModels[282] = new BorderFacade(21, 32, 32, 14, 32, 32);
		facadeModels[283] = new BorderFacade(21, 32, 32, 30, 32, 32);
		facadeModels[284] = new BorderFacade(21, 32, 32, 29, 32, 32);
		facadeModels[285] = new BorderFacade(21, 32, 32, 31, 32, 32);
		facadeModels[286] = new BorderFacade(22, 32, 32, 14, 32, 32);
		facadeModels[287] = new BorderFacade(22, 32, 32, 30, 32, 32);
		facadeModels[288] = new BorderFacade(22, 32, 32, 29, 32, 32);
		facadeModels[289] = new BorderFacade(22, 32, 32, 31, 32, 32);
		facadeModels[290] = new BorderFacade(14, 32, 32, 32, 14, 32);
		facadeModels[291] = new BorderFacade(14, 32, 32, 32, 30, 32);
		facadeModels[292] = new BorderFacade(14, 32, 32, 32, 29, 32);
		facadeModels[293] = new BorderFacade(14, 32, 32, 32, 31, 32);
		facadeModels[294] = new BorderFacade(29, 32, 32, 32, 14, 32);
		facadeModels[295] = new BorderFacade(29, 32, 32, 32, 30, 32);
		facadeModels[296] = new BorderFacade(29, 32, 32, 32, 29, 32);
		facadeModels[297] = new BorderFacade(29, 32, 32, 32, 31, 32);
		facadeModels[298] = new BorderFacade(30, 32, 32, 32, 14, 32);
		facadeModels[299] = new BorderFacade(30, 32, 32, 32, 30, 32);
		facadeModels[300] = new BorderFacade(30, 32, 32, 32, 29, 32);
		facadeModels[301] = new BorderFacade(30, 32, 32, 32, 31, 32);
		facadeModels[302] = new BorderFacade(31, 32, 32, 32, 14, 32);
		facadeModels[303] = new BorderFacade(31, 32, 32, 32, 30, 32);
		facadeModels[304] = new BorderFacade(31, 32, 32, 32, 29, 32);
		facadeModels[305] = new BorderFacade(31, 32, 32, 32, 31, 32);
		facadeModels[306] = new BorderFacade(11, 32, 32, 32, 32, 14);
		facadeModels[307] = new BorderFacade(11, 32, 32, 32, 32, 29);
		facadeModels[308] = new BorderFacade(11, 32, 32, 32, 32, 30);
		facadeModels[309] = new BorderFacade(11, 32, 32, 32, 32, 31);
		facadeModels[310] = new BorderFacade(24, 32, 32, 32, 32, 14);
		facadeModels[311] = new BorderFacade(24, 32, 32, 32, 32, 29);
		facadeModels[312] = new BorderFacade(24, 32, 32, 32, 32, 30);
		facadeModels[313] = new BorderFacade(24, 32, 32, 32, 32, 31);
		facadeModels[314] = new BorderFacade(23, 32, 32, 32, 32, 14);
		facadeModels[315] = new BorderFacade(23, 32, 32, 32, 32, 29);
		facadeModels[316] = new BorderFacade(23, 32, 32, 32, 32, 30);
		facadeModels[317] = new BorderFacade(23, 32, 32, 32, 32, 31);
		facadeModels[318] = new BorderFacade(25, 32, 32, 32, 32, 14);
		facadeModels[319] = new BorderFacade(25, 32, 32, 32, 32, 29);
		facadeModels[320] = new BorderFacade(25, 32, 32, 32, 32, 30);
		facadeModels[321] = new BorderFacade(25, 32, 32, 32, 32, 31);
		facadeModels[322] = new BorderFacade(32, 32, 32, 13, 32, 7);
		facadeModels[323] = new BorderFacade(32, 32, 32, 27, 32, 7);
		facadeModels[324] = new BorderFacade(32, 32, 32, 13, 32, 20);
		facadeModels[325] = new BorderFacade(32, 32, 32, 27, 32, 20);
		facadeModels[326] = new BorderFacade(32, 32, 32, 26, 32, 7);
		facadeModels[327] = new BorderFacade(32, 32, 32, 28, 32, 7);
		facadeModels[328] = new BorderFacade(32, 32, 32, 26, 32, 20);
		facadeModels[329] = new BorderFacade(32, 32, 32, 28, 32, 20);
		facadeModels[330] = new BorderFacade(32, 32, 32, 13, 32, 21);
		facadeModels[331] = new BorderFacade(32, 32, 32, 27, 32, 21);
		facadeModels[332] = new BorderFacade(32, 32, 32, 13, 32, 22);
		facadeModels[333] = new BorderFacade(32, 32, 32, 27, 32, 22);
		facadeModels[334] = new BorderFacade(32, 32, 32, 26, 32, 21);
		facadeModels[335] = new BorderFacade(32, 32, 32, 28, 32, 21);
		facadeModels[336] = new BorderFacade(32, 32, 32, 26, 32, 22);
		facadeModels[337] = new BorderFacade(32, 32, 32, 28, 32, 22);
		facadeModels[338] = new BorderFacade(32, 32, 32, 7, 13, 32);
		facadeModels[339] = new BorderFacade(32, 32, 32, 7, 27, 32);
		facadeModels[340] = new BorderFacade(32, 32, 32, 20, 13, 32);
		facadeModels[341] = new BorderFacade(32, 32, 32, 20, 27, 32);
		facadeModels[342] = new BorderFacade(32, 32, 32, 7, 26, 32);
		facadeModels[343] = new BorderFacade(32, 32, 32, 7, 28, 32);
		facadeModels[344] = new BorderFacade(32, 32, 32, 20, 26, 32);
		facadeModels[345] = new BorderFacade(32, 32, 32, 20, 28, 32);
		facadeModels[346] = new BorderFacade(32, 32, 32, 21, 13, 32);
		facadeModels[347] = new BorderFacade(32, 32, 32, 21, 27, 32);
		facadeModels[348] = new BorderFacade(32, 32, 32, 22, 13, 32);
		facadeModels[349] = new BorderFacade(32, 32, 32, 22, 27, 32);
		facadeModels[350] = new BorderFacade(32, 32, 32, 21, 26, 32);
		facadeModels[351] = new BorderFacade(32, 32, 32, 21, 28, 32);
		facadeModels[352] = new BorderFacade(32, 32, 32, 22, 26, 32);
		facadeModels[353] = new BorderFacade(32, 32, 32, 22, 28, 32);
		facadeModels[354] = new BorderFacade(32, 32, 7, 32, 32, 13);
		facadeModels[355] = new BorderFacade(32, 32, 20, 32, 32, 13);
		facadeModels[356] = new BorderFacade(32, 32, 7, 32, 32, 27);
		facadeModels[357] = new BorderFacade(32, 32, 20, 32, 32, 27);
		facadeModels[358] = new BorderFacade(32, 32, 21, 32, 32, 13);
		facadeModels[359] = new BorderFacade(32, 32, 22, 32, 32, 13);
		facadeModels[360] = new BorderFacade(32, 32, 21, 32, 32, 27);
		facadeModels[361] = new BorderFacade(32, 32, 22, 32, 32, 27);
		facadeModels[362] = new BorderFacade(32, 32, 7, 32, 32, 26);
		facadeModels[363] = new BorderFacade(32, 32, 20, 32, 32, 26);
		facadeModels[364] = new BorderFacade(32, 32, 7, 32, 32, 28);
		facadeModels[365] = new BorderFacade(32, 32, 20, 32, 32, 28);
		facadeModels[366] = new BorderFacade(32, 32, 21, 32, 32, 26);
		facadeModels[367] = new BorderFacade(32, 32, 22, 32, 32, 26);
		facadeModels[368] = new BorderFacade(32, 32, 21, 32, 32, 28);
		facadeModels[369] = new BorderFacade(32, 32, 22, 32, 32, 28);
		facadeModels[370] = new BorderFacade(32, 32, 13, 32, 7, 32);
		facadeModels[371] = new BorderFacade(32, 32, 27, 32, 7, 32);
		facadeModels[372] = new BorderFacade(32, 32, 13, 32, 20, 32);
		facadeModels[373] = new BorderFacade(32, 32, 27, 32, 20, 32);
		facadeModels[374] = new BorderFacade(32, 32, 26, 32, 7, 32);
		facadeModels[375] = new BorderFacade(32, 32, 28, 32, 7, 32);
		facadeModels[376] = new BorderFacade(32, 32, 26, 32, 20, 32);
		facadeModels[377] = new BorderFacade(32, 32, 28, 32, 20, 32);
		facadeModels[378] = new BorderFacade(32, 32, 13, 32, 21, 32);
		facadeModels[379] = new BorderFacade(32, 32, 27, 32, 21, 32);
		facadeModels[380] = new BorderFacade(32, 32, 13, 32, 22, 32);
		facadeModels[381] = new BorderFacade(32, 32, 27, 32, 22, 32);
		facadeModels[382] = new BorderFacade(32, 32, 26, 32, 21, 32);
		facadeModels[383] = new BorderFacade(32, 32, 28, 32, 21, 32);
		facadeModels[384] = new BorderFacade(32, 32, 26, 32, 22, 32);
		facadeModels[385] = new BorderFacade(32, 32, 28, 32, 22, 32);
		
		
	}

	@Override
	public IBakedModel getModelVariant(int variantID) {
		return facadeModels[variantID];
	}
	
	private class BorderFacade implements IBakedModel {

		private final byte upFace;
		private final byte downFace;
		private final byte eastFace;
		private final byte westFace;
		private final byte northFace;
		private final byte southFace;
		
		public BorderFacade(int upFace, int downFace, int eastFace, int westFace, int northFace, int southFace) {
			this.upFace = (byte) upFace;
			this.downFace = (byte) downFace;
			this.eastFace = (byte) eastFace;
			this.westFace = (byte) westFace;
			this.northFace = (byte) northFace;
			this.southFace = (byte) southFace;
		}

		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing face) {

			switch (face) {
			case DOWN:
				return faceQuads[EnumFacing.DOWN.ordinal()][downFace];
			case UP:
				return faceQuads[EnumFacing.UP.ordinal()][upFace];
			case NORTH:
				return faceQuads[EnumFacing.NORTH.ordinal()][northFace];
			case SOUTH:
				return faceQuads[EnumFacing.SOUTH.ordinal()][southFace];
			case EAST:
				return faceQuads[EnumFacing.EAST.ordinal()][eastFace];
			case WEST:
				return faceQuads[EnumFacing.WEST.ordinal()][westFace];
			}

			return Collections.emptyList();
		}

		@Override
		public List<BakedQuad> getGeneralQuads() {
			return Collections.emptyList();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return controller.isShaded;
		}

		@Override
		public boolean isGui3d() {
			return true;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return NiceModelBorder.this.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

	}

    @Override
    protected List<BakedQuad> getItemQuads()
    {
        return itemQuads;
    }

}
