package grondag.adversity.gui;

public enum Layout
{
    /** use the given dimension exactly */
    FIXED,
    /** dimension represents weight for allocating variable space */
    WEIGHTED,
    /** scale dimension to other axis according to aspect ratio */
    PROPORTIONAL
}
