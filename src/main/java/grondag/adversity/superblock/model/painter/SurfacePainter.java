package grondag.adversity.superblock.model.painter;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.model.texture.TextureProvider;
import grondag.adversity.niceblock.model.texture.TextureProviders;
import grondag.adversity.superblock.model.painter.QuadPainter.QuadPainterFactory;
import grondag.adversity.superblock.model.painter.surface.SurfaceTopology;
import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

public enum SurfacePainter
{
    NONE(STATE_FLAG_NONE, QuadPainter::makeNullQuadPainter),
    CUBIC_TILES(STATE_FLAG_NONE, CubicQuadPainterTiles::makeQuadPainter),
    CUBIC_BIGTEX(STATE_FLAG_NEEDS_POS, CubicQuadPainterBigTex::makeQuadPainter),
    CUBIC_BORDER(STATE_FLAG_NEEDS_CORNER_JOIN, CubicQuadPainterBorders::makeQuadPainter),
    CUBIC_MASONRY(STATE_FLAG_NEEDS_SIMPLE_JOIN, CubicQuadPainterMasonry::makeQuadPainter),
    SURFACE_TILES(STATE_FLAG_NONE, SurfaceQuadPainterTiles::makeQuadPainter),
    SURFACE_CYLINDER(STATE_FLAG_NONE, SurfaceQuadPainterCylinder::makeQuadPainter),
    SURFACE_TOROID(STATE_FLAG_NONE, SurfaceQuadPainterTorus::makeQuadPainter);
    
    static
    {
        CUBIC_TILES.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.BLOCK_INDIVIDUAL).build();
        CUBIC_TILES.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.CUBIC).build();
        
        CUBIC_BIGTEX.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.BIG_TEX).build();
        CUBIC_BIGTEX.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.CUBIC).build();
        
        CUBIC_BORDER.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.BORDERS).build();
        CUBIC_BORDER.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.CUBIC).build();
        
        CUBIC_MASONRY.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.MASONRY).build();
        CUBIC_MASONRY.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.CUBIC).build();
        
        SURFACE_TILES.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.BLOCK_INDIVIDUAL, TextureProviders.BIG_TEX).build();
        SURFACE_TILES.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.TILED).build();
        
        SURFACE_CYLINDER.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.BLOCK_INDIVIDUAL, TextureProviders.BIG_TEX).build();
        SURFACE_CYLINDER.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.CYLINDRICAL).build();
        
        SURFACE_TOROID.textureProviders = new ImmutableList.Builder<TextureProvider>()
                .add(TextureProviders.BLOCK_INDIVIDUAL, TextureProviders.BIG_TEX).build();
        SURFACE_TOROID.surfaceTopologies = new ImmutableList.Builder<SurfaceTopology>()
                .add(SurfaceTopology.TOROIDAL).build();}
    
    public final int stateFlags;
    
    public final QuadPainterFactory quadPainterFactory;
    
    private SurfacePainter(int stateFlags, QuadPainterFactory quadPainterFactory)
    {
        this.stateFlags = stateFlags;
        this.quadPainterFactory = quadPainterFactory;
    }
    
    private List<TextureProvider> textureProviders;
    public List<TextureProvider> textureProviders() { return this.textureProviders; }
    
    private List<SurfaceTopology> surfaceTopologies;
    public List<SurfaceTopology> surfaceTopologies() { return this.surfaceTopologies; }
}
