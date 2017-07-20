package grondag.hard_science.superblock.model.shape;

import java.util.List;

import javax.vecmath.Matrix4d;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.library.render.RawQuad;
import grondag.hard_science.library.world.Rotation;
import grondag.hard_science.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.EnumFacing;

public class StairMeshFactory extends AbstractWedgeMeshFactory
{
    private static ShapeMeshGenerator instance;
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new StairMeshFactory();
        return instance; 
    }
    
    private StairMeshFactory()
    {
        super();
    }
    
    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east faces.

        Matrix4d matrix = getMatrix4d(modelState);
        
        RawQuad template = new RawQuad();
        template.color = 0xFFFFFFFF;
        template.rotation = Rotation.ROTATE_NONE;
        template.isFullBrightness = false;
        template.lockUV = true;

        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
        
        RawQuad quad = template.clone();
        quad.surfaceInstance = BACK_AND_BOTTOM_INSTANCE;
        quad.setFace(EnumFacing.NORTH);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0, EnumFacing.UP);
        builder.add(quad.transform(matrix));
      
        quad = template.clone();
        quad.surfaceInstance = BACK_AND_BOTTOM_INSTANCE;
        quad.setFace(EnumFacing.EAST);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.UP, 0.0, 0.5, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.UP, 0.5, 0.0, 1.0, 0.5, 0.0, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.DOWN, 0.0, 0.5, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.DOWN, 0.0, 0.0, 0.5, 0.5, 0.0, EnumFacing.NORTH);
        builder.add(quad.transform(matrix));

        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.SOUTH, 0.5, 0.0, 1.0, 1.0, 0.0, EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.WEST, 0.0, 0.0, 0.5, 1.0, 0.0, EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        
        quad = template.clone();
        quad.surfaceInstance = TOP_INSTANCE;
        quad.setupFaceQuad(EnumFacing.SOUTH, 0.0, 0.0, 0.5, 1.0, 0.5, EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        quad = template.clone();
        quad.surfaceInstance = TOP_INSTANCE;
        quad.setupFaceQuad(EnumFacing.WEST, 0.5, 0.0, 1.0, 1.0, 0.5, EnumFacing.UP);
        builder.add(quad.transform(matrix));
        
        return builder.build();
    }
}
