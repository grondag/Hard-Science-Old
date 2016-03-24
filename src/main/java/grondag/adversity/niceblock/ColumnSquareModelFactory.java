package grondag.adversity.niceblock;

import grondag.adversity.library.Useful;
import grondag.adversity.library.model.QuadFactory;
import grondag.adversity.library.model.QuadFactory.FaceVertex;
import grondag.adversity.library.model.QuadFactory.QuadInputs;
import grondag.adversity.library.model.QuadFactory.SimpleQuadBounds;
import grondag.adversity.niceblock.base.ModelFactory;
import grondag.adversity.niceblock.base.ModelState;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.IColorProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.joinstate.BlockJoinSelector;
import grondag.adversity.niceblock.joinstate.FaceJoinState;
import grondag.adversity.niceblock.joinstate.FaceSide;
import grondag.adversity.niceblock.joinstate.BlockJoinSelector.BlockJoinState;

import java.util.List;
import com.google.common.collect.ImmutableList;
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

    
//    protected IBakedModel[] templateModels;
    
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

    @Override
    public List<BakedQuad> getFaceQuads(ModelState modelState, IColorProvider colorProvider, EnumFacing face) 
    {
        if (face == null) return QuadFactory.EMPTY_QUAD_LIST;
        
    	QuadInputs quadInputs = new QuadInputs();
        quadInputs.lockUV = true;
        quadInputs.isShaded = myController.modelType != ColumnSquareController.ModelType.LAMP_BASE;
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(EnumColorMap.BASE);
        int clientShapeIndex = modelState.getClientShapeIndex(controller.getRenderLayer().ordinal());
        EnumFacing.Axis axis = EnumFacing.Axis.values()[myController.getAxisFromModelIndex(clientShapeIndex)];
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(myController.getTextureFromModelIndex(clientShapeIndex)));
        BlockJoinState bjs = BlockJoinSelector.getJoinState(myController.getShapeFromModelIndex(clientShapeIndex));

        int cutColor = myController.modelType == ColumnSquareController.ModelType.NORMAL 
                ? QuadFactory.shadeColor(quadInputs.color, 0.85F, false) : colorMap.getColorMap(EnumColorMap.LAMP);
    
        if(face.getAxis() == axis)
        {
            return makeCapFace(face, quadInputs, bjs.getFaceJoinState(face), cutColor, axis);
        }
        else
        {
            return makeSideFace(face, quadInputs, bjs.getFaceJoinState(face), cutColor, axis);
        }
        
    }

    private List<BakedQuad> makeSideFace(EnumFacing face, QuadInputs qi, FaceJoinState fjs, int cutColor, EnumFacing.Axis axis)
    {
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        
        if(fjs != FaceJoinState.NO_FACE)
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
                    builder.add(qi.createNormalQuad());
                }              

                //top
                if(!hasTopJoin)
                {
                    qi.setupFaceQuad(face, 0.0 - CAULK, 1.0 - topCapHeight - CAULK, 1.0 + CAULK, 1.0 + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }

                //left margin
                if(leftMarginWidth > 0)
                {
                    qi.setupFaceQuad(face, 0.0 - CAULK, bottomCapHeight - CAULK, leftMarginWidth + CAULK, 1.0 - topCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }

                // right margin
                if(rightMarginWidth > 0)
                {
                    qi.setupFaceQuad(face, 1.0 - rightMarginWidth - CAULK, bottomCapHeight - CAULK, 1.0 + CAULK, 1.0 - topCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }

                //splines
                for(int i = 0; i < actualCutCount - 1; i++)
                {
                    qi.setupFaceQuad(face, leftMarginWidth + cutWidth * 2.0 * (double)i + cutWidth - CAULK, bottomCapHeight - CAULK,
                            leftMarginWidth + cutWidth * 2.0 * ((double)i + 1.0) + CAULK, 1.0 - topCapHeight + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }
                
                // top left corner
                if(fjs.needsCorner(topFace, leftFace, face))
                {
                    qi.setupFaceQuad(face, Math.max(leftMarginWidth, 0.0) - CAULK, 1.0 - baseMarginWidth - CAULK, leftMarginWidth + cutWidth + CAULK, 1.0 + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }
                
                // bottom left corner
                if(fjs.needsCorner(bottomFace, leftFace, face))
                {
                    qi.setupFaceQuad(face, Math.max(leftMarginWidth, 0.0) - CAULK, 0.0 - CAULK, leftMarginWidth + cutWidth + CAULK, baseMarginWidth + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }
         
                // top right corner
                if(fjs.needsCorner(topFace, rightFace, face))
                {
                    qi.setupFaceQuad(face, 1.0 - rightMarginWidth - cutWidth - CAULK, 1.0 - baseMarginWidth - CAULK, Math.min(1.0 - rightMarginWidth, 1.0) + CAULK, 1.0 + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
                }
                
                // bottom right corner
                if(fjs.needsCorner(bottomFace, rightFace, face))
                {
                    qi.setupFaceQuad(face, 1.0 - rightMarginWidth - cutWidth - CAULK, 0.0 - CAULK, Math.min(1.0 - rightMarginWidth, 1.0) + CAULK, baseMarginWidth + CAULK, 0.0, topFace);
                    builder.add(qi.createNormalQuad());
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
                        builder.add(qi.createNormalQuad());
                    }
        
                    // right face
                    if(sx1 < 0.9999)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(leftFace, topCapHeight, 1.0-cutDepth, 1.0-bottomCapHeight, 1.0, sx1, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // top face
                    if(topCapHeight > 0)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, sx0, 1.0-cutDepth, sx1, 1.0, 1.0 -topCapHeight, face));
                        builder.add(qi.createNormalQuad());
                    }
        
                    // bottom face
                    if(bottomCapHeight > 0)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, 1.0 -sx1, 1.0-cutDepth, 1.0 -sx0, 1.0, 1.0 -bottomCapHeight, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // top left corner
                    if(fjs.needsCorner(topFace, leftFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0.0), 1.0-cutDepth, leftMarginWidth + cutWidth, 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // bottom left corner
                    if(fjs.needsCorner(bottomFace, leftFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, 1.0 - leftMarginWidth - cutWidth, 1.0-cutDepth, Math.min(1.0 - leftMarginWidth, 1.0), 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());

                    }
             
                    // top right corner
                    if(fjs.needsCorner(topFace, rightFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, 1.0 - rightMarginWidth - cutWidth, 1.0-cutDepth, Math.min(1.0 - rightMarginWidth, 1.0), 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // bottom right corner
                    if(fjs.needsCorner(bottomFace, rightFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0.0), 1.0-cutDepth, rightMarginWidth + cutWidth, 1.0, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }
                }
                
                // bottom can be a single poly
                QuadInputs qiCut = qi.clone();
                qiCut.color = cutColor; 
                qiCut.setupFaceQuad(face, Math.max(0.0, leftMarginWidth), bottomCapHeight, Math.min(1.0, 1.0 - rightMarginWidth), 1.0 - topCapHeight, cutDepth, topFace);
                builder.add(qiCut.createNormalQuad());
            }
        }
        return builder.build();
    }
    
    private List<BakedQuad> makeCapFace(EnumFacing face, QuadInputs qi, FaceJoinState fjs, int cutColor, EnumFacing.Axis axis)
    {
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();

        if(fjs != FaceJoinState.NO_FACE)
        {

            if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
            {
                //cut bottom can be a single poly
                QuadInputs qiCut = qi.clone();
                qiCut.color = cutColor; 
                qiCut.setupFaceQuad(face, 
                        fjs.isJoined(FaceSide.LEFT) ? 0.0 : baseMarginWidth, 
                        fjs.isJoined(FaceSide.BOTTOM) ? 0.0 : baseMarginWidth, 
                        fjs.isJoined(FaceSide.RIGHT) ? 1.0 : 1.0 -baseMarginWidth, 
                        fjs.isJoined(FaceSide.TOP) ? 1.0 : 1.0 -baseMarginWidth, 
                        cutDepth, 
                        FaceSide.TOP.getRelativeFace(face));
                builder.add(qiCut.createNormalQuad());
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
                        // margin corner faces
                        qi.setupFaceQuad(face, 
                                new FaceVertex(baseMarginWidth - CAULK, 1.0 - baseMarginWidth - CAULK),
                                new FaceVertex(baseMarginWidth + CAULK, 1.0 - baseMarginWidth - CAULK),
                                new FaceVertex(baseMarginWidth + CAULK, 1.0 + CAULK),
                                new FaceVertex(0.0 - CAULK, 1.0 + CAULK), 
                                0.0, side);
                        builder.add(qi.createNormalQuad());
                        qi.setupFaceQuad(face, 
                                new FaceVertex(1.0 - baseMarginWidth - CAULK, 1.0 + CAULK),  
                                new FaceVertex(1.0 - baseMarginWidth - CAULK, 1.0 -baseMarginWidth - CAULK),
                                new FaceVertex(1.0 - baseMarginWidth + CAULK, 1.0 -baseMarginWidth - CAULK), 
                                new FaceVertex(1.0 + CAULK, 1.0 + CAULK), 
                                0.0, side);
                        builder.add(qi.createNormalQuad());                
                    }

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                    {
                        // margin corner sides
                        setupCutSideQuad(qi, cutColor, 
                                new SimpleQuadBounds(Useful.rightOf(face, side), 1.0 -baseMarginWidth - CAULK, 1.0 -cutDepth, 1.0 + CAULK, 1, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());

                        setupCutSideQuad(qi, cutColor, 
                                new SimpleQuadBounds(Useful.leftOf(face, side), 0.0 - CAULK, 1.0 -cutDepth, baseMarginWidth + CAULK, 1, 1.0 -baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }

                    //splines
                    for (int i = 0; i < myController.cutCount / 2; i++)
                    {
                        double xLeft = baseMarginWidth + ((double)i * 2.0 + 1.0) * this.cutWidth;
                        double xRight = Math.min(xLeft + cutWidth, 0.5);

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                        {
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(xLeft - CAULK, 1.0 -xLeft - CAULK),  
                                    new FaceVertex(xRight + CAULK, 1.0 -xRight - CAULK),
                                    new FaceVertex(xRight + CAULK, 1.0 + CAULK), 
                                    new FaceVertex(xLeft - CAULK, 1.0 + CAULK), 
                                    0.0, side);
                            builder.add(qi.createNormalQuad());
                            // mirror on right side, reverse winding order
                            builder.add(qi.createNormalQuad());                             
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(1.0 - xRight - CAULK, 1.0 - xRight  - CAULK),  
                                    new FaceVertex(1.0 - xLeft + CAULK, 1.0 -xLeft - CAULK),
                                    new FaceVertex(1.0 - xLeft + CAULK, 1.0 + CAULK), 
                                    new FaceVertex(1.0 - xRight - CAULK, 1.0 + CAULK), 
                                    0.0, side);
                            builder.add(qi.createNormalQuad());
                        }

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                        {
                            // cut sides

                            // with odd number of cuts, these overlap in middle, avoid with this check
                            if(xLeft < 0.4999)
                            {
                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.leftOf(face, side), 0.0 - CAULK, 1.0 -cutDepth, xLeft + CAULK, 1.0, xLeft, face));
                                builder.add(qi.createNormalQuad());

                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.rightOf(face, side), 1.0 -xLeft - CAULK, 1.0 -cutDepth, 1.0 + CAULK, 1.0, xLeft, face));
                                builder.add(qi.createNormalQuad());
                            }
                            if(xRight < 0.4999)
                            {
                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.rightOf(face, side), 1.0 -xRight - CAULK, 1.0 -cutDepth, 1.0 + CAULK, 1.0, 1.0 -xRight, face));
                                builder.add(qi.createNormalQuad());

                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.leftOf(face, side), 0.0 - CAULK, 1.0 -cutDepth, xRight + CAULK, 1.0, 1.0 -xRight, face));

                                builder.add(qi.createNormalQuad());
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
                                new FaceVertex(baseMarginWidth-CAULK, 1.0 -baseMarginWidth),  
                                new FaceVertex(1.0 -baseMarginWidth+CAULK, 1.0 -baseMarginWidth),
                                new FaceVertex(1.0 +CAULK, 1.0), 
                                new FaceVertex(0-CAULK, 1.0), 
                                0.0, side);
                        builder.add(qi.createNormalQuad());   
                    }

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                    {
                        // outer cut sides
                        for(int i = 0; i < (myController.cutCount + 1.0) / 2; i++)
                        {
                            double offset = baseMarginWidth + (cutWidth * 2.0 * i);

                            setupCutSideQuad(qi, cutColor,
                                    new SimpleQuadBounds(side.getOpposite(), offset - CAULK, 1.0 -cutDepth, 1.0 -offset + CAULK, 1.0, 1.0 -offset, face));
                            builder.add(qi.createNormalQuad());

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
                            builder.add(qi.createNormalQuad());
                        }

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                        {
                            // spline / center
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(offset + cutWidth - CAULK, 1.0 - offset - cutWidth - CAULK),  
                                    new FaceVertex(1.0 - offset - cutWidth + CAULK, 1.0 - offset - cutWidth - CAULK),
                                    new FaceVertex(1.0 - offset + CAULK, 1.0 - offset + CAULK ), 
                                    new FaceVertex(offset - CAULK, 1.0 - offset + CAULK), 
                                    0.0, side);
                            builder.add(qi.createNormalQuad());  
                        }

                    }
                }
            }
        }

        return builder.build();
    }
    
    private void setupCutSideQuad(QuadInputs qi, int cutColor, SimpleQuadBounds qb)
    {
        int cutSideColor;
        if(qi.isShaded)
        {
            cutSideColor = qi.color;
        }
        else
        {
            cutSideColor = QuadFactory.shadeColor(cutColor, (LightUtil.diffuseLight(qb.face) + 2) / 3, false);
        }
        
        qi.setupFaceQuad(qb.face,
                new FaceVertex.Colored(qb.x0, qb.y0, cutSideColor),
                new FaceVertex.Colored(qb.x1, qb.y0, cutSideColor),
                new FaceVertex.Colored(qb.x1, qb.y1, qi.color),
                new FaceVertex.Colored(qb.x0, qb.y1, qi.color), 
                qb.depth, qb.topFace);
    }    
    
    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
    {
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder<BakedQuad>();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.addAll(this.getFaceQuads(modelState, colorProvider, face));
        }        
        return general.build();
    }
}

