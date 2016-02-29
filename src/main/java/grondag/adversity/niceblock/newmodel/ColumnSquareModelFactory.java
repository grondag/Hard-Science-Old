package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.model.ModelCookbook;
import grondag.adversity.niceblock.model.ModelCookbook.Ingredients;
import grondag.adversity.niceblock.newmodel.color.IColorProvider;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ColumnSquareModelFactory extends BakedModelFactory
{
    /** convenience reference to avoid casts everywhere */
    private final ColumnSquareController myController;
    
    protected IBakedModel[] templateModels;
    
    public ColumnSquareModelFactory(ColumnSquareController controller)
    {
        super(controller);
        myController = controller;
    }

    @Override
    public IBakedModel getBlockModel(ModelState modelState, IColorProvider colorProvider)
    {
        IBakedModel template = templateModels[modelState.getClientShapeIndex(controller.renderLayer.ordinal())];
        int color = colorProvider.getColor(modelState.colorIndex).getColorMap(EnumColorMap.BASE);
        return new SimpleModel(template, color);
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
        templateModels = new IBakedModel[controller.getShapeCount()];

        for(int modelIndex = 0; modelIndex < controller.getShapeCount(); modelIndex++)
        {
            int shapeIndex = myController.getShapeFromModelIndex(modelIndex);
            int axisOrdinal = myController.getAxisFromModelIndex(modelIndex);
            int textureOffset = controller.alternateTextureCount * myController.getTextureFromModelIndex(modelIndex);
            String modelName = myController.modelNames[ myController.MODEL_FOR_SHAPE_INDEX[shapeIndex].index];
            TRSRTransformation transform = myController.ROTATION_LOOKUPS[axisOrdinal][shapeIndex];
            
            ImmutableMap.Builder<String, String> textures = new ImmutableMap.Builder<String, String>();
            textures.put("inner", controller.getTextureName(textureOffset + 0));
            textures.put("outer", controller.getTextureName(textureOffset + 8));
            textures.put("column_face", controller.getTextureName(textureOffset + 7));
            textures.put("cap_opposite_neighbors", controller.getTextureName(textureOffset + 7));
            textures.put("cap_three_neighbors", controller.getTextureName(textureOffset + 6));
            textures.put("cap_adjacent_neighbors", controller.getTextureName(textureOffset + 2));
            textures.put("cap_one_neighbor", controller.getTextureName(textureOffset + 3));
            textures.put("cap_four_neighbors", controller.getTextureName(textureOffset + 1));
            textures.put("cap_no_neighbors", controller.getTextureName(textureOffset + 5));
            textures.put("cap_inner_side", controller.getTextureName(textureOffset + 4));
    
            try 
            {
                IRetexturableModel template = (IRetexturableModel) event.modelLoader.getModel(new ModelResourceLocation(modelName));
                IModel model = template.retexture(textures.build());
                templateModels[modelIndex] = model.bake(transform, DefaultVertexFormats.ITEM, ModelReference.DEFAULT_TEXTURE_GETTER);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            
            // Provide a default to contain the damage if we derp it up.
            if(templateModels[modelIndex] == null)
            {
                templateModels[modelIndex] = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
            }
        }
    }
}
