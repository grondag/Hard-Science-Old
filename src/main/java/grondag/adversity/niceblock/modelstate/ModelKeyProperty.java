package grondag.adversity.niceblock.modelstate;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelKeyProperty implements IUnlistedProperty<Long> {

	@Override
	public String getName() {
		return "ModelKey";
	}

    @Override
    public boolean isValid(Long value)
    {
        return true;
    }

    @Override
    public String valueToString(Long value)
    {
        return value.toString();
    }

    @Override
    public Class<Long> getType()
    {
        return Long.class;
    }
	

}
