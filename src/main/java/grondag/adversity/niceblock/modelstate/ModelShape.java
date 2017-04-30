package grondag.adversity.niceblock.modelstate;


import grondag.adversity.niceblock.model.CSGModelFactory;
import grondag.adversity.niceblock.model.FlowModelFactory;
import grondag.adversity.niceblock.model.HeightModelFactory;
import grondag.adversity.niceblock.support.AbstractCollisionHandler;

public enum ModelShape
{
    CUBE,
    COLUMN_SQUARE,
    FLOWING_TERRAIN,
    ICOSAHEDRON,
    HEIGHT;
    
    static
    {
        FLOWING_TERRAIN.collisionHandler = FlowModelFactory.makeCollisionHandler();
        ICOSAHEDRON.collisionHandler = CSGModelFactory.makeCollisionHandler();
        HEIGHT.collisionHandler = HeightModelFactory.makeCollisionHandler();
    }
    
    private AbstractCollisionHandler collisionHandler = null;
    
    /**
     * CAN BE NULL! If non-null, blocks with this shape require special collision handling, typically because it is not a standard cube shape. 
     */
    public AbstractCollisionHandler getCollisionHandler()
    {
        return collisionHandler;
    };
    
   
}
