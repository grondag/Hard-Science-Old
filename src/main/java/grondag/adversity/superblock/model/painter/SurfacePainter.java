package grondag.adversity.superblock.model.painter;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.library.model.quadfactory.RawQuad;
import grondag.adversity.niceblock.model.texture.TextureProvider;
import grondag.adversity.niceblock.model.texture.TextureProviders;
import grondag.adversity.superblock.model.painter.surface.SurfaceTopology;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

import static grondag.adversity.superblock.model.state.ModelStateFactory.ModelState.*;

public enum SurfacePainter
{
    NONE(STATE_FLAG_NONE, QuadPainter::nullQuadPainter),
    CUBIC_TILES(STATE_FLAG_NEEDS_BLOCK_RANDOMS, CubicQuadPainters::paintCubicTiles),
    CUBIC_BIGTEX(STATE_FLAG_NEEDS_POS, CubicQuadPainters::paintCubicBigTex),
    CUBIC_BORDER(STATE_FLAG_NEEDS_CORNER_JOIN, CubicQuadPainters::paintCubicBorder),
    CUBIC_MASONRY(STATE_FLAG_NEEDS_SIMPLE_JOIN, CubicQuadPainters::paintCubicMasonry),
    SURFACE_TILES(STATE_FLAG_NONE, SurfaceQuadPainters::paintSurfaceTiles),
    SURFACE_CYLINDER(STATE_FLAG_NONE, SurfaceQuadPainters::paintSurfaceCylinder),
    SURFACE_TOROID(STATE_FLAG_NONE, SurfaceQuadPainters::paintSurfaceTorus);
    
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
    
    private final QuadPainter quadPainter;
    
    private SurfacePainter(int stateFlags, QuadPainter quadPainter)
    {
        this.stateFlags = stateFlags;
        this.quadPainter = quadPainter;
    }
    
    private List<TextureProvider> textureProviders;
    public List<TextureProvider> textureProviders() { return this.textureProviders; }
    
    private List<SurfaceTopology> surfaceTopologies;
    public List<SurfaceTopology> surfaceTopologies() { return this.surfaceTopologies; }
    
    public void addPaintedQuadsToList(ModelState modelState, int painterIndex, Collection<RawQuad> shapeQuads, List<RawQuad> outputQuads)
    {
        quadPainter.addPaintedQuadsToList(modelState, painterIndex, shapeQuads, outputQuads);
    }

}
