package grondag.hard_science.gui.shape;

import grondag.hard_science.superblock.model.shape.ModelShape;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiShapeFinder
{
    public static GuiShape findGuiForShape(ModelShape shape, Minecraft mc)
    {
        switch(shape)
        {
        case COLUMN_SQUARE:
            return new GuiSquareColumn(mc);
        case STACKED_PLATES:
            return new GuiStackedPlates(mc);
        default:
            return new GuiSimpleShape(true);
        
        }
    }
}
