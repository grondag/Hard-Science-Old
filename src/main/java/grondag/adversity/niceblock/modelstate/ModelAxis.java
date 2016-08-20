package grondag.adversity.niceblock.modelstate;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class ModelAxis implements IModelStateValue<ModelAxis, EnumFacing.Axis>
{
    public static final ModelAxis X = new ModelAxis(EnumFacing.Axis.X);
    public static final ModelAxis Y = new ModelAxis(EnumFacing.Axis.Y);
    public static final ModelAxis Z = new ModelAxis(EnumFacing.Axis.Z);
    private static final ModelAxis[] LOOKUP = {X, Y, Z};
    
    public static ModelAxis fromEnum(EnumFacing.Axis axis)
    {
        return LOOKUP[axis.ordinal()];
    }
    
    private final Axis axis;

    ModelAxis(EnumFacing.Axis axis)
    {
        this.axis = axis;
    }

    @Override
    public long getBits()
    {
        return axis.ordinal();
    }

    @Override
    public Axis getValue()
    {
        return this.axis;
    }

    @Override
    public ModelStateComponent<ModelAxis, Axis> getComponentType()
    {
        return ModelStateComponents.AXIS;
    }
}