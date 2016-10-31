package grondag.adversity.niceblock.color;

import java.util.ArrayList;

import grondag.adversity.library.Color;
import grondag.adversity.niceblock.color.HueSet.Chroma;
import grondag.adversity.niceblock.color.HueSet.Luminance;
import grondag.adversity.niceblock.color.NiceHues.Hue;

public class BlockColorMapProvider implements IColorMapProvider
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
                    if(Color.fromHCL(hue.hueDegrees(), chroma.value, luminance.value).IS_VISIBLE)
                    {
                        ColorMap newMap = ColorMap.makeColorMap(hue, chroma, luminance, i++);
                        colorMaps.add(newMap);
                        allColors[hue.ordinal()][chroma.ordinal()][luminance.ordinal()] = newMap;
                    }
                }
            }
        }
        this.validColors = colorMaps.toArray(new ColorMap[0]);
    }
  
    
    @Override
    public int getColorMapCount()
    {
        return validColors.length;
    }

    @Override
    public ColorMap getColorMap(int colorIndex)
    {
        return validColors[Math.max(0, Math.min(validColors.length-1, colorIndex))];
    }
}
