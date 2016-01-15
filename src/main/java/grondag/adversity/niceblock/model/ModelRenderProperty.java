package grondag.adversity.niceblock.model;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelRenderProperty implements IUnlistedProperty<ModelRenderState> {

	@Override
	public String getName() {
		return "ModelRenderProps";
	}

	@Override
	public boolean isValid(ModelRenderState value) {
		return true;
	}

	@Override
	public Class getType() {
		return ModelRenderState.class;
	}

	@Override
	public String valueToString(ModelRenderState value) {
		return value.toString();
	}
	

}
