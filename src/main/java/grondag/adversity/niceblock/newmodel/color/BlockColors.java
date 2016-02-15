package grondag.adversity.niceblock.newmodel.color;

import grondag.adversity.niceblock.newmodel.color.HueSet.HuePosition;
import grondag.adversity.niceblock.newmodel.color.HueSet.Tint;
import grondag.adversity.niceblock.newmodel.color.NiceHues.Hue;

public class BlockColors implements IColorProvider
{
    
    public static final BlockColors INSTANCE = new BlockColors();
    
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
        
        switch(subset)
        {
        case FLEXSTONE:
            retVal = new int[16];
            retVal[0] = getIndex(Hue.RED, Tint.WHITE);
            retVal[1] = getIndex(Hue.GREEN, Tint.WHITE);
            retVal[2] = getIndex(Hue.BLUE, Tint.WHITE);
            retVal[3] = getIndex(Hue.RED, Tint.GREY_FAINT);
            retVal[4] = getIndex(Hue.GREEN, Tint.GREY_FAINT);
            retVal[5] = getIndex(Hue.BLUE, Tint.GREY_FAINT);
            retVal[6] = getIndex(Hue.RED, Tint.GREY_LIGHT);
            retVal[7] = getIndex(Hue.GREEN, Tint.GREY_LIGHT);
            retVal[8] = getIndex(Hue.BLUE, Tint.GREY_LIGHT);
            retVal[9] = getIndex(Hue.RED, Tint.GREY_MID);
            retVal[10] = getIndex(Hue.GREEN, Tint.GREY_MID);
            retVal[11] = getIndex(Hue.CYAN, Tint.GREY_MID);
            retVal[12] = getIndex(Hue.BLUE, Tint.GREY_MID);
            retVal[13] = getIndex(Hue.RED, Tint.GREY_DEEP);
            retVal[14] = getIndex(Hue.GREEN, Tint.GREY_DEEP);
            retVal[15] = getIndex(Hue.BLUE, Tint.GREY_DEEP);
            break;
        default:
            retVal = new int[0];
        }

        return retVal;
    }

}
