package grondag.adversity.feature.volcano.lava;

import java.util.Collection;

import net.minecraft.util.math.BlockPos;

public class BarrierCell extends FluidCell
{
    public static final BarrierCell INSTANCE = new BarrierCell();

    public BarrierCell()
    {
        super(null, null);
    }

    @Override
    public boolean isCellOnGround(FluidTracker tracker)
    {
        return false;
    }

    @Override
    public void doStep(FluidTracker tracker, double seconds)
    {
        //NOOP
    }

    @Override
    public boolean canAcceptFluidDirectly(FluidTracker tracker)
    {
        return false;
    }

    @Override
    public void changeLevel(FluidTracker tracker, float amount)
    {
        //NOOP
    }

    @Override
    public void applyUpdates(FluidTracker tracker)
    {
        //NOOP
    }

    @Override
    public void provideBlockUpdate(FluidTracker tracker, Collection<LavaBlockUpdate> updateList)
    {
        //NOOP
    }

    @Override
    public float getCurrentLevel()
    {
        return Float.MAX_VALUE;
    }

    @Override
    public float getDelta()
    {
        return 0;
    }

    @Override
    public void delete(FluidTracker tracker)
    {
        //NOOP
    }
}
