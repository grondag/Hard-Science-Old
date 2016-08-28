package grondag.adversity.niceblock.color;

import grondag.adversity.Adversity;
import grondag.adversity.library.Color;
import grondag.adversity.library.Color.EnumHCLFailureMode;
import grondag.adversity.niceblock.color.HueSet.HuePosition;
import grondag.adversity.niceblock.color.HueSet.Tint;
import grondag.adversity.niceblock.color.NiceHues.Hue;

public class ColorMap
{
    public final String colorMapName;
    public final int ordinal;
    private final int[] colors = new int[EnumColorMap.values().length];

    public ColorMap(String vectorName, int ordinal)
    {
        this.colorMapName = vectorName;
        this.ordinal = ordinal;
    }

    public ColorMap setColor(EnumColorMap whichColor, int colorValue)
    {
        colors[whichColor.ordinal()] = colorValue;
        return this;
    }
    
    public int getColor(EnumColorMap whichColor)
    {
        return colors[whichColor.ordinal()];
    }
    
    public static ColorMap makeColorMap(Hue hue, Tint tint, int ordinal)
    {
        String mapName =  tint.tintName + " " + hue.hueName();
                
        ColorMap newColorMap = new ColorMap(mapName, ordinal);
    
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
        
        Color lampColor = Color.fromHCL(hue.hueDegrees(), Math.min(tint.chroma * 0.65, 25), Color.HCL_MAX);
        if(lampColor.RGB_int == 0)
        {
            Adversity.log.info("whoops hcl" + hue.hueDegrees() + " " + tint.chroma / 2 + " " + Color.HCL_MAX);
        }
        newColorMap.setColor(EnumColorMap.LAMP, lampColor.RGB_int | 0xFF000000);
    
        return newColorMap;
    }

    public static enum EnumColorMap
    {
        BASE,
        HIGHLIGHT,
        BORDER,
        LAMP
    }
}
