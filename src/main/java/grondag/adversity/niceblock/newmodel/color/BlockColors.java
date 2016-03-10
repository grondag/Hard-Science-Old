package grondag.adversity.niceblock.newmodel.color;

import java.util.ArrayList;

import grondag.adversity.Adversity;
import grondag.adversity.library.Color;
import grondag.adversity.library.Color.EnumHCLFailureMode;
import grondag.adversity.niceblock.newmodel.color.ColorMap.EnumColorMap;
import grondag.adversity.niceblock.newmodel.color.HueSet.HuePosition;
import grondag.adversity.niceblock.newmodel.color.HueSet.Tint;
import grondag.adversity.niceblock.newmodel.color.NiceHues.Hue;

public class BlockColors implements IColorProvider
{
    public static final BlockColors INSTANCE = new BlockColors();
    
    private final int COLOR_COUNT = Hue.values().length * 25;
    private final ColorMap[] COLORS = new ColorMap[COLOR_COUNT];

    public static ColorMap makeColorMap(Hue hue, Tint tint)
    {
        String mapName =  tint.tintName + " " + hue.hueName();
                
        ColorMap newColorMap = new ColorMap(mapName);

        Color baseColor = Color.fromHCL(hue.hueDegrees(), tint.chroma, tint.luminance);

        newColorMap.setColor(EnumColorMap.BASE, baseColor.RGB_int | 0xFF000000);

        // use these for manipulation so can use realistic values for HCL_MAX inputs
        double chroma = baseColor.HCL_C;
        double luminance = baseColor.HCL_L;

        // BORDERS
        Color whichColor = Color.fromHCL(hue.hueDegrees() + 15,
                chroma < 10 ? chroma + 10 : chroma * 0.5,
                luminance < 60 ? luminance + 15 : luminance - 15,
                EnumHCLFailureMode.REDUCE_CHROMA);
        if(!whichColor.IS_VISIBLE)
        {
            Adversity.log.debug("makeColorMap produced invisible border color for hue=" + hue + " tint=" + tint);
        }
        newColorMap.setColor(EnumColorMap.BORDER, whichColor.RGB_int | 0xFF000000);

        newColorMap.setColor(EnumColorMap.HIGHLIGHT,
                NiceHues.INSTANCE.getHueSet(hue).getColorSetForHue(HuePosition.OPPOSITE).getColor(tint) | 0xFF000000);
        
        Color lampColor = Color.fromHCL(hue.hueDegrees(), tint.chroma, Color.HCL_MAX);
        newColorMap.setColor(EnumColorMap.LAMP, lampColor.RGB_int | 0xFF000000);

        return newColorMap;
    }

    protected BlockColors()
    {
        int i=0;

        for(Hue hue: Hue.values())
        {
            COLORS[i++] = makeColorMap(hue, Tint.WHITE);
            COLORS[i++] = makeColorMap(hue, Tint.GREY_BRIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.GREY_LIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.GREY_MID);
            COLORS[i++] = makeColorMap(hue, Tint.GREY_DEEP);
            COLORS[i++] = makeColorMap(hue, Tint.GREY_DARK);
    
            COLORS[i++] = makeColorMap(hue, Tint.NEUTRAL_BRIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.NEUTRAL_LIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.NEUTRAL_MID);
            COLORS[i++] = makeColorMap(hue, Tint.NEUTRAL_DEEP);
            COLORS[i++] = makeColorMap(hue, Tint.NEUTRAL_DARK);
 
            COLORS[i++] = makeColorMap(hue, Tint.RICH_BRIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.RICH_LIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.RICH_MID);
            COLORS[i++] = makeColorMap(hue, Tint.RICH_DEEP);
            COLORS[i++] = makeColorMap(hue, Tint.RICH_DARK);

            COLORS[i++] = makeColorMap(hue, Tint.BOLD_BRIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.BOLD_LIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.BOLD_MID);
            COLORS[i++] = makeColorMap(hue, Tint.BOLD_DEEP);
            COLORS[i++] = makeColorMap(hue, Tint.BOLD_DARK);

            COLORS[i++] = makeColorMap(hue, Tint.ACCENT_LIGHT);
            COLORS[i++] = makeColorMap(hue, Tint.ACCENT_MID);
            COLORS[i++] = makeColorMap(hue, Tint.ACCENT_DEEP);
            
            COLORS[i++] = makeColorMap(hue, Tint.ACCENT_ULTRA);
        }
    }
  
    
    @Override
    public int getColorCount()
    {
        return COLOR_COUNT;
    }

    @Override
    public ColorMap getColor(int colorIndex)
    {
        return COLORS[Math.max(0, Math.min(COLOR_COUNT-1, colorIndex))];
    }
}
