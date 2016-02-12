package grondag.adversity.niceblock.newmodel.color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import grondag.adversity.Adversity;
import grondag.adversity.library.Color;

public class NiceColor2
{
   private static final HCLInputs HCL_ACCENTS = new HCLInputs("Accent Colors", 31, 10, 100, 30, 1, 94, 40);
//   private static final HCLInputs HCL_ACCENTS = new HCLInputs("Accent Colors", 32, 50, 85, 4, 30, 94, 6);
   private static final HCLInputs HCL_PASTELS = new HCLInputs("Pastels", 31, 85, 95, 4, 5, 25, 4);
   private static final HCLInputs HCL_DARK_NEUTRALS = new HCLInputs("Dark Neturals", 31, 30, 45, 4, 5, 25, 4);

   private static final HCLInputs HCL_BRIGHT_WHITES = new HCLInputs("Bright Whites", 40, 94, 94, 1, 2, 2, 1);
   private static final HCLInputs HCL_LIGHT_NEUTRALS = new HCLInputs("Light Neutrals", 40, 30, 85, 6, 7, 21.5, 3);

   private static final HCLInputs HCL_RAW_STONE = new HCLInputs("Raw Stone", 40, 40, 90, 6, 1, 6, 6);

   private static final LabInputs LAB_ALL = new LabInputs("All Colors", 20, 85, 20, -99, 99, 50, -99, 99, 50);
   private static final LabInputs LAB_WHITES = new LabInputs("Whites", 91, 94, 2, -2, 2, 6, -2, 2, 6);
   private static final LabInputs LAB_BRIGHTS = new LabInputs("Bright Accents", 85, 85, 1, -83, 30, 14, -22, 83, 12);
   private static final LabInputs LAB_LIGHT_ACCENTS = new LabInputs("Light Accents", 75, 85, 1, -79, 55, 14, -42, 75, 12);

   public static void makeAtlas(File folderName)
   {
   
       
       File output = new File(folderName, "adversity_color_debug.html");
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
           buffer.write("    width: 30px;"); buffer.newLine();
           buffer.write("    vertical-align: center;");
           buffer.write("    text-align: center;");
           buffer.write("}"); buffer.newLine();
           buffer.write("</style>"); buffer.newLine();
           buffer.write("</head>"); buffer.newLine();

           Integer blockColorCount = 0;
           
//           blockColorCount += generateColorsHCL(buffer, HCL_ACCENTS);
           blockColorCount += generateColorsHCL(buffer, HCL_BRIGHT_WHITES);
//           blockColorCount += generateColorsHCL(buffer, HCL_PASTELS);
           blockColorCount += generateColorsHCL(buffer, HCL_RAW_STONE);
//           blockColorCount += generateColorsHCL(buffer, HCL_DARK_NEUTRALS);
//           blockColorCount += generateColorsLab(buffer, LAB_WHITES);
           blockColorCount += generateColorsLab(buffer, LAB_BRIGHTS, 20);
           blockColorCount += generateColorsLab(buffer, LAB_LIGHT_ACCENTS, 20);
           
           buffer.write("<h2>Block Color Count = " + blockColorCount + "</h2>");  buffer.newLine();

           generateColorsLab(buffer, LAB_ALL, 0);

           buffer.close();
           fos.close();

       }
       catch (IOException e)
       {
           Adversity.log.warn("Unable to output color atlas due to file error:" + e.getMessage());
       }

   }


   private static int generateColorsHCL(BufferedWriter buffer, HCLInputs inputs) throws IOException
   {
       int blockColorCount = 0;

       buffer.write("<h1>" + inputs.title + "</h1>"); buffer.newLine();       
       buffer.write("<table class=\"w3-table-all\">"); buffer.newLine();       
       

       for(int c = 0; c < inputs.chromaSlices; c+= 1)
      {
          double chroma = (inputs.chromaSlices == 1) ? inputs.chromaMin 
                  : inputs.chromaMin + c * (inputs.chromaMax - inputs.chromaMin) / (inputs.chromaSlices - 1);

           for(int l = -1; l < inputs.luminanceSlices ; l++)
           {
               
           double luminance = (inputs.luminanceSlices == 1) ? inputs.luminanceMin
                   : inputs.luminanceMax - l * (inputs.luminanceMax - inputs.luminanceMin) / (inputs.luminanceSlices - 1);
           
                   
               buffer.write("<tr>"); buffer.newLine();
               buffer.write("<td>" + ((l == -1) ? (int)Math.round(chroma) : Math.round(luminance)) + "</td>"); buffer.newLine();

               for(int h = 0; h < inputs.hueSlices; h+= 1)
               {
                   double hue =  (double)(h * 360) / inputs.hueSlices;

                   if(l == -1)
                   {
                       buffer.write("<td>" + (int)Math.round(hue) + "</td>"); buffer.newLine();
                   }
                   else
                   {
                       Color color = Color.fromHCL(hue, chroma, luminance);
                       if(color.IS_VISIBLE)
                       {
                           buffer.write("<td style=\"background:#" + String.format("%06X", color.RGB_int) + "\">");
                           blockColorCount++;
                       }
                       else
                       {
                           buffer.write("<td>");
                       }
                   }
                   buffer.write("</td>");  buffer.newLine();
               }

               buffer.write("</tr>");  buffer.newLine();

           }
       }
       buffer.write("</table>");  buffer.newLine();
       return blockColorCount;
   }
   
   private static int generateColorsLab(BufferedWriter buffer, LabInputs inputs, double minChroma) throws IOException
   {
       int blockColorCount = 0;

       buffer.write("<h1>" + inputs.title + "</h1>"); buffer.newLine();       
       buffer.write("<table class=\"w3-table-all\">"); buffer.newLine();       
       
       for(int lum = 0; lum < inputs.luminanceSlices; lum+= 1)
       {
           double luminance = (inputs.luminanceSlices == 1) ? inputs.luminanceMin
                   : inputs.luminanceMax - lum * (inputs.luminanceMax - inputs.luminanceMin) / (inputs.luminanceSlices - 1);

           for(int a = -1; a < inputs.aSlices ; a++)
           {
               double a_val = inputs.aMax - a * (inputs.aMax - inputs.aMin) / (inputs.aSlices - 1);
               buffer.write("<tr>"); buffer.newLine();
               buffer.write("<td>" + ((a == -1) ? (int)Math.round(luminance) : Math.round(a_val)) + "</td>"); buffer.newLine();


               for(int b = 0; b < inputs.bSlices; b+= 1)
               {
                   double b_val = inputs.bMin + b * (inputs.bMax - inputs.bMin) / (inputs.bSlices - 1);
                   if(a == -1)
                   {
                       buffer.write("<td>" + (int)Math.round(b_val) + "</td>"); buffer.newLine();
                   }
                   else
                   {
                       Color color = Color.fromLab(luminance, a_val, b_val);
                       if(color.IS_VISIBLE && (color.HCL_C > minChroma))
                       {
                           buffer.write("<td style=\"background:#" + String.format("%06X", color.RGB_int) + "\">");
                           blockColorCount++;
                       }
                       else
                       {
                           buffer.write("<td>");
                       }
                   }
                   buffer.write("</td>");  buffer.newLine();
               }

               buffer.write("</tr>");  buffer.newLine();

           }
       }
       buffer.write("</table>");  buffer.newLine();
       return blockColorCount;
   }
    
     private static class LabInputs
    {
        public final String title;
        public final double luminanceMin;
        public final double luminanceMax;
        public final double luminanceSlices;   
        public final double aMin;
        public final double aMax;
        public final double aSlices;
        public final double bMin;
        public final double bMax;
        public final double bSlices;

        public LabInputs(String title, double luminanceMin, double luminanceMax, double luminanceSlices, double aMin, double aMax, double aSlices, double bMin, double bMax, double bSlices)
        {
            this.title = title;
            this.luminanceMin = luminanceMin;
            this.luminanceMax = luminanceMax;
            this.luminanceSlices = luminanceSlices;   
            this.aMin = aMin;
            this.aMax = aMax;
            this.aSlices = aSlices;           
            this.bMin = bMin;
            this.bMax = bMax;
            this.bSlices = bSlices;           
        }
    }

    private static class HCLInputs
    {
        public final String title;
        public final double hueSlices;
        public final double luminanceMin;
        public final double luminanceMax;
        public final double luminanceSlices;   
        public final double chromaMin;
        public final double chromaMax;
        public final double chromaSlices;
        
        public HCLInputs(String title, double hueSlices, double luminanceMin, double luminanceMax, double luminanceSlices, double chromaMin, double chromaMax, double chromaSlices)
        {
            this.title = title;
            this.hueSlices = hueSlices;
            this.luminanceMin = luminanceMin;
            this.luminanceMax = luminanceMax;
            this.luminanceSlices = luminanceSlices;   
            this.chromaMin = chromaMin;
            this.chromaMax = chromaMax;
            this.chromaSlices = chromaSlices;           
        }
    }
    
}
