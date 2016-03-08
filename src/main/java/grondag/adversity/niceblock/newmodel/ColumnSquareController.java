package grondag.adversity.niceblock.newmodel;

import grondag.adversity.niceblock.model.ModelCookbookAxisOriented.AxisAlignedModel;
import net.minecraft.util.EnumWorldBlockLayer;

public class ColumnSquareController extends AxisOrientedController
{
    
    public final int cutCount;
    public final boolean areCutsOnEdge;
    
    public ColumnSquareController(String textureName, int alternateTextureCount, EnumWorldBlockLayer renderLayer, boolean isShaded, int cutCount, boolean areCutsOnEdge)
    {
        super(textureName, alternateTextureCount, renderLayer, isShaded);
        this.textureCount = 1;
        this.cutCount = cutCount;
        this.areCutsOnEdge = areCutsOnEdge;
        // important that this be called AFTER attributes used by model are initialized
        this.bakedModelFactory = new ColumnSquareModelFactory(this);
    }

}
