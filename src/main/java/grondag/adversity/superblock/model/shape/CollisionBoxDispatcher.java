package grondag.adversity.superblock.model.shape;

import java.util.List;

import grondag.adversity.library.cache.ObjectSimpleCacheLoader;
import grondag.adversity.library.cache.ObjectSimpleLoadingCache;
import grondag.adversity.library.varia.Useful;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import grondag.adversity.superblock.varia.CollisionBoxGenerator;
import net.minecraft.util.math.AxisAlignedBB;

public class CollisionBoxDispatcher
{
    public static final CollisionBoxDispatcher INSTANCE = new CollisionBoxDispatcher();
    
    private final ObjectSimpleLoadingCache<CollisionKey, List<AxisAlignedBB>> modelBounds = new ObjectSimpleLoadingCache<CollisionKey, List<AxisAlignedBB>>(new CollisionBoxLoader(),  0xFFF);

    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState)
    {
        return this.modelBounds.get(new CollisionKey(modelState));
    }
    
    private static class CollisionKey
    {
        private final ModelState modelState;
        private final long collisionKey;
        private final int hash;
        
        private CollisionKey(ModelState modelState)
        {
            this.modelState = modelState;
            this.collisionKey = modelState.getShape().meshFactory().collisionHandler().collisionKey(modelState);
            this.hash = (int) Useful.longHash(this.collisionKey ^ modelState.getShape().ordinal());
        }
        
        @Override 
        public boolean equals(Object other)
        {
            if(other == null) return false;
            
            if(other instanceof CollisionKey)
            {
                CollisionKey otherSpec = (CollisionKey)other;
                return otherSpec.collisionKey == this.collisionKey
                        && otherSpec.modelState.getShape() == this.modelState.getShape();
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.hash;
        }
    }
    
    private static class CollisionBoxLoader implements ObjectSimpleCacheLoader<CollisionKey, List<AxisAlignedBB>>
    {
        @Override
        public List<AxisAlignedBB> load(CollisionKey key)
        {
            return CollisionBoxGenerator.makeCollisionBoxList(key.modelState.getShape().meshFactory().getShapeQuads(key.modelState));
        }
    }
}
