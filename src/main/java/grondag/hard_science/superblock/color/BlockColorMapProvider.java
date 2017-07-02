package grondag.hard_science.superblock.color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import grondag.hard_science.Log;
import grondag.hard_science.library.varia.Color;
import grondag.hard_science.library.varia.Color.EnumHCLFailureMode;

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


    public static void writeColorAtlas(File folderName)
    {
        File output = new File(folderName, "hard_science_color_atlas.html");
        try
        {
            if(output.exists())
            {
                output.delete();
            }
            output.createNewFile();
    
            FileOutputStream fos = new FileOutputStream(output);
            BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
    
            buffer.write("<head>"); buffer.newLine();
            buffer.write("<style>"); buffer.newLine();
            buffer.write("table {"); buffer.newLine();
            buffer.write("    border-spacing: 1px 1px;"); buffer.newLine();
            buffer.write("}"); buffer.newLine();
            buffer.write("th, td {"); buffer.newLine();
            buffer.write("    height: 23px;"); buffer.newLine();
            buffer.write("    width: 130px;"); buffer.newLine();
            buffer.write("    vertical-align: center;");
            buffer.write("}"); buffer.newLine();
            buffer.write("th {"); buffer.newLine();
            buffer.write("    text-align: center;");
            buffer.write("}"); buffer.newLine();
            buffer.write("td {"); buffer.newLine();
            buffer.write("    text-align: right;");
            buffer.write("}"); buffer.newLine();
            buffer.write("</style>"); buffer.newLine();
            buffer.write("</head>"); buffer.newLine();
    
            buffer.write("<table class=\"w3-table-all\">"); buffer.newLine();
            buffer.write("<tr><th>Hue Name</th>");
            buffer.write("<th>RGB</th>");
            buffer.write("<th>H deg</th>");
            buffer.write("</tr>");
            for(Hue h : Hue.values())
            {
                buffer.write("<tr>");
    
                int color = h.hueSample() & 0xFFFFFF;
                buffer.write(String.format("<td style=\"background:#%1$06X\">" + h.localizedName() + "</td>", color));
    
                buffer.write(String.format("<td style=\"background:#%1$06X\">" + Integer.toHexString(color) + "</td>", color));
    
                buffer.write(String.format("<td style=\"background:#%1$06X\">" + Math.round(h.hueDegrees()) + "</td>", color));
                buffer.write("</tr>");
            }
            buffer.write("</table>");
            buffer.write("<h1>&nbsp;</h1>");
            buffer.close();
            fos.close();
    
        }
        catch (IOException e)
        {
            Log.warn("Unable to output color atlas due to file error:" + e.getMessage());
        }
    }
}
