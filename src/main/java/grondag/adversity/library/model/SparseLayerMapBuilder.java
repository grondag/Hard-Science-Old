package grondag.adversity.library.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.BlockRenderLayer;

/**
 * Super lightweight version of EnumMap for Block layers to QuadContainer. Only stores values for keys that are used.
 */
public class SparseLayerMapBuilder
{
    private final int[] layerIndices = new int[BlockRenderLayer.values().length];
    private final int size;
    public final ImmutableList<BlockRenderLayer> layerList;
    
    public SparseLayerMapBuilder(List<BlockRenderLayer> layers)
    {
        this.size = layers.size();
        this.layerList = ImmutableList.copyOf(layers);
        int counter = 0;
        
        for(BlockRenderLayer l: layers)
        {
            layerIndices[l.ordinal()] = counter++;
        }
    }
    
    public SparseLayerMap makeNewMap()
    {
        if(size == 1)
            return new SparseLayerSingletonMap();
        else 
            return new SparseLayerArrayMap();
    }
    
    public abstract class SparseLayerMap
    {
        public abstract QuadContainer get(BlockRenderLayer layer);
        
        public abstract void set(BlockRenderLayer layer, QuadContainer value);
        
    }
    
    private class SparseLayerArrayMap extends SparseLayerMap
    {
        private final QuadContainer[] values = new QuadContainer[size];
        
        private SparseLayerArrayMap()
        {
            //NOOP - just making it private
        }
        
        public QuadContainer get(BlockRenderLayer layer)
        {
            // block breaking can send us layers that don't exist for this model
            // in that case default to the first layer provided
            return values[layer == null ? 0 : layerIndices[layer.ordinal()]];
        }
        
        public void set(BlockRenderLayer layer, QuadContainer value)
        {
            values[layerIndices[layer.ordinal()]] = value;
        }
        
    }
     
    private class SparseLayerSingletonMap extends SparseLayerMap
    {
        private QuadContainer value;
        
        private SparseLayerSingletonMap()
        {
            //NOOP - just making it private
        }
        
        public QuadContainer get(BlockRenderLayer layer)
        {
            return value;
        }
        
        public void set(BlockRenderLayer layer, QuadContainer value)
        {
            this.value = value;
        }
    }
}
