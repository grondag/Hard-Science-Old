package grondag.adversity.superblock.collision;

import java.util.List;

import grondag.adversity.library.cache.ObjectSimpleCacheLoader;
import grondag.adversity.library.cache.ObjectSimpleLoadingCache;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.math.AxisAlignedBB;

public class CollisionBoxDispatcher
{
    public static final CollisionBoxDispatcher INSTANCE = new CollisionBoxDispatcher();
    
    private final ObjectSimpleLoadingCache<ModelState, List<AxisAlignedBB>> modelBounds = new ObjectSimpleLoadingCache<ModelState, List<AxisAlignedBB>>(new CollisionBoxLoader(),  0xFFF);

    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return this.modelBounds.get(modelState.geometricState());
    }
    
    private static class CollisionBoxLoader implements ObjectSimpleCacheLoader<ModelState, List<AxisAlignedBB>>
    {
        @Override
        public List<AxisAlignedBB> load(ModelState key)
        {
            return CollisionBoxGenerator.makeCollisionBoxList(key.getShape().meshFactory().getShapeQuads(key));
        }
    }
}
