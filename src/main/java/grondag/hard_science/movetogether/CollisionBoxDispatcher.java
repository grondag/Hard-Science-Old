package grondag.hard_science.movetogether;

import java.util.List;

import grondag.exotic_matter.cache.ObjectSimpleCacheLoader;
import grondag.exotic_matter.cache.ObjectSimpleLoadingCache;
import net.minecraft.util.math.AxisAlignedBB;

public class CollisionBoxDispatcher
{
    public static final CollisionBoxDispatcher INSTANCE = new CollisionBoxDispatcher();
    
    private final ObjectSimpleLoadingCache<ISuperModelState, List<AxisAlignedBB>> modelBounds = new ObjectSimpleLoadingCache<ISuperModelState, List<AxisAlignedBB>>(new CollisionBoxLoader(),  0xFFF);

    public List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState)
    {
        return this.modelBounds.get(modelState.geometricState());
    }
    
    private static class CollisionBoxLoader implements ObjectSimpleCacheLoader<ISuperModelState, List<AxisAlignedBB>>
    {
        @Override
        public List<AxisAlignedBB> load(ISuperModelState key)
        {
            return CollisionBoxGenerator.makeCollisionBoxList(key.getShape().meshFactory().getShapeQuads(key));
        }
    }
}
