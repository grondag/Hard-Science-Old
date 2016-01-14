package grondag.adversity.niceblock.model;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelRenderProperty implements IUnlistedProperty<ModelRenderData> {

	@Override
	public String getName() {
		return "ModelRenderProps";
	}

	@Override
	public boolean isValid(ModelRenderData value) {
		return true;
	}

	@Override
	public Class getType() {
		return ModelRenderData.class;
	}

	@Override
	public String valueToString(ModelRenderData value) {
		return value.toString();
	}
	

}
