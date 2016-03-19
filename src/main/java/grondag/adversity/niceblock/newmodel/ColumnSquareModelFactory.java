package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import grondag.adversity.niceblock.newmodel.AxisOrientedController.ModelType;
import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.newmodel.QuadFactory.QuadInputs;
import grondag.adversity.niceblock.newmodel.QuadFactory.FaceVertex;
import grondag.adversity.niceblock.newmodel.QuadFactory.SimpleQuadBounds;
import grondag.adversity.niceblock.newmodel.QuadFactory.Vertex;
import grondag.adversity.niceblock.newmodel.color.ColorMap;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.joinstate.BlockJoinSelector;
import grondag.adversity.niceblock.newmodel.joinstate.BlockJoinSelector.BlockJoinState;
import grondag.adversity.niceblock.newmodel.joinstate.FaceJoinState;
import grondag.adversity.niceblock.newmodel.joinstate.FaceSide;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ColumnSquareModelFactory extends BakedModelFactory
{
    /** convenience reference to avoid casts everywhere */
    private final ColumnSquareController myController;
    private final float cutWidth;
    private final float baseMarginWidth;
    private final float marginOffset;
    private final float cutDepth;

    
//    protected IBakedModel[] templateModels;
    
    public ColumnSquareModelFactory(ColumnSquareController controller)
    {
        super(controller);
        myController = controller;
        if(myController.areCutsOnEdge)
        {
            cutWidth = 0.5F / (myController.cutCount + 1.0F);
            baseMarginWidth = 1.5F * cutWidth;
            marginOffset = -0.5F;
            cutDepth = cutWidth * 0.8F;
        }
        else
        {
            cutWidth = 0.5F / (myController.cutCount + 2.0F);
            baseMarginWidth = 2.5F * cutWidth;
            marginOffset = 0.5F;
            cutDepth = cutWidth / 2;
        }
    }

    @Override
    public IBakedModel getBlockModel(ModelState modelState, IColorProvider colorProvider)
    {
        QuadInputs quadInputs = new QuadInputs();
        quadInputs.lockUV = true;
        quadInputs.isShaded = myController.modelType != ColumnSquareController.ModelType.LAMP_BASE;
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(EnumColorMap.BASE);
        int clientShapeIndex = modelState.getClientShapeIndex(controller.renderLayer.ordinal());
        EnumFacing.Axis axis = EnumFacing.Axis.values()[myController.getAxisFromModelIndex(clientShapeIndex)];
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(myController.getTextureFromModelIndex(clientShapeIndex)));
        BlockJoinState bjs = BlockJoinSelector.getJoinState(myController.getShapeFromModelIndex(clientShapeIndex));

        int cutColor = myController.modelType == ColumnSquareController.ModelType.NORMAL 
                ? QuadFactory.shadeColor(quadInputs.color, 0.85F, false) : colorMap.getColorMap(EnumColorMap.LAMP);
    
        
        List<BakedQuad>[] faceQuads = new List[6];

        for(EnumFacing face : EnumFacing.values())
        {
            if(face.getAxis() == axis)
            {
                faceQuads[face.ordinal()] = makeCapFace(face, quadInputs, bjs.getFaceJoinState(face), cutColor, axis);
            }
            else
            {
                faceQuads[face.ordinal()] = makeSideFace(face, quadInputs, bjs.getFaceJoinState(face), cutColor, axis);
            }
        }
        
        return new SimpleCubeModel(faceQuads, controller.isShaded);
    }

    private List<BakedQuad> makeSideFace(EnumFacing face, QuadInputs qi, FaceJoinState fjs, int cutColor, EnumFacing.Axis axis)
    {
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        
        if(fjs != FaceJoinState.NO_FACE)
        {
            EnumFacing topFace = ModelReference.getAxisTop(axis);
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
            
            float leftMarginWidth = hasLeftJoin ? marginOffset * cutWidth : baseMarginWidth;
            float rightMarginWidth = hasRightJoin ? marginOffset * cutWidth : baseMarginWidth;
            float topCapHeight = hasTopJoin ? 0 : baseMarginWidth;
            float bottomCapHeight = hasBottomJoin ? 0 : baseMarginWidth;;
            
            if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
            {
                //bottom
                if(!hasBottomJoin)
                {
                    qi.setupFaceQuad(face, 0.0F, 0.0F, 1.0F, bottomCapHeight, 0.0F, topFace);
                    builder.add(qi.createNormalQuad());
                }              

                //top
                if(!hasTopJoin)
                {
                    qi.setupFaceQuad(face, 0.0F, 1.0F - topCapHeight, 1.0F, 1.0F, 0.0F, topFace);
                    builder.add(qi.createNormalQuad());
                }

                //left margin
                if(leftMarginWidth > 0)
                {
                    qi.setupFaceQuad(face, 0.0F, bottomCapHeight, leftMarginWidth, 1.0F - topCapHeight, 0.0F, topFace);
                    builder.add(qi.createNormalQuad());
                }

                // right margin
                if(rightMarginWidth > 0)
                {
                    qi.setupFaceQuad(face, 1.0F - rightMarginWidth, bottomCapHeight, 1.0F, 1.0F - topCapHeight, 0.0F, topFace);
                    builder.add(qi.createNormalQuad());
                }

                //splines
                for(int i = 0; i < actualCutCount - 1; i++)
                {
                    qi.setupFaceQuad(face, leftMarginWidth + cutWidth * 2 * i + cutWidth, bottomCapHeight, leftMarginWidth + cutWidth * 2 * (i + 1), 1.0F - topCapHeight, 0.0F, topFace);
                    builder.add(qi.createNormalQuad());
                }
                
                // top left corner
                if(fjs.needsCorner(topFace, leftFace, face))
                {
                    qi.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 1 - baseMarginWidth, leftMarginWidth + cutWidth, 1, 0, topFace);
                    builder.add(qi.createNormalQuad());
                }
                
                // bottom left corner
                if(fjs.needsCorner(bottomFace, leftFace, face))
                {
                    qi.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 0, leftMarginWidth + cutWidth, baseMarginWidth, 0, topFace);
                    builder.add(qi.createNormalQuad());
                }
         
                // top right corner
                if(fjs.needsCorner(topFace, rightFace, face))
                {
                    qi.setupFaceQuad(face, 1 - rightMarginWidth - cutWidth, 1 - baseMarginWidth, Math.min(1 - rightMarginWidth, 1), 1, 0, topFace);
                    builder.add(qi.createNormalQuad());
                }
                
                // bottom right corner
                if(fjs.needsCorner(bottomFace, rightFace, face))
                {
                    qi.setupFaceQuad(face, 1 - rightMarginWidth - cutWidth, 0, Math.min(1 - rightMarginWidth, 1), baseMarginWidth, 0, topFace);
                    builder.add(qi.createNormalQuad());
                }
            }
            
            if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
            {
                for(int i = 0; i < actualCutCount; i++)
                {
                    float sx0 = Math.max(0.0F, leftMarginWidth + cutWidth * 2 * i);
                    float sx1 = Math.min(1.0F, leftMarginWidth + cutWidth * 2 * i + cutWidth);
          
                    // left face
                    if(sx0 > 0.0001)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(rightFace, bottomCapHeight, 1.0F-cutDepth, 1.0F-topCapHeight, 1.0F, 1 - sx0, face));
                        builder.add(qi.createNormalQuad());
                    }
        
                    // right face
                    if(sx1 < 0.9999)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(leftFace, topCapHeight, 1.0F-cutDepth, 1.0F-bottomCapHeight, 1.0F, sx1, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // top face
                    if(topCapHeight > 0)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, sx0, 1.0F-cutDepth, sx1, 1.0F, 1-topCapHeight, face));
                        builder.add(qi.createNormalQuad());
                    }
        
                    // bottom face
                    if(bottomCapHeight > 0)
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, 1-sx1, 1.0F-cutDepth, 1-sx0, 1.0F, 1-bottomCapHeight, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // top left corner
                    if(fjs.needsCorner(topFace, leftFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0), 1.0F-cutDepth, leftMarginWidth + cutWidth, 1.0F, 1-baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // bottom left corner
                    if(fjs.needsCorner(bottomFace, leftFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, 1 - leftMarginWidth - cutWidth, 1.0F-cutDepth, Math.min(1 - leftMarginWidth, 1), 1.0F, 1-baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());

                    }
             
                    // top right corner
                    if(fjs.needsCorner(topFace, rightFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(bottomFace, 1 - rightMarginWidth - cutWidth, 1.0F-cutDepth, Math.min(1 - rightMarginWidth, 1), 1.0F, 1-baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }
                    
                    // bottom right corner
                    if(fjs.needsCorner(bottomFace, rightFace, face))
                    {
                        setupCutSideQuad(qi, cutColor,
                                new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0), 1.0F-cutDepth, rightMarginWidth + cutWidth, 1.0F, 1-baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }
                }
                
                // bottom can be a single poly
                QuadInputs qiCut = qi.clone();
                qiCut.color = cutColor; 
                qiCut.setupFaceQuad(face, Math.max(0.0F, leftMarginWidth), bottomCapHeight, Math.min(1.0F, 1.0F - rightMarginWidth), 1.0F - topCapHeight, cutDepth, topFace);
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
                        fjs.isJoined(FaceSide.LEFT) ? 0 : baseMarginWidth, 
                        fjs.isJoined(FaceSide.BOTTOM) ? 0 : baseMarginWidth, 
                        fjs.isJoined(FaceSide.RIGHT) ? 1 : 1-baseMarginWidth, 
                        fjs.isJoined(FaceSide.TOP) ? 1 : 1-baseMarginWidth, 
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
                                new FaceVertex(baseMarginWidth, 1-baseMarginWidth),
                                new FaceVertex(baseMarginWidth, 1-baseMarginWidth),
                                new FaceVertex(baseMarginWidth, 1),
                                new FaceVertex(0, 1), 
                                0.0F, side);
                        builder.add(qi.createNormalQuad());
                        qi.setupFaceQuad(face, 
                                new FaceVertex(1-baseMarginWidth, 1),  
                                new FaceVertex(1-baseMarginWidth, 1),
                                new FaceVertex(1-baseMarginWidth, 1-baseMarginWidth), 
                                new FaceVertex(1, 1), 
                                0.0F, side);
                        builder.add(qi.createNormalQuad());                
                    }

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                    {
                        // margin corner sides
                        setupCutSideQuad(qi, cutColor, 
                                new SimpleQuadBounds(Useful.rightOf(face, side), 1-baseMarginWidth, 1-cutDepth, 1, 1, 1-baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());

                        setupCutSideQuad(qi, cutColor, 
                                new SimpleQuadBounds(Useful.leftOf(face, side), 0, 1-cutDepth, baseMarginWidth, 1, 1-baseMarginWidth, face));
                        builder.add(qi.createNormalQuad());
                    }

                    //splines
                    for (int i = 0; i < myController.cutCount / 2; i++)
                    {
                        float xLeft = baseMarginWidth + (i * 2 + 1) * this.cutWidth;
                        float xRight = Math.min(xLeft + cutWidth, 0.5F);

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                        {
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(xLeft, 1-xLeft),  
                                    new FaceVertex(xRight, 1-xRight),
                                    new FaceVertex(xRight, 1), 
                                    new FaceVertex(xLeft, 1), 
                                    0.0F, side);
                            builder.add(qi.createNormalQuad());
                            // mirror on right side, reverse winding order
                            builder.add(qi.createNormalQuad());                             
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(1-xRight, 1-xRight),  
                                    new FaceVertex(1-xLeft, 1-xLeft),
                                    new FaceVertex(1-xLeft, 1), 
                                    new FaceVertex(1-xRight, 1), 
                                    0.0F, side);
                            builder.add(qi.createNormalQuad());
                        }

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                        {
                            // cut sides

                            // with odd number of cuts, these overlap in middle, avoid with this check
                            if(xLeft < 0.49999)
                            {
                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.leftOf(face, side), 0, 1-cutDepth, xLeft, 1, xLeft, face));
                                builder.add(qi.createNormalQuad());

                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.rightOf(face, side), 1-xLeft, 1-cutDepth, 1, 1, xLeft, face));
                                builder.add(qi.createNormalQuad());
                            }
                            if(xRight < 0.49999)
                            {
                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.rightOf(face, side), 1-xRight, 1-cutDepth, 1, 1, 1-xRight, face));
                                builder.add(qi.createNormalQuad());

                                setupCutSideQuad(qi, cutColor, 
                                        new SimpleQuadBounds(Useful.leftOf(face, side), 0, 1-cutDepth, xRight, 1, 1-xRight, face));

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
                                new FaceVertex(baseMarginWidth, 1-baseMarginWidth),  
                                new FaceVertex(1-baseMarginWidth, 1-baseMarginWidth),
                                new FaceVertex(1, 1), 
                                new FaceVertex(0, 1), 
                                0.0F, side);
                        builder.add(qi.createNormalQuad());   
                    }

                    if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                    {
                        // outer cut sides
                        for(int i = 0; i < (myController.cutCount + 1) / 2; i++)
                        {
                            float offset = baseMarginWidth + (cutWidth * 2.0F * i);

                            setupCutSideQuad(qi, cutColor,
                                    new SimpleQuadBounds(side.getOpposite(), offset, 1-cutDepth, 1-offset, 1.0F, 1-offset, face));
                            builder.add(qi.createNormalQuad());

                        }
                    }

                    for(int i = 0; i < myController.cutCount / 2; i++)
                    {
                        float offset = baseMarginWidth + (cutWidth * (2.0F * i + 1));

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_OVERLAY)
                        {
                            // inner cut sides
                            setupCutSideQuad(qi, cutColor,
                                    new SimpleQuadBounds(side, offset, 1-cutDepth, 1-offset, 1.0F, offset, face));
                            builder.add(qi.createNormalQuad());
                        }

                        if(myController.modelType != ColumnSquareController.ModelType.LAMP_BASE)
                        {
                            // spline / center
                            qi.setupFaceQuad(face, 
                                    new FaceVertex(offset+cutWidth, 1-offset-cutWidth),  
                                    new FaceVertex(1-offset-cutWidth, 1-offset-cutWidth),
                                    new FaceVertex(1-offset, 1-offset), 
                                    new FaceVertex(offset, 1-offset), 
                                    0.0F, side);
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
        IBakedModel template = getBlockModel(modelState, colorProvider);
        ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder();
        general.addAll(template.getGeneralQuads());
        for(EnumFacing face : EnumFacing.VALUES)
        {
            general.addAll(template.getFaceQuads(face));
        }        
        return general.build();
    }

    @Override
    public void handleBakeEvent(ModelBakeEvent event)
    {
//        templateModels = new IBakedModel[controller.getShapeCount()];
//
//        for(int modelIndex = 0; modelIndex < controller.getShapeCount(); modelIndex++)
//        {
//            int shapeIndex = myController.getShapeFromModelIndex(modelIndex);
//            int axisOrdinal = myController.getAxisFromModelIndex(modelIndex);
//            int textureOffset = controller.alternateTextureCount * myController.getTextureFromModelIndex(modelIndex);
//            String modelName = myController.modelNames[ myController.MODEL_FOR_SHAPE_INDEX[shapeIndex].index];
//            TRSRTransformation transform = myController.ROTATION_LOOKUPS[axisOrdinal][shapeIndex];
//            
//            ImmutableMap.Builder<String, String> textures = new ImmutableMap.Builder<String, String>();
//            textures.put("inner", controller.getTextureName(textureOffset + 0));
//            textures.put("outer", controller.getTextureName(textureOffset + 8));
//            textures.put("column_face", controller.getTextureName(textureOffset + 7));
//            textures.put("cap_opposite_neighbors", controller.getTextureName(textureOffset + 7));
//            textures.put("cap_three_neighbors", controller.getTextureName(textureOffset + 6));
//            textures.put("cap_adjacent_neighbors", controller.getTextureName(textureOffset + 2));
//            textures.put("cap_one_neighbor", controller.getTextureName(textureOffset + 3));
//            textures.put("cap_four_neighbors", controller.getTextureName(textureOffset + 1));
//            textures.put("cap_no_neighbors", controller.getTextureName(textureOffset + 5));
//            textures.put("cap_inner_side", controller.getTextureName(textureOffset + 4));
//    
//            try 
//            {
//                IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ModelResourceLocation(modelName));
//                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
//                ModelBlock modelBlock = ModelBlock.deserialize(reader);
//                ModelLoader.WeightedRandomModel weighted = (WeightedRandomModel)event.modelLoader.getModel(new ModelResourceLocation(modelName));
//                IRetexturableModel template = event.modelLoader.
//                //IRetexturableModel template = (IRetexturableModel) event.modelLoader.getModel(new ModelResourceLocation(modelName));
//                IModel model = template.retexture(textures.build());
//                templateModels[modelIndex] = model.bake(transform, DefaultVertexFormats.ITEM, ModelReference.DEFAULT_TEXTURE_GETTER);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//            
//            // Provide a default to contain the damage if we derp it up.
//            if(templateModels[modelIndex] == null)
//            {
//                templateModels[modelIndex] = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
//            }
//        }
        
//        @Override
//        public IBakedModel getBlockModel(ModelState modelState, IColorProvider colorProvider)
//        {
//            IBakedModel template = templateModels[modelState.getClientShapeIndex(controller.renderLayer.ordinal())];
//            int color = colorProvider.getColor(modelState.colorIndex).getColorMap(EnumColorMap.BASE);
//            return new SimpleModel(template, color);
//        }
//
//        @Override
//        public List<BakedQuad> getItemQuads(ModelState modelState, IColorProvider colorProvider)
//        {
//            IBakedModel template = getBlockModel(modelState, colorProvider);
//            ImmutableList.Builder<BakedQuad> general = new ImmutableList.Builder();
//            general.addAll(template.getGeneralQuads());
//            for(EnumFacing face : EnumFacing.VALUES)
//            {
//                general.addAll(template.getFaceQuads(face));
//            }        
//            return general.build();
//        }
    }
}
