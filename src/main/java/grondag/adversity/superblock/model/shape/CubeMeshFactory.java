package grondag.adversity.superblock.model.shape;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.STATE_FLAG_NONE;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.Rotation;
import grondag.adversity.library.model.quadfactory.CubeInputs;
import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.color.ColorMap;
import grondag.adversity.niceblock.color.NoColorMapProvider;
import grondag.adversity.niceblock.color.ColorMap.EnumColorMap;
import grondag.adversity.superblock.model.painter.surface.Surface;
import grondag.adversity.superblock.model.painter.surface.SurfaceTopology;
import grondag.adversity.superblock.model.painter.surface.SurfaceType;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.model.state.ModelStateFactory.StateFormat;
import net.minecraft.util.EnumFacing;

public class CubeMeshFactory extends ShapeMeshGenerator
{
    private static ShapeMeshGenerator instance;
    
    public static ShapeMeshGenerator getShapeMeshFactory()
    {
        if(instance == null) instance = new CubeMeshFactory();
        return instance; 
    };
    
    /** never changes so may as well save it */
    private final Collection<RawQuad> cachedQuads;
    
    protected CubeMeshFactory()
    {
        super(StateFormat.BLOCK, STATE_FLAG_NONE, new Surface(0, SurfaceType.MAIN, SurfaceTopology.CUBIC));
        this.cachedQuads = getCubeQuads();
    }

    @Override
    public Collection<RawQuad> getShapeQuads(ModelState modelState)
    {
        return cachedQuads;
    }
    
    private Collection<RawQuad> getCubeQuads()
    {
        CubeInputs result = new CubeInputs();
        ColorMap colorMap = NoColorMapProvider.INSTANCE.getColorMap(0);
        result.color = colorMap.getColor(EnumColorMap.BASE);
        result.textureRotation = Rotation.ROTATE_NONE;
        result.lightingMode = LightingMode.SHADED;
        result.u0 = 0;
        result.v0 = 0;
        result.u1 = 16;
        result.v1 = 16;
        result.isOverlay = false;
        result.surface = this.surfaces.get(0);
        
        ImmutableList.Builder<RawQuad> builder = new ImmutableList.Builder<RawQuad>();
       
        builder.add(result.makeRawFace(EnumFacing.DOWN));
        builder.add(result.makeRawFace(EnumFacing.UP));
        builder.add(result.makeRawFace(EnumFacing.EAST));
        builder.add(result.makeRawFace(EnumFacing.WEST));
        builder.add(result.makeRawFace(EnumFacing.SOUTH));
        builder.add(result.makeRawFace(EnumFacing.NORTH));
       
        return builder.build();
    }
}
