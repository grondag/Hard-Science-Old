package grondag.adversity.superblock.model.shape;

import java.util.List;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;
import net.minecraft.util.math.AxisAlignedBB;

public interface ICollisionHandler
{
    public List<AxisAlignedBB> getCollisionBoxes(ModelState modelState);
    
    public AxisAlignedBB getCollisionBoundingBox(ModelState modelState);
    
    public AxisAlignedBB getRenderBoundingBox(ModelState modelState);
}
