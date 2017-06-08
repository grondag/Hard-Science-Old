package grondag.adversity.gui.shape;

import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public class GuiSimpleShape extends GuiShape
{
    public GuiSimpleShape(boolean isVertical)
    {
        super(isVertical);
    }

    @Override
    public void loadSettings(ModelState modelState)
    {
        //ignore
    }

    @Override
    public boolean saveSettings(ModelState modelState)
    {
        return false;
    }

}
