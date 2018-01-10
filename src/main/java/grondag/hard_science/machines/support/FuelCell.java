package grondag.hard_science.machines.support;

public abstract class FuelCell extends AbstractPowerComponent
{
    @Override
    public EnergyComponentType componentType()
    {
        return EnergyComponentType.GENERATOR;
    }

}