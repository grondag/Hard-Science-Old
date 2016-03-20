package grondag.adversity.niceblock.newmodel;

import net.minecraft.util.EnumWorldBlockLayer;

public class ColumnSquareController extends AxisOrientedController
{
    
    public final int cutCount;
    public final boolean areCutsOnEdge;
    public final ModelType modelType;
    
    public ColumnSquareController(String textureName, int alternateTextureCount, ModelType modelType, boolean isShaded, int cutCount, boolean areCutsOnEdge)
    {
        super(textureName, alternateTextureCount, modelType, isShaded);
        this.textureCount = 1;
        this.cutCount = cutCount;
        this.areCutsOnEdge = areCutsOnEdge;
        this.modelType = modelType;
        // important that this be called AFTER attributes used by model are initialized
        this.bakedModelFactory = new ColumnSquareModelFactory(this);
    }


}
