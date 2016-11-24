package grondag.adversity.feature.volcano;

import java.util.HashMap;

import net.minecraft.util.math.BlockPos;

/**
 * Tracks lava origin blocks with purpose of knowing 
 * how far each has flowed and if is now flowing down.
 */
public class OriginTracker extends HashMap<BlockPos, OriginTracker.OriginInfo>
{

    private static final long serialVersionUID = 2363769115646251688L;

    public static class OriginInfo
    {
        private double maxDistanceSquared;
        private boolean isFlowingDown;
        
        public OriginInfo(double maxDistanceSquared, boolean isFlowingDown)
        {
            this.setMaxDistanceSquared(maxDistanceSquared);
            this.setFlowingDown(isFlowingDown);
        }

        public double getMaxDistanceSquared()
        {
            return maxDistanceSquared;
        }

        public void setMaxDistanceSquared(double maxDistanceSquared)
        {
            this.maxDistanceSquared = maxDistanceSquared;
        }

        public boolean isFlowingDown()
        {
            return isFlowingDown;
        }

        public void setFlowingDown(boolean isFlowingDown)
        {
            this.isFlowingDown = isFlowingDown;
        }
        
        
    }
}
