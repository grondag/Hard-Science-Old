package grondag.adversity.niceblock.newmodel;

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

        faceQuads[EnumFacing.UP.ordinal()] = makeFace(EnumFacing.UP, quadInputs);
        faceQuads[EnumFacing.DOWN.ordinal()] = makeFace(EnumFacing.DOWN, quadInputs);
        faceQuads[EnumFacing.EAST.ordinal()] = makeFace(EnumFacing.EAST, quadInputs);
        faceQuads[EnumFacing.WEST.ordinal()] = makeFace(EnumFacing.WEST, quadInputs);
        faceQuads[EnumFacing.NORTH.ordinal()] = makeFace(EnumFacing.NORTH, quadInputs);
        faceQuads[EnumFacing.SOUTH.ordinal()] = makeFace(EnumFacing.SOUTH, quadInputs);
        
        return new SimpleCubeModel(faceQuads, controller.isShaded);
    }

    private List<BakedQuad> makeFace(EnumFacing face, QuadInputs qi)
    {
       ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
       
       setupFaceQuad(face, qi, 0.25F, 0.0F, 0.75F, 1.0F, 0.05F);

       builder.add(qi.createNormalQuad());
       return builder.build();
    }
    
//case DOWN:
//    f3 = f * 16.0F;
//    f4 = (1.0F - f2) * 16.0F;
//    break;
//case UP:
//    f3 = f * 16.0F;
//    f4 = f2 * 16.0F;
//    break;
//case NORTH:
//    f3 = (1.0F - f) * 16.0F;
//    f4 = (1.0F - f1) * 16.0F;
//    break;
//case SOUTH:
//    f3 = f * 16.0F;
//    f4 = (1.0F - f1) * 16.0F;
//    break;
//case WEST:
//    f3 = f2 * 16.0F;
//    f4 = (1.0F - f1) * 16.0F;
//    break;
//case EAST:
//    f3 = (1.0F - f2) * 16.0F;
//    f4 = (1.0F - f1) * 16.0F;
    
    private void setupFaceQuad(EnumFacing face, QuadInputs qi, float x0, float y0, float x1, float y1, float depth)
    {
        switch(face)
        {
        case UP:
            qi.v1 = new Vertex(x0, 1-depth, y0, x0 * 16.0F, y0 * 16.0F);
            qi.v2 = new Vertex(x0, 1-depth, y1, x0 * 16.0F, y1 * 16.0F);
            qi.v3 = new Vertex(x1, 1-depth, y1, x1 * 16.0F, y1 * 16.0F);
            qi.v4 = new Vertex(x1, 1-depth, y0, x1 * 16.0F, y0 * 16.0F);
            qi.side = EnumFacing.UP;
            break;

        case DOWN:     
            qi.v1 = new Vertex(x1, depth, y1, x1 * 16.0F, (1-y1) * 16.0F);
            qi.v2 = new Vertex(x0, depth, y1, x0 * 16.0F, (1-y1) * 16.0F); 
            qi.v3 = new Vertex(x0, depth, y0, x0 * 16.0F, (1-y0) * 16.0F); 
            qi.v4 = new Vertex(x1, depth, y0, x1 * 16.0F, (1-y0) * 16.0F);
            qi.side = EnumFacing.DOWN;
            break;

        case WEST:
            qi.v1 = new Vertex(depth, y0, x0, x0 * 16.0F, (1-y0) * 16.0F);
            qi.v2 = new Vertex(depth, y0, x1, x1 * 16.0F, (1-y0) * 16.0F);
            qi.v3 = new Vertex(depth, y1, x1, x1 * 16.0F, (1-y1) * 16.0F);
            qi.v4 = new Vertex(depth, y1, x0, x0 * 16.0F, (1-y1) * 16.0F);
            qi.side = EnumFacing.WEST;
            break;
            
        case EAST:
            qi.v1 = new Vertex(1-depth, y0, x0, (1-x0) * 16.0F, (1-y0) * 16.0F);
            qi.v2 = new Vertex(1-depth, y1, x0, (1-x0) * 16.0F, (1-y1) * 16.0F);
            qi.v3 = new Vertex(1-depth, y1, x1, (1-x1) * 16.0F, (1-y1) * 16.0F);
            qi.v4 = new Vertex(1-depth, y0, x1, (1-x1) * 16.0F, (1-y0) * 16.0F);
            qi.side = EnumFacing.EAST;
            break;

        case NORTH:
            qi.v1 = new Vertex(x0, y0, depth, (1-x0) * 16.0F, (1-y0) * 16.0F);
            qi.v2 = new Vertex(x0, y1, depth, (1-x0) * 16.0F, (1-y1) * 16.0F);
            qi.v3 = new Vertex(x1, y1, depth, (1-x1) * 16.0F, (1-y1) * 16.0F);
            qi.v4 = new Vertex(x1, y0, depth, (1-x1) * 16.0F, (1-y0) * 16.0F);
            qi.side = EnumFacing.NORTH;
            break;

        case SOUTH:
            qi.v1 = new Vertex(x0, y0, 1-depth, x0 * 16.0F, (1-y0) * 16.0F);
            qi.v2 = new Vertex(x1, y0, 1-depth, x1 * 16.0F, (1-y0) * 16.0F);
            qi.v3 = new Vertex(x1, y1, 1-depth, x1 * 16.0F, (1-y1) * 16.0F);
            qi.v4 = new Vertex(x0, y1, 1-depth, x0 * 16.0F, (1-y1) * 16.0F);
            qi.side = EnumFacing.SOUTH;
            break;
        }
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
