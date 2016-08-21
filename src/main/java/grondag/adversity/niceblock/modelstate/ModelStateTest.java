package grondag.adversity.niceblock.modelstate;

import grondag.adversity.niceblock.modelstate.ModelStateSet.ModelStateSetValue;
import net.minecraft.util.EnumFacing;


public class ModelStateTest
{

    public static void testDongle()
    {
        ModelStateGroup g1 = ModelStateGroup.find(ModelStateComponents.AXIS_DYNAMIC, ModelStateComponents.CORNER_JOIN_DYNAMIC);
        ModelStateGroup g2 = ModelStateGroup.find(ModelStateComponents.ROTATION_INNER_YES, ModelStateComponents.TEXTURE_INNER_4);
        ModelStateSet set = ModelStateSet.find(g1, g2);
        ModelStateSetValue value = set.getValue(ModelStateComponents.AXIS_DYNAMIC.fromEnum(EnumFacing.Axis.X));
        EnumFacing.Axis axis = value.getValue(ModelStateComponents.AXIS_DYNAMIC);
        long subKey = value.getGroupKey(g1);
    }
}
