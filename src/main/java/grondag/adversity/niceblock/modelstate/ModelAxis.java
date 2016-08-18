package grondag.adversity.niceblock.modelstate;

import net.minecraft.util.EnumFacing;

public enum ModelAxis implements IModelStateComponent<ModelAxis>
{
    X(EnumFacing.Axis.X),
    Y(EnumFacing.Axis.Y),
    Z(EnumFacing.Axis.Z);
    
    private final EnumFacing.Axis axis;

    private ModelAxis(EnumFacing.Axis axis)
    {
        this.axis = axis;
    }
    
    public EnumFacing.Axis getAxis() { return this.axis; }

    @Override
    public ModelStateComponentType getComponentType()
    {
        return ModelStateComponentType.AXIS;
    }

    @Override
    public long getBits()
    {
        return this.ordinal();
    }
}