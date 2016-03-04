package grondag.adversity.niceblock.newmodel;

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
        quadInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(myController.getTextureFromModelIndex(modelState.getClientShapeIndex(controller.renderLayer.ordinal()))));

        List<BakedQuad>[] faceQuads = new List[6];

        faceQuads[EnumFacing.UP.ordinal()] = makeCapFace(EnumFacing.UP, quadInputs);
        faceQuads[EnumFacing.DOWN.ordinal()] = makeCapFace(EnumFacing.DOWN, quadInputs);
        faceQuads[EnumFacing.EAST.ordinal()] = makeSideFace(EnumFacing.EAST, quadInputs);
        faceQuads[EnumFacing.WEST.ordinal()] = makeSideFace(EnumFacing.WEST, quadInputs);
        faceQuads[EnumFacing.NORTH.ordinal()] = makeSideFace(EnumFacing.NORTH, quadInputs);
        faceQuads[EnumFacing.SOUTH.ordinal()] = makeSideFace(EnumFacing.SOUTH, quadInputs);
        
        return new SimpleCubeModel(faceQuads, controller.isShaded);
    }

    private List<BakedQuad> makeSideFace(EnumFacing face, QuadInputs qi)
    {
        float capHeight = 0.2F;
        float marginWidth = 0.2F;
        float cutDepth = 0.05F;
        int cutCount = 4;
        EnumFacing topFace = EnumFacing.UP; //Useful.rightOf(face, EnumFacing.UP);
        float cutWidth = (1 - marginWidth * 2) / (cutCount * 2 - 1);

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
          qi.setupFaceQuad(face, 0.2F, 0.5F, 0.8F, 0.95F, 0.0F, topFace, false);
//          qi.setupFaceQuad(face, 0.3F, 0.1F, 0.7F, 0.9F, 0.0F, topFace, false);

          builder.add(qi.createNormalQuad());

       
//       //top and bottom
//       qi.setupFaceQuad(face, 0.0F, 0.0F, 1.0F, capHeight, 0.0F);
//       builder.add(qi.createNormalQuad());
//       qi.setupFaceQuad(face, 0.0F, 1.0F - capHeight, 1.0F, 1.0F, 0.0F);
//       builder.add(qi.createNormalQuad());
//
//       //margins
//       qi.setupFaceQuad(face, 0.0F, capHeight, marginWidth, 1.0F - capHeight, 0.0F);
//       builder.add(qi.createNormalQuad());
//       qi.setupFaceQuad(face, 1.0F - marginWidth, capHeight, 1.0F, 1.0F - capHeight, 0.0F);
//       builder.add(qi.createNormalQuad());
//
//       //cuts
//       QuadInputs qiCut = qi.clone();
//       qiCut.color = QuadFactory.shadeColor(qi.color, 0.85F, false); 
//       for(int i = 0; i < cutCount; i++)
//       {
//           // bottom
//           float sx0 = marginWidth + cutWidth * 2 * i;
//           float sx1 = marginWidth + cutWidth * 2 * i + cutWidth;
//           
//           qiCut.setupFaceQuad(face, sx0, capHeight, sx1, 1.0F - capHeight, cutDepth);
//           builder.add(qiCut.createNormalQuad());

//           // left face
//           qi.setupFaceQuad(Useful.rightOf(face, axis), 0, capHeight, cutDepth, 1.0F - capHeight, 1 - sx0);
//           builder.add(qi.createNormalQuad());
//
//           // right face
//           qi.setupFaceQuad(Useful.leftOf(face, axis), 1.0F - cutDepth, capHeight, 1.0F, 1.0F - capHeight, sx1);
//           builder.add(qi.createNormalQuad());

//           // top face
//           qi.setupFaceQuad(Useful.bottomOf(face, axis), sx0, 1.0F - cutDepth, sx1, 1.0F, 1-capHeight);
//           builder.add(qi.createNormalQuad());

//       }
//       
//       //splines
//       for(int i = 0; i < cutCount - 1; i++)
//       {
//           qi.setupFaceQuad(face, marginWidth + cutWidth * 2 * i + cutWidth, capHeight, marginWidth + cutWidth * 2 * (i + 1), 1.0F - capHeight, 0.0F);
//           builder.add(qi.createNormalQuad());
//       }
       return builder.build();
    }
    
    private List<BakedQuad> makeCapFace(EnumFacing face, QuadInputs qi)
    {
        float marginWidth = 0.2F;
        float cutDepth = 0.05F;
        int cutCount = 4;
        float cutWidth = (1 - marginWidth * 2) / (cutCount * 2 - 1);

        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
       
       //temporary
       qi.setupFaceQuad(face, 0.2F, 0.5F, 0.8F, 0.95F, 0.0F, EnumFacing.NORTH, true);
//       qi.setupFaceQuad(face, 0.3F, 0.1F, 0.7F, 0.9F, 0.0F, Useful.rightOf(EnumFacing.UP, EnumFacing.NORTH), false);

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
