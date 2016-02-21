package grondag.adversity.niceblock.newmodel.color;

import grondag.adversity.Adversity;
import grondag.adversity.library.Color;
import grondag.adversity.niceblock.newmodel.color.HueSet.HuePosition;
import grondag.adversity.niceblock.newmodel.color.HueSet.Tint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

public class NiceHues 
{
    public static final NiceHues INSTANCE = new NiceHues();
    
    private final HueSet HUES[];
    
    private NiceHues()
    {
        ArrayList<HueSet> colors = new ArrayList<HueSet>();
        for(Hue h : Hue.values())
        {
            colors.add(new HueSet(h.hueDegrees()));
        }
        HUES = colors.toArray(new HueSet[0]);
        
        // was used to probe limits of colors across all hues
//      for(int c = 1; c < 100; c++)
//      {
//          int minLum = 100;
//          int maxLum  = 0;
//          for(int l = 20; l < 100; l++)
//          {
//              int validCount = 0;
//
//              for(int h = 0; h < 360; h++)
//              {
//                  Color test = Color.fromHCL(h, c, l);
//                  if(test.IS_VISIBLE && test.RGB_int != 0)
//                  {
//                      validCount++;
//                  }
//          
//              }
//              if(validCount == 360)
//              {
//                  minLum = Math.min(minLum, l);
//                  maxLum = Math.max(maxLum, l);
//              }
//          }
//          if(minLum < 100 && maxLum > 0)
//          {
//              Adversity.log.info("chroma ,minLum, maxLum: " + c + ", " + minLum + ", " + maxLum);
//          }
//      }
    }
    
    public HueSet getHueSet(Hue hue)
    {
        return HUES[hue.ordinal()];
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
            for(Hue h : Hue.values())
            {
                buffer.write("<table class=\"w3-table-all\">"); buffer.newLine();
                
                buffer.write("<tr><th>" + h.hueName() + "</th>");
                buffer.write("<th>" + HuePosition.NONE.positionName + "</th>");
                buffer.write("<th>" + HuePosition.OPPOSITE.positionName + "</th>");
                buffer.write("<th>" + HuePosition.FAR_LEFT.positionName + "</th>");
                buffer.write("<th>" + HuePosition.NEAR_LEFT.positionName + "</th>");
                buffer.write("<th>" + HuePosition.NONE.positionName + "</th>");
                buffer.write("<th>" + HuePosition.NEAR_RIGHT.positionName + "</th>");
                buffer.write("<th>" + HuePosition.FAR_RIGHT.positionName + "</th>");
                buffer.write("</tr>");
                
                for(Tint t : Tint.values())
                {
                    buffer.write("<tr><td>" + t.tintName + "</td>");

                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.NONE).getColor(t) & 0xFFFFFF));
                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.OPPOSITE).getColor(t) & 0xFFFFFF));
                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.FAR_LEFT).getColor(t) & 0xFFFFFF));
                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.NEAR_LEFT).getColor(t) & 0xFFFFFF));
                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.NONE).getColor(t) & 0xFFFFFF));
                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.NEAR_RIGHT).getColor(t) & 0xFFFFFF));
                    buffer.write(String.format("<td style=\"background:#%1$06X\"></td>", 
                            HUES[h.ordinal()].getColorSetForHue(HuePosition.FAR_RIGHT).getColor(t) & 0xFFFFFF));

                    buffer.write("</tr>");
                }
                buffer.write("</table>");
                buffer.write("<h1>&nbsp;</h1>");
            }

            buffer.close();
            fos.close();

        }
        catch (IOException e)
        {
            Adversity.log.warn("Unable to output color atlas due to file error:" + e.getMessage());
        }
    }
     
    public static enum Hue
    {
        GOLD("Gold"),
        ORANGE("Orange"),
        ORANGE_RED("Orange-Red"),
        BRIGHT_RED("Bright Red"),
        RED("Red"),
        BURGUNDY("Burgundy"),
        FUSCHIA("Fuschia"),
        MAGENTA("Magenta"),
        LAVENDER("Lavender"),
        VIOLET("Violet"),
        ULTRAMARINE("Ultramarine"),
        COBALT("Cobalt"),
        BLUE("Blue"),
        AZURE("Azure"),
        CERULEAN("Cerulean"),
        TEAL("Teal"),
        AQUA("Aqua"),
        CYAN("Cyan"),
        BLUE_GREEN("Blue-Green"),
        COOL_GREEN("Cool Green"),
        GREEN("Green"),
        BRIGHT_GREEN("Bright Green"),
        LIME("Lime"),
        YELLOW("Yellow");
        
        private final String hueName;
        
        /**
         * Rotate our color cylinder by this many degrees
         * to tweak which colors we actually get.
         * A purely aesthetic choice.
         */
        private static final double HUE_SALT = 5.12;

        
        Hue(String hueName)
        {
            this.hueName = hueName;
        }
        
        public String hueName()
        {
            return hueName;
        }
        
        public double hueDegrees()
        {
            return this.ordinal() * 360 / this.values().length + HUE_SALT;
        }
    }

}
