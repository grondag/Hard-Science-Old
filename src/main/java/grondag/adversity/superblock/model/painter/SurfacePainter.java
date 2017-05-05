package grondag.adversity.superblock.model.painter;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.adversity.niceblock.model.texture.TextureProvider;
import grondag.adversity.niceblock.model.texture.TextureProviders;
import grondag.adversity.superblock.model.shape.SurfaceTopology;

public enum SurfacePainter
{
    NONE,
    CUBIC_TILES,
    CUBIC_BIGTEX,
    CUBIC_BORDER,
    CUBIC_MASONRY,
    SURFACE_TILES,
    SURFACE_CYLINDER,
    SURFACE_TOROID;
    
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
    
    private List<TextureProvider> textureProviders;
    public List<TextureProvider> textureProviders() { return this.textureProviders; }
    
    private List<SurfaceTopology> surfaceTopologies;
    public List<SurfaceTopology> surfaceTopologies() { return this.surfaceTopologies; }

}
