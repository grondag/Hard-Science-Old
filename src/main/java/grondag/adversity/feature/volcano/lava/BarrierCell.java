package grondag.adversity.feature.volcano.lava;

import java.util.Collection;

import net.minecraft.util.math.BlockPos;

public class BarrierCell extends LavaSimCell
{
    public static final BarrierCell INSTANCE = new BarrierCell();

    public BarrierCell()
    {
        super(null, null);
    }

    @Override
    public boolean isCellOnGround(LavaSimulator tracker)
    {
        return false;
    }

    @Override
    public void doStep(LavaSimulator tracker, double seconds)
    {
        //NOOP
    }

    @Override
    public boolean canAcceptFluidDirectly(LavaSimulator tracker)
    {
        return false;
    }

    @Override
    public void changeLevel(LavaSimulator tracker, float amount)
    {
        //NOOP
    }

    @Override
    public void applyUpdates(LavaSimulator tracker)
    {
        //NOOP
    }

    @Override
    public void provideBlockUpdate(LavaSimulator tracker, Collection<LavaBlockUpdate> updateList)
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
    public void delete(LavaSimulator tracker)
    {
        //NOOP
    }
}
