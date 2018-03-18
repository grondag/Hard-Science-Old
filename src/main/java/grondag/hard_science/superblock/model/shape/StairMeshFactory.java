package grondag.hard_science.superblock.model.shape;

import java.util.List;

import javax.vecmath.Matrix4d;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.render.RawQuad;
import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.superblock.model.state.ModelState;
import net.minecraft.util.EnumFacing;

public class StairMeshFactory extends AbstractWedgeMeshFactory
{
    @Override
    public List<RawQuad> getShapeQuads(ModelState modelState)
    {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east faces.

        Matrix4d matrix = modelState.getMatrix4d();
        
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
        quad = quad.transform(matrix);
        builder.add(quad);
      
        quad = template.clone();
        quad.surfaceInstance = BACK_AND_BOTTOM_INSTANCE;
        quad.setFace(EnumFacing.EAST);
        quad.setupFaceQuad(0.0, 0.0, 1.0, 1.0, 0.0, EnumFacing.UP);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts.  AO is done by vertex, and having
        // a T-junction tends to mess about with the results.
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.UP, 0.0, 0.5, 0.5, 1.0, 0.0, EnumFacing.NORTH);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.UP, 0.5, 0.5, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.UP, 0.5, 0.0, 1.0, 0.5, 0.0, EnumFacing.NORTH);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts.  AO is done by vertex, and having
        // a T-junction tends to mess about with the results.
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.DOWN, 0.0, 0.5, 0.5, 1.0, 0.0, EnumFacing.NORTH);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.DOWN, 0.5, 0.5, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.DOWN, 0.0, 0.0, 0.5, 0.5, 0.0, EnumFacing.NORTH);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.SOUTH, 0.5, 0.0, 1.0, 1.0, 0.0, EnumFacing.UP);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = TOP_INSTANCE;
        quad.setupFaceQuad(EnumFacing.SOUTH, 0.0, 0.0, 0.5, 1.0, 0.5, EnumFacing.UP);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = SIDE_INSTANCE;
        quad.setupFaceQuad(EnumFacing.WEST, 0.0, 0.0, 0.5, 1.0, 0.0, EnumFacing.UP);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        quad = template.clone();
        quad.surfaceInstance = TOP_INSTANCE;
        quad.setupFaceQuad(EnumFacing.WEST, 0.5, 0.0, 1.0, 1.0, 0.5, EnumFacing.UP);
        quad = quad.transform(matrix);
        builder.add(quad);
        
        return builder.build();
    }
}
