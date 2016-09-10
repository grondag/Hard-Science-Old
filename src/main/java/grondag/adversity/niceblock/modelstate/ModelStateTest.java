package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import net.minecraft.util.EnumFacing;


public class ModelStateTest
{

    @SuppressWarnings("unused")
    public static void testDongle()
    {
        ModelStateGroup g1 = ModelStateGroup.find(ModelStateComponents.AXIS, ModelStateComponents.CORNER_JOIN);
        ModelStateGroup g2 = ModelStateGroup.find(ModelStateComponents.ROTATION, ModelStateComponents.TEXTURE_4);
        ModelStateSet set = ModelStateSet.find(g1, g2);
        ModelStateSetValue value = set.getValue(ModelStateComponents.AXIS.fromEnum(EnumFacing.Axis.X));
        EnumFacing.Axis axis = value.getValue(ModelStateComponents.AXIS);
    }
}
