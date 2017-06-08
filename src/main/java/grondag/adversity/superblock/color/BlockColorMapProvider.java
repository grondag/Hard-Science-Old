package grondag.adversity.superblock.color;

import java.util.ArrayList;

import grondag.adversity.Output;
import grondag.adversity.library.Color;
import grondag.adversity.library.Color.EnumHCLFailureMode;
import grondag.adversity.superblock.color.HueSet.Chroma;
import grondag.adversity.superblock.color.HueSet.Luminance;
import grondag.adversity.superblock.color.NiceHues.Hue;

@SuppressWarnings("unused")
public class BlockColorMapProvider
{
    public static final BlockColorMapProvider INSTANCE = new BlockColorMapProvider();
    // note: can't be static because must come after Hue static initializaiton
    public final int hueCount = Hue.values().length;
    private final ColorMap[] validColors;
    private final ColorMap[][][] allColors = new ColorMap[hueCount][Chroma.values().length][Luminance.values().length];
    protected BlockColorMapProvider()
    {
        
        ArrayList<ColorMap> colorMaps = new ArrayList<ColorMap>(allColors.length);
        int i=0;

        for(Hue hue: Hue.values())
        {
            for(Luminance luminance : Luminance.values())
            {
                for(Chroma chroma : Chroma.values())
                {
                    if(chroma != Chroma.PURE_NETURAL)
                    {
                        Color testColor = Color.fromHCL(hue.hueDegrees(), chroma.value, luminance.value, EnumHCLFailureMode.REDUCE_CHROMA);
                        
                        if(testColor.IS_VISIBLE && testColor.HCL_C > chroma.value - 6)
                        {
                            ColorMap newMap = ColorMap.makeColorMap(hue, chroma, luminance, i++);
                            colorMaps.add(newMap);
                            allColors[hue.ordinal()][chroma.ordinal()][luminance.ordinal()] = newMap;
                        }
                    }
                }
            }
        }
        
        // pure neutrals
        for(Luminance luminance : Luminance.values())
        {
            Color testColor = Color.fromHCL(Hue.BLUE.hueDegrees(), Chroma.PURE_NETURAL.value, luminance.value, EnumHCLFailureMode.REDUCE_CHROMA);
            
            if(testColor.IS_VISIBLE)
            {
                ColorMap newMap = ColorMap.makeColorMap(Hue.BLUE, Chroma.PURE_NETURAL, luminance, i++);
                colorMaps.add(newMap);

                for(Hue hue: Hue.values())
                {
                    allColors[hue.ordinal()][Chroma.PURE_NETURAL.ordinal()][luminance.ordinal()] = newMap;
                }
            }
        }
        
        this.validColors = colorMaps.toArray(new ColorMap[0]);
    }
  
   
    public int getColorMapCount()
    {
        return validColors.length;
    }

    public ColorMap getColorMap(int colorIndex)
    {
        return validColors[Math.max(0, Math.min(validColors.length-1, colorIndex))];
    }
    
    /** may return NULL */
    public ColorMap getColorMap(Hue hue, Chroma chroma, Luminance luminance)
    {
        return allColors[hue.ordinal()][chroma.ordinal()][luminance.ordinal()];
    }
}
