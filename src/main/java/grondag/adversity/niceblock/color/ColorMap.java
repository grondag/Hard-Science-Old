package grondag.adversity.niceblock.color;

import grondag.adversity.Output;
import grondag.adversity.library.Color;
import grondag.adversity.library.Color.EnumHCLFailureMode;
import grondag.adversity.niceblock.color.HueSet.Chroma;
import grondag.adversity.niceblock.color.HueSet.Luminance;
import grondag.adversity.niceblock.color.NiceHues.Hue;

public class ColorMap
{
    public final String colorMapName;
    public final int ordinal;
    public final Hue hue;
    public final Chroma chroma;
    public final Luminance luminance;
    
    private final int[] colors = new int[EnumColorMap.values().length];

    public ColorMap(String vectorName, Hue hue, Chroma chromaIn, Luminance luminanceIn, int ordinal)
    {
        this.colorMapName = vectorName;
        this.ordinal = ordinal;
        this.hue = hue;
        this.chroma = chromaIn;
        this.luminance = luminanceIn;
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
    
    public static ColorMap makeColorMap(Hue hue, Chroma chromaIn, Luminance luminanceIn, int ordinal)
    {
        String mapName =  luminanceIn.name + " " + chromaIn.name + " " + hue.hueName();
                
        ColorMap newColorMap = new ColorMap(mapName, hue, chromaIn, luminanceIn, ordinal);
    
        // use these for manipulation so can use realistic values for HCL_MAX inputs
        double chroma = chromaIn.value;
        double luminance = luminanceIn.value;

        Color baseColor = Color.fromHCL(hue.hueDegrees(), chroma, luminance, EnumHCLFailureMode.REDUCE_CHROMA);
    
        newColorMap.setColor(EnumColorMap.BASE, baseColor.RGB_int | 0xFF000000);
    
    
        // BORDERS
        Color whichColor = Color.fromHCL(hue.hueDegrees() + 15,
                chroma < 10 ? chroma + 10 : chroma * 0.5,
                luminance < 60 ? luminance + 15 : luminance - 15,
                EnumHCLFailureMode.REDUCE_CHROMA);
        if(!whichColor.IS_VISIBLE)
        {
            Output.getLog().debug("makeColorMap produced invisible border color for " + mapName);
        }
        newColorMap.setColor(EnumColorMap.BORDER, whichColor.RGB_int | 0xFF000000);
    
//        newColorMap.setColor(EnumColorMap.HIGHLIGHT,
//                NiceHues.INSTANCE.getHueSet(hue).getColorSetForHue(HuePosition.OPPOSITE).getColor(tint) | 0xFF000000);
        
        Color lampColor = Color.fromHCL(hue.hueDegrees(), Math.min(chromaIn.value * 0.65, 25), Color.HCL_MAX);
        if(lampColor.RGB_int == 0)
        {
            Output.getLog().info("whoops hcl" + hue.hueDegrees() + " " + chromaIn.value / 2 + " " + Color.HCL_MAX);
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
