package grondag.hard_science.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum Layout
{
    /** use the given dimension exactly */
    FIXED,
    /** dimension represents weight for allocating variable space */
    WEIGHTED,
    /** scale dimension to other orthogonalAxis according to aspect ratio */
    PROPORTIONAL
}
