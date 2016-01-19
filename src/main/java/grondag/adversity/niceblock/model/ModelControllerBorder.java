package grondag.adversity.niceblock.model;

import grondag.adversity.niceblock.NiceSubstance;
import net.minecraft.util.EnumWorldBlockLayer;

public class ModelControllerBorder extends ModelController {

	protected ModelControllerBorder(int textureIndex, int alternateCount, EnumWorldBlockLayer renderLayer, boolean isShaded, boolean useRotations) {
		super(textureIndex, alternateCount, false, renderLayer, isShaded, useRotations);
	}

	@Override
	public NiceModelBorder getModel(NiceSubstance substance) {
		// TODO Auto-generated method stub
		return null;
	}


}
