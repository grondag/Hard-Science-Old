package grondag.hard_science.library.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.hard_science.Configurator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuadCache
{
    public static final QuadCache INSTANCE = new QuadCache();
    public final LoadingCache<CachedBakedQuad, CachedBakedQuad> cache; 

    public QuadCache()        
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .maximumSize(Configurator.RENDER.quadCacheSizeLimit)
                .initialCapacity(0xFFFF);

        if(Configurator.RENDER.enableQuadCacheStatistics)
        {
            builder = builder.recordStats();
        }

        this.cache = builder.build(new CacheLoader<CachedBakedQuad, CachedBakedQuad>()
        {

            @Override
            public CachedBakedQuad load(CachedBakedQuad key) throws Exception
            {
                return key;
            }
        });

    }

    public CachedBakedQuad getCachedQuad(CachedBakedQuad quadIn)
    {
        return this.cache.getUnchecked(quadIn);
    }



}
