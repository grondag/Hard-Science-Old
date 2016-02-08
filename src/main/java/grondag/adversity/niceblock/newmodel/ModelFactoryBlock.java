package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.newmodel.QuadFactory.CubeInputs;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.event.ModelBakeEvent;

public class ModelFactoryBlock extends BakedModelFactory
{
    public ModelFactoryBlock(ContollerBlock controller)
    {
        super(controller);
    }

    @Override
    public IBakedModel getBlockModel(ModelState modelState)
    {
        CubeInputs cubeInputs = new CubeInputs();
        
        ContollerBlock controller = (ContollerBlock)this.controller;
        
        cubeInputs.color = controller.renderLayer == EnumWorldBlockLayer.SOLID ? modelState.getColor().base : modelState.getColor().highlight;
        cubeInputs.textureRotation = controller.getTextureRotationFromShapeIndex(modelState.getShapeIndex());
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(controller.getTextureOffsetFromShapeIndex(modelState.getShapeIndex())));

        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        
        List<BakedQuad>[] faceQuads = new List[6];

        faceQuads[EnumFacing.UP.ordinal()] = cubeInputs.makeFace(EnumFacing.UP);
        faceQuads[EnumFacing.DOWN.ordinal()] = cubeInputs.makeFace(EnumFacing.DOWN);
        faceQuads[EnumFacing.EAST.ordinal()] = cubeInputs.makeFace(EnumFacing.EAST);
        faceQuads[EnumFacing.WEST.ordinal()] = cubeInputs.makeFace(EnumFacing.WEST);
        faceQuads[EnumFacing.NORTH.ordinal()] = cubeInputs.makeFace(EnumFacing.NORTH);
        faceQuads[EnumFacing.SOUTH.ordinal()] = cubeInputs.makeFace(EnumFacing.SOUTH);
        
        return new SimpleCubeModel(faceQuads, controller.isShaded);
    }

    @Override
    public List<BakedQuad> getItemQuads(ModelState modelState)
    {
        CubeInputs cubeInputs = new CubeInputs();
        
        ContollerBlock controller = (ContollerBlock)this.controller;
        
        cubeInputs.color = controller.renderLayer == EnumWorldBlockLayer.SOLID ? modelState.getColor().base : modelState.getColor().highlight;
        cubeInputs.textureSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                controller.getTextureName(0));

        cubeInputs.u0 = 0;
        cubeInputs.v0 = 0;
        cubeInputs.u1 = 16;
        cubeInputs.v1 = 16;
        cubeInputs.isItem = true;
        cubeInputs.isOverlay = controller.renderLayer != EnumWorldBlockLayer.SOLID;

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
    public void handleBakeEvent(ModelBakeEvent event)
    {
        // nothing to do, all baking is lazy
    }
}