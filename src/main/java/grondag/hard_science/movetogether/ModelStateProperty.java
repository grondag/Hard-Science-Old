package grondag.hard_science.movetogether;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelStateProperty implements IUnlistedProperty<ISuperModelState> {

    @Override
    public String getName() {
        return "ModelState";
    }

    @Override
    public boolean isValid(ISuperModelState value)
    {
        return true;
    }

    @Override
    public String valueToString(ISuperModelState value)
    {
        return value.toString();
    }

    @Override
    public Class<ISuperModelState> getType()
    {
        return ISuperModelState.class;
    }
    

}
