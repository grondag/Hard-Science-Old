package grondag.hard_science.superblock.color;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.library.varia.Color.EnumHCLFailureMode;
import net.minecraft.util.text.translation.I18n;

public class ColorMap
{
    public final int ordinal;
    public final Hue hue;
    public final Chroma chroma;
    public final Luminance luminance;
    
    private final int[] colors = new int[EnumColorMap.values().length];

    public ColorMap(Hue hue, Chroma chromaIn, Luminance luminanceIn, int ordinal)
    {
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
    
    public String localizedName()
    {
        @SuppressWarnings("deprecation")
        String format = I18n.translateToLocal(this.chroma == Chroma.PURE_NETURAL ? "color.format.pure_neutral" : "color.format.color");
        return String.format(format, this.hue.localizedName(), this.chroma.localizedName(), this.luminance.localizedName());
    
    }
    public static ColorMap makeColorMap(Hue hue, Chroma chromaIn, Luminance luminanceIn, int ordinal)
    {
        ColorMap newColorMap = new ColorMap(hue, chromaIn, luminanceIn, ordinal);
    
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
            Log.debug("makeColorMap produced invisible border color for " + newColorMap.localizedName());
        }
        newColorMap.setColor(EnumColorMap.BORDER, whichColor.RGB_int | 0xFF000000);
    
//        newColorMap.setColor(EnumColorMap.HIGHLIGHT,
//                NiceHues.INSTANCE.getHueSet(hue).getColorSetForHue(HuePosition.OPPOSITE).getColor(tint) | 0xFF000000);
        
        Color lampColor = Color.fromHCL(hue.hueDegrees(), baseColor.HCL_C, Color.HCL_MAX, EnumHCLFailureMode.NORMAL);
        if(lampColor.RGB_int == 0)
        {
            Log.info("whoops hcl" + hue.hueDegrees() + " " + chromaIn.value / 2 + " " + Color.HCL_MAX);
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
