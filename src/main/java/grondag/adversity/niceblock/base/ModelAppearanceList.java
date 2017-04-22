package grondag.adversity.niceblock.base;

import java.util.ArrayList;

import grondag.adversity.library.model.quadfactory.LightingMode;
import grondag.adversity.niceblock.modelstate.ModelColorMapComponent;
import net.minecraft.util.BlockRenderLayer;

public class ModelAppearanceList
{
    private final ArrayList<ModelAppearance> list = new ArrayList<ModelAppearance>();
    
    private int maxTexturesVersions = 1;
    
    public final LightingMode lightingMode;
    public final BlockRenderLayer renderLayer;
    public final ModelColorMapComponent colorMap;
    
    public ModelAppearanceList(LightingMode lightingMode, BlockRenderLayer renderLayer, ModelColorMapComponent colorMap)
    {
        this.lightingMode = lightingMode;
        this.renderLayer = renderLayer;
        this.colorMap = colorMap;
    }
    
    public int maxAltVersions() { return this.maxTexturesVersions; }
    public int size() { return list.size(); }
    
    public void add(ModelAppearance appearance)
    {
        list.add(appearance);
        maxTexturesVersions = Math.max(maxTexturesVersions, appearance.textureVersionCount);
    }
    
    public ModelAppearance get(int index)
    {
        return list.get(index);
    }
}
