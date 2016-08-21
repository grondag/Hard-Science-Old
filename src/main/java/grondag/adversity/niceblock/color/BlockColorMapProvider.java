package grondag.adversity.niceblock.color;

import grondag.adversity.niceblock.color.HueSet.Tint;
import grondag.adversity.niceblock.color.NiceHues.Hue;

public class BlockColorMapProvider implements IColorMapProvider
{
    public static final BlockColorMapProvider INSTANCE = new BlockColorMapProvider();
    
    private final int COLOR_COUNT = Hue.values().length * 25;
    private final ColorMap[] COLORS = new ColorMap[COLOR_COUNT];

    protected BlockColorMapProvider()
    {
        int i=0;

        for(Hue hue: Hue.values())
        {
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.WHITE);
            
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.GREY_BRIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.GREY_LIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.GREY_MID);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.GREY_DEEP);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.GREY_DARK);
    
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_BRIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_LIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_MID);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_DEEP);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_DARK);
 
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.RICH_BRIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.RICH_LIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.RICH_MID);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.RICH_DEEP);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.RICH_DARK);

            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.BOLD_BRIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.BOLD_LIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.BOLD_MID);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.BOLD_DEEP);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.BOLD_DARK);

            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.ACCENT_LIGHT);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.ACCENT_MID);
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.ACCENT_DEEP);
            
            COLORS[i++] = ColorMap.makeColorMap(hue, Tint.ACCENT_ULTRA);
        }
    }
  
    
    @Override
    public int getColorMapCount()
    {
        return COLOR_COUNT;
    }

    @Override
    public ColorMap getColorMap(int colorIndex)
    {
        return COLORS[Math.max(0, Math.min(COLOR_COUNT-1, colorIndex))];
    }
}
