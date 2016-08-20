package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import net.minecraft.util.EnumFacing;


public class ModelStateTest
{

    public static void testDongle()
    {
        ModelStateSetValue value = ModelStateSet.TEST.getSetValue(ModelAxis.X);
        EnumFacing.Axis axis = value.getValue(ModelStateComponents.AXIS);
        long subKey = value.getGroupKey(ModelStateGroup.INNER_SHAREDCOLOR_TEX4_ROTATE);
    }
}
