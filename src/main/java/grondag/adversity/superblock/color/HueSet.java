package grondag.adversity.superblock.color;

public class HueSet
   {
       public static enum HuePosition
       {
           FAR_LEFT("Far Left", -120),
           NEAR_LEFT("Near Left", -23),
           NONE("None", 0),
           NEAR_RIGHT("Near Right", 23),
           FAR_RIGHT("Far Right", 120),
           OPPOSITE("Opposite", 180);
    
           public final String positionName;
           public final double hueOffset;
           
           private HuePosition(String positionName, double hueOffset)
           {
               this.positionName = positionName;
               this.hueOffset = hueOffset;
           }
       }
       
       public static enum Chroma
       {
           PURE_NETURAL("Pure Neutral", 0),
           WHITE("Faint", 2.5),
           GREY("Subtle", 5),
           NEUTRAL("Neutral", 10),
           RICH("Rich", 20),
           DEEP("Deep", 30),
           EXTRA_DEEP("Extra Deep", 40),
           BOLD("Bold", 50),
           SUPER_BOLD("Super Bold", 60),
           ACCENT("Accent", 70),
           ULTRA_ACCENT("Ultra Accent", 80),
           SUPER_ACCENT("Whoa", 90);
           
           public final String name;
           public final double value;
           
           private Chroma(String chromaName, double chromaValue)
           {
               this.name = chromaName;
               this.value = chromaValue;
           }
       }
       
       public static enum Luminance
       {
           BRILLIANT("Brilliant", 90),
           EXTRA_BRIGHT("Extra Bright", 81),
           BRIGHT("Bright", 72),
           EXTRA_LIGHT("Extra Light", 63),
           LIGHT("Light", 54),
           MEDIUM_LIGHT("Medium Light", 45),
           MEDIUM_DARK("Medium Dark", 36),
           DARK("Dark", 27),
           EXTRA_DARK("Extra Dark", 18);
           
           public final String name;
           public final double value;
           
           private Luminance(String luminanceName, double luminanceValue)
           {
               this.name = luminanceName;
               this.value = luminanceValue;
           }
       }
   }