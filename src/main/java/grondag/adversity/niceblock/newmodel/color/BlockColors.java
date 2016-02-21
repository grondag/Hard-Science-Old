package grondag.adversity.niceblock.newmodel.color;

import java.util.ArrayList;

import grondag.adversity.niceblock.newmodel.color.HueSet.HuePosition;
import grondag.adversity.niceblock.newmodel.color.HueSet.Tint;
import grondag.adversity.niceblock.newmodel.color.NiceHues.Hue;

public class BlockColors implements IColorProvider
{
    public static final BlockColors INSTANCE = new BlockColors();
    
    private final int COLOR_COUNT = Hue.values().length * 25;
    private final ColorVector[] COLORS = new ColorVector[COLOR_COUNT];

    public static ColorVector makeColorVector(Hue hue, Tint tint)
    {
        String vectorName =  tint.tintName + " " + hue.hueName();
                
        return new ColorVector(
                vectorName,
                NiceHues.INSTANCE.getHueSet(hue).getColorSetForHue(HuePosition.NONE).getColor(tint) | 0xFF000000,
                NiceHues.INSTANCE.getHueSet(hue).getColorSetForHue(HuePosition.FAR_LEFT).getColor(tint) | 0xFF000000,
                NiceHues.INSTANCE.getHueSet(hue).getColorSetForHue(HuePosition.NEAR_RIGHT).getColor(tint) | 0xFF000000);
    }

    protected BlockColors()
    {
        int i=0;

        for(Hue hue: Hue.values())
        {
            COLORS[i++] = makeColorVector(hue, Tint.WHITE);
            COLORS[i++] = makeColorVector(hue, Tint.GREY_BRIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.GREY_LIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.GREY_MID);
            COLORS[i++] = makeColorVector(hue, Tint.GREY_DEEP);
            COLORS[i++] = makeColorVector(hue, Tint.GREY_DARK);
    
            COLORS[i++] = makeColorVector(hue, Tint.NEUTRAL_BRIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.NEUTRAL_LIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.NEUTRAL_MID);
            COLORS[i++] = makeColorVector(hue, Tint.NEUTRAL_DEEP);
            COLORS[i++] = makeColorVector(hue, Tint.NEUTRAL_DARK);
 
            COLORS[i++] = makeColorVector(hue, Tint.RICH_BRIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.RICH_LIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.RICH_MID);
            COLORS[i++] = makeColorVector(hue, Tint.RICH_DEEP);
            COLORS[i++] = makeColorVector(hue, Tint.RICH_DARK);

            COLORS[i++] = makeColorVector(hue, Tint.BOLD_BRIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.BOLD_LIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.BOLD_MID);
            COLORS[i++] = makeColorVector(hue, Tint.BOLD_DEEP);
            COLORS[i++] = makeColorVector(hue, Tint.BOLD_DARK);

            COLORS[i++] = makeColorVector(hue, Tint.ACCENT_LIGHT);
            COLORS[i++] = makeColorVector(hue, Tint.ACCENT_MID);
            COLORS[i++] = makeColorVector(hue, Tint.ACCENT_DEEP);
            
            COLORS[i++] = makeColorVector(hue, Tint.ACCENT_ULTRA);
        }
    }
  
    
    @Override
    public int getColorCount()
    {
        return COLOR_COUNT;
    }

    @Override
    public ColorVector getColor(int colorIndex)
    {
        return COLORS[Math.max(0, Math.min(COLOR_COUNT-1, colorIndex))];
    }
}
