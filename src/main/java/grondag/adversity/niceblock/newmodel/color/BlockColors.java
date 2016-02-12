package grondag.adversity.niceblock.newmodel.color;

import grondag.adversity.Adversity;
import grondag.adversity.library.Color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

public class BlockColors implements IColorProvider
{
    public static final BlockColors INSTANCE = new BlockColors();
    
    protected final BlockColor COLORS[];

    public static final int COUNT_WHITE = 12;
    public static final int COUNT_LIGHT = 32;
    public static final int COUNT_MID = 32;
    public static final int COUNT_DARK = 12;

    public static final int FIRST_WHITE = 0;
    public static final int FIRST_LIGHT = FIRST_WHITE + COUNT_WHITE;
    public static final int FIRST_MID = FIRST_LIGHT + COUNT_LIGHT;
    public static final int FIRST_DARK = FIRST_MID + COUNT_MID;
    
    public static final int[] SUBSET_FLEXSTONE = 
        {
            FIRST_WHITE + 5, 
            FIRST_WHITE + 6
        };
    
    public static final int[] SUBSET_DURASTONE1 =
    {
        FIRST_WHITE + 5, 
        FIRST_WHITE + 6
    };
    
    public static final int[] SUBSET_DURASTONE2 =
    {
        FIRST_WHITE + 5, 
        FIRST_WHITE + 6
    };
    
    public static final int[] SUBSET_DURASTONE_ALL =
    {
        FIRST_WHITE + 5, 
        FIRST_WHITE + 6
    };
    
    public static final int[] SUBSET_HYPERSTONE =
    {
        FIRST_WHITE + 5, 
        FIRST_WHITE + 6
    };

    public static final int[] SUBSET_SUPERWOOD =
    {
        FIRST_WHITE + 5, 
        FIRST_WHITE + 6
    };

    
    protected BlockColors()
    {
        ArrayList<BlockColor> colors = new ArrayList<BlockColor>();
        colors.addAll(makeColors("White", COUNT_WHITE, 1, 84));
        colors.addAll(makeColors("Light", COUNT_LIGHT, 5, 70));
        colors.addAll(makeColors("Mid", COUNT_MID, 15, 55));
        colors.addAll(makeColors("Dark", COUNT_DARK, 20, 30));
        COLORS = colors.toArray(new BlockColor[0]);
    }
    
    protected ArrayList<BlockColor> makeColors(String name, int slices, int chroma, int luminance)
    {
        ArrayList<BlockColor> retVal = new ArrayList<BlockColor>(slices);
        
        for(int h = 0; h < slices; h+= 1)
        {
            double hue =  (double)(h * 360) / slices;
            retVal.add(new BlockColor(name + " #" + h , hue, chroma, luminance));
         }        
        
        return retVal;
    }
    
     public void writeColorAtlas(File folderName)
    {
        File output = new File(folderName, "adversity_color_atlas.html");
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
            buffer.write("    height: 30px;"); buffer.newLine();
            buffer.write("    width: 100px;"); buffer.newLine();
            buffer.write("    vertical-align: center;");
            buffer.write("    text-align: center;");
            buffer.write("}"); buffer.newLine();
            buffer.write("</style>"); buffer.newLine();
            buffer.write("</head>"); buffer.newLine();


            for(int c = 0; c < COLORS.length; c++)
            {
                buffer.write("<h2>" + COLORS[c].colorName + "</h2>" ); buffer.newLine();
                buffer.write("<table class=\"w3-table-all\">"); buffer.newLine();
    
                buffer.write("<tr><th>Field</th><th>Far Left</th><th>Near Left</th><th>Base</th><th>Near Right</th><th>Far Right</th><th>&nbsp;</th><th>Base</th><th>Opposite</th></tr>");
                
                final String FORMAT = "<td style=\"background:#%1$06X\"></td><td style=\"background:#%2$06X\">"
                        + "</td><td style=\"background:#%3$06X\"></td><td style=\"background:#%4$06X\">"
                        + "</td><td style=\"background:#%5$06X\"><td>&nbsp;</td></td><td style=\"background:#%6$06X\">"
                        + "</td><td style=\"background:#%7$06X\"></td></tr>";
                
                buffer.write("<tr><td>normal</td>" + 
                        String.format(FORMAT, COLORS[c].farLeft.normal.RGB_int, COLORS[c].nearLeft.normal.RGB_int,
                                COLORS[c].base.normal.RGB_int, COLORS[c].nearRight.normal.RGB_int,
                                COLORS[c].farRight.normal.RGB_int, COLORS[c].base.normal.RGB_int, COLORS[c].opposite.normal.RGB_int));
                
                buffer.write("<tr><td>shade1</td>" + 
                        String.format(FORMAT, COLORS[c].farLeft.shade1.RGB_int, COLORS[c].nearLeft.shade1.RGB_int,
                                COLORS[c].base.shade1.RGB_int, COLORS[c].nearRight.shade1.RGB_int,
                                COLORS[c].farRight.shade1.RGB_int, COLORS[c].base.shade1.RGB_int, COLORS[c].opposite.shade1.RGB_int));

                buffer.write("<tr><td>shade2</td>" + 
                        String.format(FORMAT, COLORS[c].farLeft.shade2.RGB_int, COLORS[c].nearLeft.shade2.RGB_int,
                                COLORS[c].base.shade2.RGB_int, COLORS[c].nearRight.shade2.RGB_int,
                                COLORS[c].farRight.shade2.RGB_int, COLORS[c].base.shade2.RGB_int, COLORS[c].opposite.shade2.RGB_int));
                buffer.write("</table>");
            }
            buffer.close();
            fos.close();

        }
        catch (IOException e)
        {
            Adversity.log.warn("Unable to output color atlas due to file error:" + e.getMessage());
        }
    }
    
    
    @Override
    public int getColorCount()
    {
        return COLORS.length;
    }

    @Override
    public BlockColor getColor(int colorIndex)
    {
        return COLORS[colorIndex];
    }

}
