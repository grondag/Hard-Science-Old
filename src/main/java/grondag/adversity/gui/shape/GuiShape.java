package grondag.adversity.gui.shape;

import grondag.adversity.gui.control.Panel;
import grondag.adversity.superblock.model.state.ModelStateFactory.ModelState;

public abstract class GuiShape extends Panel
{

    public GuiShape(boolean isVertical)
    {
        super(isVertical);
    }
    
    /** called before control is displayed and whenever modelstate changes */
    public abstract void loadSettings(ModelState modelState);
    
    /** called to detect user changes - return true if model state was changed */
    public abstract boolean saveSettings(ModelState modelState);

}
