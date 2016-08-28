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
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.WHITE, i++);
            
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.GREY_BRIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.GREY_LIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.GREY_MID, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.GREY_DEEP, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.GREY_DARK, i++);
    
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_BRIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_LIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_MID, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_DEEP, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.NEUTRAL_DARK, i++);
 
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.RICH_BRIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.RICH_LIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.RICH_MID, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.RICH_DEEP, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.RICH_DARK, i++);

            COLORS[i] = ColorMap.makeColorMap(hue, Tint.BOLD_BRIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.BOLD_LIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.BOLD_MID, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.BOLD_DEEP, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.BOLD_DARK, i++);

            COLORS[i] = ColorMap.makeColorMap(hue, Tint.ACCENT_LIGHT, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.ACCENT_MID, i++);
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.ACCENT_DEEP, i++);
            
            COLORS[i] = ColorMap.makeColorMap(hue, Tint.ACCENT_ULTRA, i++);
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
