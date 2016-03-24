package grondag.adversity.niceblock.base;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelStateProperty implements IUnlistedProperty<ModelState> {

	@Override
	public String getName() {
		return "ModelState";
	}

	@Override
	public boolean isValid(ModelState value) {
		return true;
	}

	@Override
	public Class<ModelState> getType() {
		return ModelState.class;
	}

	@Override
	public String valueToString(ModelState value) {
		return value.toString();
	}
	

}
