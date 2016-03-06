package grondag.adversity.niceblock.newmodel;

import grondag.adversity.Adversity;
import grondag.adversity.library.Useful;
import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;
import grondag.adversity.niceblock.newmodel.QuadFactory.QuadInputs;
import grondag.adversity.niceblock.newmodel.QuadFactory.Vertex;
import grondag.adversity.niceblock.newmodel.color.ColorMap;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
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
import net.minecraftforge.common.property.IExtendedBlockState;

public class ColumnSquareModelFactory extends BakedModelFactory
{
    /** convenience reference to avoid casts everywhere */
    private final ColumnSquareController myController;
    
//    protected IBakedModel[] templateModels;
    
    public ColumnSquareModelFactory(ColumnSquareController controller)
    {
        super(controller);
        myController = controller;
    }

    @Override
    public IBakedModel getBlockModel(ModelState modelState, IColorProvider colorProvider)
    {
        QuadInputs quadInputs = new QuadInputs();
        ColorMap colorMap = colorProvider.getColor(modelState.getColorIndex());
        quadInputs.color = colorMap.getColorMap(controller.renderLayer == EnumWorldBlockLayer.SOLID ? EnumColorMap.BASE : EnumColorMap.HIGHLIGHT);
        int clientShapeIndex = modelState.getClientShapeIndex(controller.renderLayer.ordinal());
        EnumFacing.Axis axis = EnumFacing.Axis.values()[myController.getAxisFromModelIndex(clientShapeIndex)];
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(controller.getTextureName(myController.getTextureFromModelIndex(clientShapeIndex)));
        ModelReference.AxisJoin modelJoin = new ModelReference.AxisJoin(myController.getShapeFromModelIndex(clientShapeIndex), axis);

        
        List<BakedQuad>[] faceQuads = new List[6];

        for(EnumFacing face : EnumFacing.values())
        {
            if(face.getAxis() == axis)
            {
                faceQuads[face.ordinal()] = makeCapFace(face, quadInputs, modelJoin);
            }
            else
            {
                faceQuads[face.ordinal()] = makeSideFace(face, quadInputs, modelJoin);
            }
        }
        
        return new SimpleCubeModel(faceQuads, controller.isShaded);
    }

    private List<BakedQuad> makeSideFace(EnumFacing face, QuadInputs qi, ModelReference.AxisJoin modelJoin)
    {
        EnumFacing topFace = ModelReference.getAxisTop(modelJoin.axis);
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
        
        if(modelJoin.isJoined(face))
        {
            qi.setupFaceQuad(face, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, topFace, true);
            builder.add(qi.createNormalQuad());
            return builder.build();
        }

        int baseCutCount = 5;
        int actualCutCount = baseCutCount;
        if (modelJoin.isJoined(Useful.leftOf(face, topFace))) actualCutCount++;
        if (modelJoin.isJoined(Useful.rightOf(face, topFace))) actualCutCount++;
        float cutWidth = 0.5F / (baseCutCount + 2.0F);
        float baseMarginWidth = 2.5F * cutWidth;
        float leftMarginWidth = modelJoin.isJoined(Useful.leftOf(face, topFace)) ? 0.5F * cutWidth : baseMarginWidth;
        float rightMarginWidth = modelJoin.isJoined(Useful.rightOf(face, topFace)) ? 0.5F * cutWidth : baseMarginWidth;
        float topCapHeight = modelJoin.isJoined(topFace) ? 0 : baseMarginWidth;
        float bottomCapHeight = modelJoin.isJoined(topFace.getOpposite()) ? 0 : baseMarginWidth;;
        float cutDepth = 0.05F;

        //top and bottom
        qi.setupFaceQuad(face, 0.0F, 0.0F, 1.0F, bottomCapHeight, 0.0F, topFace, true);
        builder.add(qi.createNormalQuad());
        qi.setupFaceQuad(face, 0.0F, 1.0F - topCapHeight, 1.0F, 1.0F, 0.0F, topFace, true);
        builder.add(qi.createNormalQuad());

        //margins
        qi.setupFaceQuad(face, 0.0F, bottomCapHeight, leftMarginWidth, 1.0F - topCapHeight, 0.0F, topFace, true);
        builder.add(qi.createNormalQuad());
        qi.setupFaceQuad(face, 1.0F - rightMarginWidth, bottomCapHeight, 1.0F, 1.0F - topCapHeight, 0.0F, topFace, true);
        builder.add(qi.createNormalQuad());

        //cuts
        QuadInputs qiCut = qi.clone();
        qiCut.color = QuadFactory.shadeColor(qi.color, 0.85F, false); 
        for(int i = 0; i < actualCutCount; i++)
        {
            // bottom
            float sx0 = leftMarginWidth + cutWidth * 2 * i;
            float sx1 = leftMarginWidth + cutWidth * 2 * i + cutWidth;

            qiCut.setupFaceQuad(face, sx0, bottomCapHeight, sx1, 1.0F - topCapHeight, cutDepth, topFace, true);
            builder.add(qiCut.createNormalQuad());

            // left face
            qi.setupFaceQuad(Useful.rightOf(face, topFace), bottomCapHeight, 1.0F-cutDepth, 1.0F-topCapHeight, 1.0F, 1 - sx0, face, true);
            builder.add(qi.createNormalQuad());

            // right face
            qi.setupFaceQuad(Useful.leftOf(face, topFace), topCapHeight, 1.0F-cutDepth, 1.0F-bottomCapHeight, 1.0F, sx1, face, true);
            builder.add(qi.createNormalQuad());

            // top face
            if(topCapHeight > 0)
            {
                qi.setupFaceQuad(Useful.bottomOf(face, topFace), sx0, 1.0F-cutDepth, sx1, 1.0F, 1-topCapHeight, face, true);
                builder.add(qi.createNormalQuad());
            }

            if(bottomCapHeight > 0)
            {
                // bottom face
                qi.setupFaceQuad(topFace, 1-sx1, 1.0F-cutDepth, 1-sx0, 1.0F, 1-bottomCapHeight, face, true);
                builder.add(qi.createNormalQuad());
            }
        }

        //splines
        for(int i = 0; i < actualCutCount - 1; i++)
        {
            qi.setupFaceQuad(face, leftMarginWidth + cutWidth * 2 * i + cutWidth, bottomCapHeight, leftMarginWidth + cutWidth * 2 * (i + 1), 1.0F - topCapHeight, 0.0F, topFace, true);
            builder.add(qi.createNormalQuad());
        }
        return builder.build();
    }
    
    private List<BakedQuad> makeCapFace(EnumFacing face, QuadInputs qi, ModelReference.AxisJoin modelJoin)
    {
        float marginWidth = 0.2F;
        float cutDepth = 0.05F;
        int cutCount = 4;
        float cutWidth = (1 - marginWidth * 2) / (cutCount * 2 - 1);

        if(modelJoin.isJoined(face))
        {
            return Collections.EMPTY_LIST;
        }
        
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
       
        //temporary
        qi.setupFaceQuad(face, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, face, true);
       
        builder.add(qi.createNormalQuad());
        return builder.build();
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
