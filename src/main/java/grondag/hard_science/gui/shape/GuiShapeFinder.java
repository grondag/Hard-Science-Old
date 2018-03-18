package grondag.hard_science.gui.shape;

import grondag.hard_science.movetogether.ModShapes;
import grondag.hard_science.movetogether.ModelShape;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiShapeFinder
{
    public static GuiShape findGuiForShape(ModelShape<?> shape, Minecraft mc)
    {
        if(shape == ModShapes.COLUMN_SQUARE)
            return new GuiSquareColumn(mc);
        else if(shape == ModShapes.STACKED_PLATES)
            return new GuiStackedPlates(mc);
        else
            return new GuiSimpleShape(true);
    }
}
