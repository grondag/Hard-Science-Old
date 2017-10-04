package grondag.hard_science.machines.support;

public abstract class FuelCell extends AbstractPowerComponent
{
    @Override
    public PowerComponentType componentType()
    {
        return PowerComponentType.GENERATOR;
    }

}