package grondag.adversity.niceblock;

import grondag.adversity.library.Useful;
import grondag.adversity.library.joinstate.CornerJoinBlockState;
import grondag.adversity.library.joinstate.CornerJoinBlockStateSelector;
import grondag.adversity.library.joinstate.CornerJoinFaceState;
import grondag.adversity.library.joinstate.FaceSide;
import grondag.adversity.library.model.quadfactory.FaceVertex;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.QuadFactory;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.library.model.quadfactory.SimpleQuadBounds;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.modelstate.ModelState;

import java.util.List;
import com.google.common.collect.ImmutableList;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class ColumnSquareModelFactory extends ModelFactory
{
    /** convenience reference to avoid casts everywhere */
    private final ColumnSquareController myController;
    private final double cutWidth;
    private final double baseMarginWidth;
    private final double marginOffset;
    private final double cutDepth;

    /** overextend some quads by this amount to prevent pinholes */
    private static final double CAULK = 0.0001;

    private final TIntObjectHashMap<List<BakedQuad>> faceCache = new TIntObjectHashMap<List<BakedQuad>>(4096);
    
    public ColumnSquareModelFactory(ColumnSquareController controller)
    {
        super(controller);
        myController = controller;
        if(myController.areCutsOnEdge)
        {
            cutWidth = 0.5 / (myController.cutCount + 1.0);
            baseMarginWidth = 1.5 * cutWidth;
            marginOffset = -0.5;
            cutDepth = cutWidth * 0.8;
        }
        else
        {
            cutWidth = 0.5 / (myController.cutCount + 2.0);
            baseMarginWidth = 2.5 * cutWidth;
            marginOffset = 0.5;
            cutDepth = cutWidth / 2.0;
        }
    }
    
    private int makeCacheKey(EnumFacing face, EnumFacing.Axis axis, CornerJoinFaceState fjs, int colorIndex, int textureIndex)
    {
    	int key = axis.ordinal();
    	int offset = EnumFacing.Axis.values().length;
    	key += face.ordinal() * offset;
    	offset *= EnumFacing.values().length;
    	key += fjs.ordinal() * offset;
    	offset *= CornerJoinFaceState.values().length;
    	key += textureIndex * offset;
    	offset *= controller.getAlternateTextureCount();
    	key += colorIndex * offset;
    	return key;
    }

    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face) 
    {
        return makeFaceQuads(modelState, colorProvider, face, false); 
    }
    
    private List<BakedQuad> makeFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face, boolean isItem) 
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;

        int clientShapeIndex = (int) modelState.getShapeIndex(controller.getRenderLayer());
        int textureIndex = myController.getAltTextureFromModelIndex(clientShapeIndex);
        CornerJoinBlockState bjs = CornerJoinBlockStateSelector.getJoinState(myController.getShapeFromModelIndex(clientShapeIndex));
        EnumFacing.Axis axis = EnumFacing.Axis.values()[myController.getAxisFromModelIndex(clientShapeIndex)];

        int cacheKey = 0;
        List<BakedQuad> retVal = null;
        
        if(!isItem)
        {
            cacheKey = makeCacheKey(face, axis, bjs.getFaceJoinState(face), modelState.getColorIndex(), textureIndex);
            retVal = faceCache.get(cacheKey);
        }
        
        if(retVal == null)
        {
	    	RawQuad quadInputs = new RawQuad();
	    	quadInputs.isItem = isItem;
	        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
	        quadInputs.color = colorMap.getColor(EnumColorMap.BASE);
	        int cutColor = myController.modelType == ColumnSquareController.ModelType.NORMAL 
	                ? QuadFactory.shadeColor(quadInputs.color, 0.85F, false) : colorMap.getColor(EnumColorMap.LAMP);
	        quadInputs.lockUV = true;
	        quadInputs.lightingMode = myController.modelType == ColumnSquareController.ModelType.LAMP_BASE 
	                ? LightingMode.FULLBRIGHT 
                    : LightingMode.SHADED;
	        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(textureIndex));
	
	        if(face.getAxis() == axis)
	        {
	            retVal = makeCapFace(face, quadInputs, bjs.getFaceJoinState(face), cutColor, axis);
	        }
	        else
	        {
	            retVal = makeSideFace(face, quadInputs, bjs.getFaceJoinState(face), cutColor, axis);
	        }
	        
	        if(!isItem)
	        {
	            synchronized(faceCache)
    	        {
    	        	faceCache.put(cacheKey, retVal);
    	        }
	        }
        }
        return retVal;
    }

    private List<BakedQuad> makeSideFace(EnumFacing face, RawQuad qi, CornerJoinFaceState fjs, int cutColor, EnumFacing.Axis axis)
    {
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        
        if(fjs != CornerJoinFaceState.NO_FACE)
        {
            EnumFacing topFace = Useful.getAxisTop(axis);
            EnumFacing bottomFace = topFace.getOpposite();
            EnumFacing leftFace = Useful.leftOf(face, topFace);
            EnumFacing rightFace = Useful.rightOf(face, topFace);
            
            int actualCutCount = myController.cutCount;
            
            boolean hasLeftJoin = fjs.isJoined(leftFace, face);
            boolean hasRightJoin = fjs.isJoined(rightFace, face);
            boolean hasTopJoin = fjs.isJoined(topFace, face);
            boolean hasBottomJoin = fjs.isJoined(bottomFace, face);
            
            if (hasLeftJoin) actualCutCount++;
            if (hasRightJoin) actualCutCount++;
            
            double leftMarginWidth = hasLeftJoin ? marginOffset * cutWidth : baseMarginWidth;
            double rightMarginWidth = hasRightJoin ? marginOffset * cutWidth : baseMarginWidth;
            double topCapHeight = hasTopJoin ? 0.0 : baseMarginWidth;
            double bottomCapHeight = hasBottomJoin ? 0.0 : baseMarginWidth;;
            
            if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
            {
                //bottom
                if(!hasBottomJoin)
                {
                    qi.setupFaceQuad(face, 0.0 - CAULK, 0.0 - CAULK, 1.0 + CAULK, bottomCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }              

                //top
                if(!hasTopJoin)
                {
                    qi.setupFaceQuad(face, 0.0 - CAULK, 1.0 - topCapHeight - CAULK, 1.0 + CAULK, 1.0 + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }

                //left margin
                if(leftMarginWidth > 0)
                {
                    qi.setupFaceQuad(face, 0.0 - CAULK, bottomCapHeight - CAULK, leftMarginWidth + CAULK, 1.0 - topCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }

                // right margin
                if(rightMarginWidth > 0)
                {
                    qi.setupFaceQuad(face, 1.0 - rightMarginWidth - CAULK, bottomCapHeight - CAULK, 1.0 + CAULK, 1.0 - topCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }

                //splines
                for(int i = 0; i < actualCutCount - 1; i++)
                {
                    qi.setupFaceQuad(face, leftMarginWidth + cutWidth * 2.0 * (double)i + cutWidth - CAULK, bottomCapHeight - CAULK,
                            leftMarginWidth + cutWidth * 2.0 * ((double)i + 1.0) + CAULK, 1.0 - topCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }
                
                // top left corner
                if(fjs.needsCorner(topFace, leftFace, face))
                {
                    qi.setupFaceQuad(face, Math.max(leftMarginWidth, 0.0) - CAULK, 1.0 - baseMarginWidth - CAULK, leftMarginWidth + cutWidth + CAULK, 1.0 + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }
                
                // bottom left corner
                if(fjs.needsCorner(bottomFace, leftFace, face))
                {
                    qi.setupFaceQuad(face, Math.max(leftMarginWidth, 0.0) - CAULK, 0.0 - CAULK, leftMarginWidth + cutWidth + CAULK, baseMarginWidth + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }
         
                // top right corner
                if(fjs.needsCorner(topFace, rightFace, face))
                {
                    qi.setupFaceQuad(face, 1.0 - rightMarginWidth - cutWidth - CAULK, 1.0 - baseMarginWidth - CAULK, Math.min(1.0 - rightMarginWidth, 1.0) + CAULK, 1.0 + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }
                
                // bottom right corner
                if(fjs.needsCorner(bottomFace, rightFace, face))
                {
                    qi.setupFaceQuad(face, 1.0 - rightMarginWidth - cutWidth - CAULK, 0.0 - CAULK, Math.min(1.0 - rightMarginWidth, 1.0) + CAULK, baseMarginWidth + CAULK, 0.0, topFace);
                    builder.add(qi.createBakedQuad());
                }
            }
            
            if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
            {
                for(int i = 0; i < actualCutCount; i++)
                {
                    double sx0 = Math.max(0.0, leftMarginWidth + cutWidth * 2.0 * (double)i);
                    double sx1 = Math.min(1.0, leftMarginWidth + cutWidth * 2.0 * (double)i + cutWidth);
          
                    // left face
                    if(sx0 > 0.0001)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(rightFace, bottomCapHeight, 1.0-cutDepth, 1.0-topCapHeight, 1.0, 1.0 - sx0, face));
                        builder.add(qi.createBakedQuad());
                    }
        
                    // right face
                    if(sx1 < 0.9999)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(leftFace, topCapHeight, 1.0-cutDepth, 1.0-bottomCapHeight, 1.0, sx1, face));
                        builder.add(qi.createBakedQuad());
                    }
                    
                    // top face
                    if(topCapHeight > 0)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, sx0, 1.0-cutDepth, sx1, 1.0, 1.0 -topCapHeight, face));
                        builder.add(qi.createBakedQuad());
                    }
        
                    // bottom face
                    if(bottomCapHeight > 0)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, 1.0 -sx1, 1.0-cutDepth, 1.0 -sx0, 1.0, 1.0 -bottomCapHeight, face));
                        builder.add(qi.createBakedQuad());
                    }
                    
                    // top left corner
                    if(fjs.needsCorner(topFace, leftFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0.0), 1.0-cutDepth, leftMarginWidth + cutWidth, 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createBakedQuad());
                    }
                    
                    // bottom left corner
                    if(fjs.needsCorner(bottomFace, leftFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, 1.0 - leftMarginWidth - cutWidth, 1.0-cutDepth, Math.min(1.0 - leftMarginWidth, 1.0), 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createBakedQuad());

                    }
             
                    // top right corner
                    if(fjs.needsCorner(topFace, rightFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, 1.0 - rightMarginWidth - cutWidth, 1.0-cutDepth, Math.min(1.0 - rightMarginWidth, 1.0), 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createBakedQuad());
                    }
                    
                    // bottom right corner
                    if(fjs.needsCorner(bottomFace, rightFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0.0), 1.0-cutDepth, rightMarginWidth + cutWidth, 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createBakedQuad());
                    }
                }
                
                // bottom can be a single poly
                RawQuad qiCut = qi.clone();
                qiCut.color = cutColor; 
                qiCut.setupFaceQuad(face, Math.max(0.0, leftMarginWidth), bottomCapHeight, Math.min(1.0, 1.0 - rightMarginWidth), 1.0 - topCapHeight, cutDepth, topFace);
                builder.add(qiCut.createBakedQuad());
            }
        }
        return builder.build();
    }
    
    private List<BakedQuad> makeCapFace(EnumFacing face, RawQuad qi, CornerJoinFaceState fjs, int cutColor, EnumFacing.Axis axis)
    {
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();

        if(fjs != CornerJoinFaceState.NO_FACE)
        {

            if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
            {
                //cut bottom can be a single poly
                RawQuad qiCut = qi.clone();
                qiCut.color = cutColor; 
                qiCut.setupFaceQuad(face, 
                        fjs.isJoined(FaceSide.LEFT) ? 0.0 : baseMarginWidth, 
                        fjs.isJoined(FaceSide.BOTTOM) ? 0.0 : baseMarginWidth, 
                        fjs.isJoined(FaceSide.RIGHT) ? 1.0 : 1.0 -baseMarginWidth, 
                        fjs.isJoined(FaceSide.TOP) ? 1.0 : 1.0 -baseMarginWidth, 
                        cutDepth, 
                        FaceSide.TOP.getRelativeFace(face));
                builder.add(qiCut.createBakedQuad());
            }


            // build quarter slice of cap for each side separately
            // specifications below are oriented with the side at top of cap face

            for(FaceSide joinSide : FaceSide.values())
            {

                EnumFacing side = joinSide.getRelativeFace(face);
                
                if(fjs.isJoined(joinSide))
                {
                    //This side is joined, so connect cuts to other block on this side.

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                    {
                        RawQuad tri = new RawQuad(qi, 3);
                        
                        // margin corner faces
                        tri.setupFaceQuad(face, 
                                new FaceVertex(baseMarginWidth - CAULK, 1.0 - baseMarginWidth - CAULK, 0),
                                new FaceVertex(baseMarginWidth + CAULK, 1.0 + CAULK, 0),
                                new FaceVertex(0.0 - CAULK, 1.0 + CAULK, 0), 
                                side);
                        builder.add(tri.createBakedQuad());
                        tri.setupFaceQuad(face, 
                                new FaceVertex(1.0 - baseMarginWidth - CAULK, 1.0 + CAULK, 0),  
                                new FaceVertex(1.0 - baseMarginWidth + CAULK, 1.0 -baseMarginWidth - CAULK, 0), 
                                new FaceVertex(1.0 + CAULK, 1.0 + CAULK, 0), 
                                side);
                        builder.add(tri.createBakedQuad());                
                    }

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                    {
                        // margin corner sides
                        setupCutSideQuad(qi, cutColor, 
                                new SimpleQuadBounds(Useful.rightOf(face, side), 1.0 -baseMarginWidth - CAULK, 1.0 -cutDepth, 1.0 + CAULK, 1, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createBakedQuad());

                        setupCutSideQuad(qi, cutColor, 
                                new SimpleQuadBounds(Useful.leftOf(face, side), 0.0 - CAULK, 1.0 -cutDepth, baseMarginWidth + CAULK, 1, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createBakedQuad());
                    }

                    //splines
                    for (int i = 0; i < myController.cutCount / 2; i++)
                    {
                        double xLeft = baseMarginWidth + ((double)i * 2.0 + 1.0) * this.cutWidth;
                        double xRight = Math.min(xLeft + cutWidth, 0.5);

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                        {
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(xLeft - CAULK, 1.0 -xLeft - CAULK, 0),  
                                    new FaceVertex(xRight + CAULK, 1.0 -xRight - CAULK, 0),
                                    new FaceVertex(xRight + CAULK, 1.0 + CAULK, 0), 
                                    new FaceVertex(xLeft - CAULK, 1.0 + CAULK, 0), 
                                    side);
                            builder.add(qi.createBakedQuad());
                            // mirror on right side, reverse winding order
                            builder.add(qi.createBakedQuad());                             
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(1.0 - xRight - CAULK, 1.0 - xRight  - CAULK, 0),  
                                    new FaceVertex(1.0 - xLeft + CAULK, 1.0 -xLeft - CAULK, 0),
                                    new FaceVertex(1.0 - xLeft + CAULK, 1.0 + CAULK, 0), 
                                    new FaceVertex(1.0 - xRight - CAULK, 1.0 + CAULK, 0), 
                                    side);
                            builder.add(qi.createBakedQuad());
                        }

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                        {
                            // cut sides

                            // with odd number of cuts, these overlap in middle, avoid with this check
                            if(xLeft < 0.4999)
                            {
                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.leftOf(face, side), 0.0 - CAULK, 1.0 -cutDepth, xLeft + CAULK, 1.0, xLeft, face));
                                builder.add(qi.createBakedQuad());

                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.rightOf(face, side), 1.0 -xLeft - CAULK, 1.0 -cutDepth, 1.0 + CAULK, 1.0, xLeft, face));
                                builder.add(qi.createBakedQuad());
                            }
                            if(xRight < 0.4999)
                            {
                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.rightOf(face, side), 1.0 -xRight - CAULK, 1.0 -cutDepth, 1.0 + CAULK, 1.0, 1.0 -xRight, face));
                                builder.add(qi.createBakedQuad());

                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.leftOf(face, side), 0.0 - CAULK, 1.0 -cutDepth, xRight + CAULK, 1.0, 1.0 -xRight, face));

                                builder.add(qi.createBakedQuad());
                            }
                        }
                    }
                }
                else  
                {    
                    // This side isn't joined, so don't connect cuts to other block on this side.

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE) 
                    {
                        // outer face
                        qi.setupFaceQuad(face, 
                                new FaceVertex(baseMarginWidth-CAULK, 1.0 -baseMarginWidth, 0),  
                                new FaceVertex(1.0 -baseMarginWidth+CAULK, 1.0 -baseMarginWidth, 0),
                                new FaceVertex(1.0 +CAULK, 1.0, 0), 
                                new FaceVertex(0-CAULK, 1.0, 0), 
                                side);
                        builder.add(qi.createBakedQuad());   
                    }

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                    {
                        // outer cut sides
                        for(int i = 0; i < (myController.cutCount + 1.0) / 2; i++)
                        {
                            double offset = baseMarginWidth + (cutWidth * 2.0 * i);

                            setupCutSideQuad(qi, cutColor,
                                    new SimpleQuadBounds(side.getOpposite(), offset - CAULK, 1.0 -cutDepth, 1.0 -offset + CAULK, 1.0, 1.0 -offset, face));
                            builder.add(qi.createBakedQuad());

                        }
                    }

                    for(int i = 0; i < myController.cutCount / 2; i++)
                    {
                        double offset = baseMarginWidth + cutWidth * (2.0 * (double)i + 1.0);

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                        {
                            // inner cut sides
                            setupCutSideQuad(qi, cutColor,
                                    new SimpleQuadBounds(side, offset - CAULK, 1.0 -cutDepth, 1.0 -offset + CAULK, 1.0, offset, face));
                            builder.add(qi.createBakedQuad());
                        }

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                        {
                            // spline / center
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(Math.min(0.5, offset + cutWidth) - CAULK, 1.0 - offset - cutWidth - CAULK, 0),  
                                    new FaceVertex(Math.max(0.5, 1.0 - offset - cutWidth) + CAULK, 1.0 - offset - cutWidth - CAULK, 0),
                                    new FaceVertex(1.0 - offset + CAULK, 1.0 - offset + CAULK, 0), 
                                    new FaceVertex(offset - CAULK, 1.0 - offset + CAULK, 0), 
                                    side);
                            builder.add(qi.createBakedQuad());  
                        }

                    }
                }
            }
        }

        return builder.build();
    }
    
    private void setupCutSideQuad(RawQuad qi, int cutColor, SimpleQuadBounds qb)
    {
        int cutSideColor;
        if(qi.lightingMode == LightingMode.SHADED)
        {
            cutSideColor = qi.color;
        }
        else
        {
            cutSideColor = QuadFactory.shadeColor(cutColor, (LightUtil.diffuseLight(qb.face) + 2) / 3, false);
        }
        
        qi.setupFaceQuad(qb.face,
                new FaceVertex.Colored(qb.x0, qb.y0, qb.depth, cutSideColor),
                new FaceVertex.Colored(qb.x1, qb.y0, qb.depth, cutSideColor),
                new FaceVertex.Colored(qb.x1, qb.y1, qb.depth, qi.color),
                new FaceVertex.Colored(qb.x0, qb.y1, qb.depth, qi.color), 
                qb.topFace);
    }    
    
    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.addAll(this.makeFaceQuads(modelState, colorProvider, face, true));
        }        
        return general.build();
    }
}

