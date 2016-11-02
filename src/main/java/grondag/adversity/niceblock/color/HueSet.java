package grondag.adversity.niceblock.color;

import grondag.adversity.library.Color;

public class HueSet
   {
//
//       private final ColorSet[] colorSets = new ColorSet[HuePosition.values().length];
//       
//       public HueSet(double hue)
//       {
//           for(HuePosition hp : HuePosition.values())
//           {
//               colorSets[hp.ordinal()] = new ColorSet(hue + hp.hueOffset);
//           }
//       }

//       public ColorSet getColorSetForHue(HuePosition offset)
//       {
//           return colorSets[offset.ordinal()];
//       }
//       
//       public class ColorSet
//       {
//           private final int[] colors = new int[Tint.values().length];
//
//           protected ColorSet(double hue)
//           {
//               for(Tint tint : Tint.values())
//               {
//                   colors[tint.ordinal()] = Color.fromHCL(hue, tint.chroma, tint.luminance).RGB_int | 0xFF000000;
//               }
//           }
//           
//           public int getColor(Tint tint)
//           {
//               return colors[tint.ordinal()];
//           }
//       }
       
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
           EXTRA_BRIGHT("Extra Bright", 90),
           BRIGHT("Bright", 82),
           LIGHT("Light", 67.75),
           MEDIUM_LIGHT("Medium Light", 53.5),
           MEDIUM_DARK("Medium Dark", 39.25),
           DARK("Dark", 25);
           
           public final String name;
           public final double value;
           
           private Luminance(String luminanceName, double luminanceValue)
           {
               this.name = luminanceName;
               this.value = luminanceValue;
           }
       }
   }