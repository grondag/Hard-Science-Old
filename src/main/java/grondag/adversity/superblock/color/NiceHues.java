package grondag.adversity.superblock.color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import grondag.adversity.Output;
import grondag.adversity.library.Color;
import grondag.adversity.superblock.color.HueSet.HuePosition;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("unused")
public class NiceHues 
{
    public static final NiceHues INSTANCE = new NiceHues();

    public static enum Hue
    {
        INFRARED,
        CHERRY,
        ROSE,
        POMEGRANATE,
        CRIMSON,
        SCARLET,
        RED,
        VERMILLION,
        TANGERINE,
        ORANGE,
        EMBER,
        SUNSET,
        PUMPKIN,
        CHEDDAR,
        MANGO,
        SUNFLOWER,
        GOLD,
        TORCH,
        YELLOW,
        LEMON,
        LIME,
        PERIDOT,
        CHARTREUSE,
        CACTUS,
        GREEN,
        FOLIAGE,
        MINT,
        SAGE,
        JUNIPER,
        CELADON,
        EMERALD,
        VERDIGRIS,
        TURQUOISE,
        SEA_FOAM,
        CYAN,
        ICE,
        BERYL,
        APATITE,
        MARINE,
        AQUA,
        ROBIN_EGG,
        MORNING,
        CERULEAN,
        TOPAZ,
        SKY,
        SAPPHIRE,
        PERIWINKLE,
        TWILIGHT,
        AZURE,
        OCEAN,
        COBALT,
        BLUE,
        LAPIS,
        INDIGO,
        VIOLET,
        PURPLE,
        AMETHYST,
        LILAC,
        MAGENTA,
        FUSCHIA,
        TULIP,
        PINK,
        PEONY;

        /**
         * Rotate our color cylinder by this many degrees
         * to tweak which colors we actually get.
         * A purely aesthetic choice.
         */
        private static final double HUE_SALT = 0;

        private int hueSample = 0;
     
        public double hueDegrees()
        {
            return this.ordinal() * 360.0 / Hue.values().length + HUE_SALT;
        }

        /**
         * Initialized lazily because ordinal not available during instantiation.
         * @return
         */
        public int hueSample()
        {
            if(hueSample == 0)
            {
                this.hueSample = Color.fromHCL(this.hueDegrees(), Color.HCL_MAX, Color.HCL_MAX).RGB_int | 0xFF000000;
            }
            return this.hueSample;
        }

        @SuppressWarnings("deprecation")
        public String localizedName()
        {
            return I18n.translateToLocal("color.hue." + this.name().toLowerCase());
        }
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

            buffer.write("<table class=\"w3-table-all\">"); buffer.newLine();
            buffer.write("<tr><th>Hue Name</th>");
            buffer.write("<th>RGB</th>");
            buffer.write("<th>H deg</th>");
            buffer.write("</tr>");
            for(Hue h : Hue.values())
            {
                buffer.write("<tr>");

                int color = h.hueSample & 0xFFFFFF;
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
            Output.warn("Unable to output color atlas due to file error:" + e.getMessage());
        }
    }

}
