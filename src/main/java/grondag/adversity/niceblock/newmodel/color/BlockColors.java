package grondag.adversity.niceblock.newmodel.color;

import java.util.ArrayList;

import grondag.adversity.niceblock.newmodel.color.HueSet.HuePosition;
import grondag.adversity.niceblock.newmodel.color.HueSet.Tint;
import grondag.adversity.niceblock.newmodel.color.IColorProvider.ColorSubset;
import grondag.adversity.niceblock.newmodel.color.NiceHues.Hue;

public class BlockColors implements IColorProvider
{
    
    public static final BlockColors ALL_BLOCK_COLORS = new BlockColors();
    public static final BlockColorsSubset FLEXSTONE_RAW = new BlockColorsSubset(ColorSubset.FLEXSTONE_RAW);
    public static final BlockColorsSubset DURASTONE_RAW = new BlockColorsSubset(ColorSubset.DURASTONE_RAW);
    public static final BlockColorsSubset NORMAL_BLOCK_COLORS = new BlockColorsSubset(ColorSubset.NORMAL_BLOCK_COLORS);
    
    private final int COLOR_COUNT = Hue.values().length * Tint.values().length;
    private final ColorVector[] COLORS = new ColorVector[COLOR_COUNT];

    protected BlockColors()
    {
        for(Hue hue: Hue.values())
        {
            for(Tint tint: Tint.values())
            {
                COLORS[getIndex(hue, tint)] = makeColorVector(hue, tint);
            }
        }
    }
  
    private int getIndex(Hue hue, Tint tint)
    {
        return Hue.values().length * hue.ordinal() + tint.ordinal();
    }
    private ColorVector makeColorVector(Hue hue, Tint tint)
    {
        String vectorName =  tint.tintName + " " + hue.hueName();
                
        return new ColorVector(vectorName,NiceHues.INSTANCE.getHueSet(hue)
                .getColorSetForHue(HuePosition.NONE).getColor(tint));
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

    @Override
    public int[] getSubset(ColorSubset subset)
    {
        int[] retVal;
        int i=0;
        
        switch(subset)
        {
        case FLEXSTONE_RAW:
            retVal = new int[1];
            retVal[i++] = getIndex(Hue.YELLOW, Tint.WHITE);
            break;

        case DURASTONE_RAW:
            retVal = new int[1];
            retVal[i++] = getIndex(Hue.COBALT, Tint.WHITE);
            break;
            
        case NORMAL_BLOCK_COLORS:
            retVal = new int[Hue.values().length * 24];

            for(Hue hue : Hue.values())
            {
                retVal[i++] = getIndex(hue, Tint.WHITE);
                retVal[i++] = getIndex(hue, Tint.GREY_BRIGHT);
                retVal[i++] = getIndex(hue, Tint.GREY_LIGHT);
                retVal[i++] = getIndex(hue, Tint.GREY_MID);
                retVal[i++] = getIndex(hue, Tint.GREY_DEEP);
                retVal[i++] = getIndex(hue, Tint.GREY_DARK);
    
                retVal[i++] = getIndex(hue, Tint.NEUTRAL_BRIGHT);
                retVal[i++] = getIndex(hue, Tint.NEUTRAL_LIGHT);
                retVal[i++] = getIndex(hue, Tint.NEUTRAL_MID);
                retVal[i++] = getIndex(hue, Tint.NEUTRAL_DEEP);
                retVal[i++] = getIndex(hue, Tint.NEUTRAL_DARK);
 
                retVal[i++] = getIndex(hue, Tint.RICH_BRIGHT);
                retVal[i++] = getIndex(hue, Tint.RICH_LIGHT);
                retVal[i++] = getIndex(hue, Tint.RICH_MID);
                retVal[i++] = getIndex(hue, Tint.RICH_DEEP);
                retVal[i++] = getIndex(hue, Tint.RICH_DARK);

                retVal[i++] = getIndex(hue, Tint.BOLD_BRIGHT);
                retVal[i++] = getIndex(hue, Tint.BOLD_LIGHT);
                retVal[i++] = getIndex(hue, Tint.BOLD_MID);
                retVal[i++] = getIndex(hue, Tint.BOLD_DEEP);
                retVal[i++] = getIndex(hue, Tint.BOLD_DARK);

                retVal[i++] = getIndex(hue, Tint.ACCENT_LIGHT);
                retVal[i++] = getIndex(hue, Tint.ACCENT_MID);
                retVal[i++] = getIndex(hue, Tint.ACCENT_DEEP);
            }
            break;
 
        case ALL_COLORS:
            retVal = new int[COLOR_COUNT];
            for(int index = 0; index < COLOR_COUNT; index++)
            {
                retVal[index] = index;
            }
            return retVal;

        default:
            retVal = new int[0];
        }

        return retVal;
    }

    public static class BlockColorsSubset implements IColorProvider
    {
        private final int COLOR_COUNT;
        private final ColorVector[] COLORS;

        public BlockColorsSubset(ColorSubset subset)
        {
            
            int[] included = BlockColors.ALL_BLOCK_COLORS.getSubset(subset);
            
            COLOR_COUNT = included.length;
            COLORS = new ColorVector[COLOR_COUNT];
            
            for(int i = 0; i < COLOR_COUNT; i++)
            {
                COLORS[i] = BlockColors.ALL_BLOCK_COLORS.getColor(included[i]);
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

        @Override
        public int[] getSubset(ColorSubset subset)
        {
            if (subset == ColorSubset.ALL_COLORS)
            {
                int[] retVal = new int[COLOR_COUNT];
                for(int index = 0; index < COLOR_COUNT; index++)
                {
                    retVal[index] = index;
                }
                return retVal;
            }
            else
            {
                return new int[0];
            }
        }
        
    }
}
