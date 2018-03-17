package grondag.hard_science.gui.shape;

import grondag.hard_science.superblock.model.state.ModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
