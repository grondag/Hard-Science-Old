package grondag.adversity.superblock.model.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelStateProperty implements IUnlistedProperty<ModelStateFactory.ModelState> {

    @Override
    public String getName() {
        return "ModelState";
    }

    @Override
    public boolean isValid(ModelStateFactory.ModelState value)
    {
        return true;
    }

    @Override
    public String valueToString(ModelStateFactory.ModelState value)
    {
        return value.toString();
    }

    @Override
    public Class<ModelStateFactory.ModelState> getType()
    {
        return ModelStateFactory.ModelState.class;
    }
    

}
