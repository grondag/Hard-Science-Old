package grondag.hard_science.library.varia;

import static grondag.hard_science.library.varia.VerticalAlignment.*;
import static grondag.hard_science.library.varia.HorizontalAlignment.*;


public enum Alignment
{
    TOP_LEFT(TOP, LEFT),
    TOP_CENTER(TOP, CENTER),
    TOP_RIGHT(TOP, RIGHT),
    
    MIDDLE_LEFT(MIDDLE, LEFT),
    MIDDLE_CENTER(MIDDLE, CENTER),
    MIDDLE_RIGHT(MIDDLE, RIGHT),
    
    BOTTOM_LEFT(BOTTOM, LEFT),
    BOTTOM_CENTER(BOTTOM, CENTER),
    BOTTOM_RIGHT(BOTTOM, RIGHT);
    
    
    public final VerticalAlignment vAlign;
    public final HorizontalAlignment hAlign;
    
    private Alignment(VerticalAlignment vAlign, HorizontalAlignment hAlign)
    {
        this.vAlign = vAlign;
        this.hAlign = hAlign;
    }
}
